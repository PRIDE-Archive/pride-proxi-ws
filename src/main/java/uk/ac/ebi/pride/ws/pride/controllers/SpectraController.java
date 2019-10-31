package uk.ac.ebi.pride.ws.pride.controllers;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.services.S3SpectralArchive;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.TransformerMongoProject;
import uk.ac.ebi.pride.ws.pride.assemblers.TransformerMongoSpectra;
import uk.ac.ebi.pride.ws.pride.models.ISpectrum;
import uk.ac.ebi.pride.ws.pride.models.Spectrum;
import uk.ac.ebi.pride.ws.pride.models.SpectrumStatus;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@Slf4j
public class SpectraController {


    S3SpectralArchive spectralArchive;
    PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public SpectraController(S3SpectralArchive spectralArchive, PrideMoleculesMongoService moleculesMongoService){
        this.spectralArchive = spectralArchive;
        this.moleculesMongoService = moleculesMongoService;
    }

//    @ApiOperation(notes = "Get an Spectrum by the specific usi", value = "spectra", nickname = "getSpectrum", tags = {"spectra"} )
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "OK", response = APIError.class),
//            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
//    })
//    @RequestMapping(value = "/spectrum", method = RequestMethod.GET,
//            produces = {MediaType.APPLICATION_JSON_VALUE})
//    public HttpEntity<Object> getSpectrum(@RequestParam(value = "usi", required = true) String usi){
//
//        Optional<PSMProvider> spectrumOptional = Optional.empty();
//        SpectraResourceAssembler assembler = new SpectraResourceAssembler(SpectraEvidenceController.class,
//                SpectrumEvidenceResource.class);
//        try {
//            PSMProvider evidence = spectralArchive.readPSM(usi);
//            if(evidence != null)
//                spectrumOptional = Optional.of(evidence);
//        } catch (Exception e) {
//            log.error(e.getMessage(),e);
//        }
//
//        return spectrumOptional.<ResponseEntity<Object>>map(spectrum ->
//                new ResponseEntity<>(assembler.toResource(spectrum), HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<>(WsContastants.PROTEIN_NOT_FOUND
//                        + usi + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
//    }

    @ApiOperation(notes = "Get an Spectrum by the Project Accession or Assay Accession usi", value = "spectra", nickname = "getSpectrumBy", tags = {"spectra"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/spectra", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    //Todo: All the spectra retrieve methods should be done using java.util.concurrent.CompletableFuture from Spring.
    public HttpEntity<Collection<?  extends ISpectrum>> getSpectrumBy(@RequestParam(value = "usi", required = false) String usi,
                                                     @RequestParam(value = "peptideSequence", required = false) String peptideSequence,
                                                     @RequestParam(value = "resultType", defaultValue = "compact", required = false) WsContastants.ResultType resultType,
                                                     @RequestParam(value = "accession", required = false) String accession,
                                                     @RequestParam(value="pageNumber", defaultValue = "1" ,  required = false) int pageNumber,
                                                     @RequestParam(value="pageSize", defaultValue = "1", required = false) int pageSize){


        Page<PrideMongoPsmSummaryEvidence> peptides = null;
        if(usi != null && !usi.isEmpty())
            peptides = moleculesMongoService
                    .findPsmSummaryEvidences(Collections.singletonList(usi), PageRequest.of(pageNumber, pageSize));
        else if(accession != null || peptideSequence != null)
            peptides = moleculesMongoService
                    .findPsmSummaryEvidences(accession, "", "", peptideSequence,
                    PageRequest.of(pageNumber, pageSize));
        else
            peptides = moleculesMongoService.listPsmSummaryEvidences(PageRequest.of(pageNumber, pageSize));

        ConcurrentLinkedQueue<ISpectrum> spectra = new ConcurrentLinkedQueue<>();
        TransformerMongoSpectra transformer = new TransformerMongoSpectra(SpectrumStatus.READABLE);

        peptides.getContent().parallelStream().forEach( psmEvidence ->  {
            try {
                if(resultType == WsContastants.ResultType.compact)
                    spectra.add(transformer.apply(psmEvidence));
                else
                    spectra.add(transformer.apply(spectralArchive.readPSM(psmEvidence.getUsi())));
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        });

        return new HttpEntity<Collection<? extends ISpectrum>>(spectra);
    }

}
