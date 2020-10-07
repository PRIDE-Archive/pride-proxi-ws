package uk.ac.ebi.pride.ws.pride.controllers;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.archive.spectra.services.S3SpectralArchive;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.StringUtils;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.TransformerMongoSpectra;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.TransformerPsm;
import uk.ac.ebi.pride.ws.pride.models.ErrorMessage;
import uk.ac.ebi.pride.ws.pride.models.molecules.ISpectrum;
import uk.ac.ebi.pride.ws.pride.models.molecules.Psm;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumStatus;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@Slf4j
public class SpectraController {


    S3SpectralArchive spectralArchive;
    PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public SpectraController(S3SpectralArchive spectralArchive, PrideMoleculesMongoService moleculesMongoService) {
        this.spectralArchive = spectralArchive;
        this.moleculesMongoService = moleculesMongoService;
    }

    @ApiOperation(notes = "Get an Spectrum by the Project Accession or Assay Accession usi", value = "spectra", nickname = "getSpectrumBy", tags = {"spectra"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/spectra", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    //Todo: All the spectra retrieve methods should be done using java.util.concurrent.CompletableFuture from Spring.
    public ResponseEntity getSpectrumBy(@RequestParam(value = "usi", required = false) String usi,
                                                                     @RequestParam(value = "peptideSequence", required = false) String peptideSequence,
                                                                     @RequestParam(value = "resultType", defaultValue = "compact") WsContastants.ResultType resultType,
                                                                     @RequestParam(value = "accession", required = false) String accession,
                                                                     @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
                                                                     @RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(pageNumber, pageSize);
        pageNumber = facetPageParams.getKey();
        pageSize = facetPageParams.getValue();

        Page<PrideMongoPsmSummaryEvidence> peptides = null;
        if (!StringUtils.isEmpty(usi)){
            String[] usiString = usi.split(":");
            String spectraUSI = String.join(":", usiString[0], usiString[1], usiString[2], usiString[3], usiString[4]);
            Optional<PrideMongoPsmSummaryEvidence> psm = moleculesMongoService
                    .findPsmSummaryEvidencesSpectraUsi(spectraUSI);
            if (psm.isPresent())
                peptides = new PageImpl<PrideMongoPsmSummaryEvidence>(Collections.singletonList(psm.get()), PageRequest.of(pageNumber, pageSize).first(),1);
            else
                peptides = moleculesMongoService.findPsmSummaryEvidences(Collections.singletonList(usi), PageRequest.of(pageNumber, pageSize));

        }else if (!StringUtils.isEmpty(accession) || !StringUtils.isEmpty(peptideSequence))
            peptides = moleculesMongoService
                    .findPsmSummaryEvidences(accession, "", "", peptideSequence,
                            PageRequest.of(pageNumber, pageSize));
        else
            peptides = moleculesMongoService.listPsmSummaryEvidences(PageRequest.of(pageNumber, pageSize));

        ConcurrentLinkedQueue<ISpectrum> spectra = new ConcurrentLinkedQueue<>();
        TransformerMongoSpectra transformer = new TransformerMongoSpectra(SpectrumStatus.READABLE);

        peptides.getContent().parallelStream().forEach(psmEvidence -> {
            try {
                if (resultType == WsContastants.ResultType.compact)
                    spectra.add(transformer.apply(psmEvidence));
                else
                    spectra.add(transformer.apply(spectralArchive.readPSM(psmEvidence.getUsi())));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });

        if(spectra.size() == 0){
            return new ResponseEntity<>(ErrorMessage.builder().status(HttpStatus.NOT_FOUND).detail("The usi has been found").title("ScanNotFound").build(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(spectra, HttpStatus.OK);
    }

    @ApiOperation(notes = "Get specific Peptide Spectrum Matches (PSMs) if they are present in the database and have been identified by a previous experiment in the resource.", value = "psms", nickname = "getPsms", tags = {"spectra"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 422, message = "UNPROCESSABLE_ENTITY"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/psms", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Collection<? extends Psm>> getPsms(@RequestParam(value = "resultType", defaultValue = "compact") WsContastants.ResultType resultType,
                                                         @RequestParam(value = "usi", required = false) String usi,
                                                         @RequestParam(value = "accession", required = false) String accession,
                                                         @RequestParam(value = "msrun", required = false) String msrun,
                                                         @RequestParam(value = "scan", required = false) String scan,
                                                         @RequestParam(value = "passThreshold", required = false) Boolean passThreshold,
                                                         @RequestParam(value = "peptideSequence", required = false) String peptideSequence,
                                                         @RequestParam(value = "proteinAccession", required = false) String proteinAccession,
                                                         @RequestParam(value = "charge", required = false) Integer charge,
                                                         @RequestParam(value = "modification", required = false) String modification,
                                                         @RequestParam(value = "peptidoform", required = false) String peptidoform,
                                                         @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
                                                         @RequestParam(value = "pageSize", defaultValue = "100") int pageSize) {


        if (!StringUtils.isEmpty(proteinAccession)) {
            return new ResponseEntity("'proteinAccession' parameter is not supported in PRIDE", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (passThreshold != null && !passThreshold) { // all the entries in our DB have passThreshold
            return new HttpEntity<>(Collections.emptyList());
        }

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(pageNumber, pageSize);
        pageNumber = facetPageParams.getKey();
        pageSize = facetPageParams.getValue();

        Page<PrideMongoPsmSummaryEvidence> peptides = null;
        if (!StringUtils.isEmpty(usi))
            peptides = moleculesMongoService.findPsmSummaryEvidences(Collections.singletonList(usi), PageRequest.of(pageNumber, pageSize));
        else if (!StringUtils.isEmpty(accession) || !StringUtils.isEmpty(msrun) || !StringUtils.isEmpty(scan)
                || !StringUtils.isEmpty(peptideSequence) || charge != null || !StringUtils.isEmpty(modification)
                || !StringUtils.isEmpty(peptidoform)) //TODO peptidoform should be a separate field in DB..regex is too slow
            peptides = moleculesMongoService
                    .findPsmSummaryEvidences(accession, msrun, scan,
                            peptideSequence, charge, modification, peptidoform,
                            PageRequest.of(pageNumber, pageSize));
        else
            peptides = moleculesMongoService.listPsmSummaryEvidences(PageRequest.of(pageNumber, pageSize));

        ConcurrentLinkedQueue<Psm> psms = new ConcurrentLinkedQueue<>();
        peptides.getContent().parallelStream().forEach(psmEvidence -> {
            try {
                if (resultType == WsContastants.ResultType.compact)
                    psms.add(TransformerPsm.apply(psmEvidence));
                else
                    psms.add(TransformerPsm.apply(psmEvidence, spectralArchive.readPSM(psmEvidence.getUsi())));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });

        return new HttpEntity<>(psms);
    }
}
