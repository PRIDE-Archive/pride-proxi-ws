package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.param.MongoCvParam;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.TransformerMongoProject;
import uk.ac.ebi.pride.ws.pride.models.Dataset;
import uk.ac.ebi.pride.ws.pride.models.IDataset;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectResource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class DatasetController {


    final PrideFileMongoService mongoFileService;

    final PrideProjectMongoService mongoProjectService;

    @Autowired
    public DatasetController(
                             PrideFileMongoService mongoFileService,
                             PrideProjectMongoService mongoProjectService) {
        this.mongoFileService = mongoFileService;
        this.mongoProjectService = mongoProjectService;
    }

    @ApiOperation(notes = "List of datasets in the respository", value = "datasets", nickname = "listDatasets", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 400, message = "Bad request. The requested paramters are not supported.", response = APIError.class),
            @ApiResponse(code = 401, message = "Authorization information is missing or invalid.", response = APIError.class),
            @ApiResponse(code = 404, message = "No data found for the specified paramters.", response = APIError.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = APIError.class)

    })
    @RequestMapping(value = "/datasets", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<List<IDataset>> listDatasets(@RequestParam(value="pageSize", defaultValue = "100", required = false) Integer pageSize,
                                                            @RequestParam(value="pageNumber", defaultValue = "1", required = false ) Integer pageNumber,
                                                            @RequestParam(value="resultType", defaultValue = WsContastants.COMPACT, required = false) WsContastants.ResultType resultType,
                                                            @RequestParam(value="species", required = false) String species,
                                                            @RequestParam(value="accession", required = false) String accession,
                                                            @RequestParam(value="instrument", required = false) String instrument,
                                                            @RequestParam(value="contact", required = false) String contact,
                                                            @RequestParam(value="modification", required = false) String modification,
                                                            @RequestParam(value="publication", required = false) String publication,
                                                            @RequestParam(value="search", required = false) String search){

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(pageNumber, pageSize);
        pageNumber = facetPageParams.getKey();
        pageSize   = facetPageParams.getValue();

        List<IDataset> datasets  = mongoProjectService.findAll(PageRequest.of(pageNumber -1, pageSize)).getContent()
                .stream()
                .map(new TransformerMongoProject(resultType)).collect(Collectors.toList());
        if(resultType == WsContastants.ResultType.Full){
            List<String> accessions = datasets.stream().map(IDataset::getAccession).collect(Collectors.toList());
            List<MongoPrideFile> mongoFiles = mongoFileService.findFilesByProjectAccessions(accessions);
            datasets.forEach(x-> {
                List<MongoPrideFile> files = mongoFiles.stream().filter( y -> y.getProjectAccessions().contains(x.getAccession())).collect(Collectors.toList());
                ((Dataset) x).setDataFiles(files.stream()
                        .map(file -> {
                            String value = "";
                            Optional<MongoCvParam> cvParam = file.getPublicFileLocations()
                                    .stream()
                                    .filter(accTerm -> accTerm.getAccession().equalsIgnoreCase(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession()))
                                    .findFirst();
                            if(cvParam.isPresent())
                                value = cvParam.get().getValue();
                            return OntologyTerm.builder()
                                    .accession(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession())
                                    .name(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getName())
                                    .cvLabel(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getCvLabel())
                                    .value(value)
                                    .build();
                        })
                        .collect(Collectors.toList())
                );
            });
        }

        return new HttpEntity<>(datasets);
    }

    @ApiOperation(notes = "Get dataset by Accession", value = "dataset", nickname = "getDataset", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 400, message = "Bad request. The requested paramters are not supported.", response = APIError.class),
            @ApiResponse(code = 401, message = "Authorization information is missing or invalid.", response = APIError.class),
            @ApiResponse(code = 404, message = "No data found for the specified paramters.", response = APIError.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = APIError.class)

    })
    @RequestMapping(value = "/datasets/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<IDataset> listDatasets(@PathVariable(name = "accession", required = true) String accession){

        Optional<MongoPrideProject> mongoPrideProject  = mongoProjectService.findByAccession(accession);
        IDataset dataset = null;
        if(mongoPrideProject.isPresent())
            dataset = new TransformerMongoProject(WsContastants.ResultType.Full).apply(mongoPrideProject.get());

        if(dataset != null){
            List<MongoPrideFile> mongoFiles = mongoFileService.findFilesByProjectAccession(dataset.getAccession());
            IDataset finalDataset = dataset;
            List<MongoPrideFile> files = mongoFiles.stream().filter(y -> y.getProjectAccessions().contains(finalDataset.getAccession())).collect(Collectors.toList());
            ((Dataset)dataset).setDataFiles(files.stream()
                    .map(file -> {
                        String value = "";
                            Optional<MongoCvParam> cvParam = file.getPublicFileLocations()
                                    .stream()
                                    .filter(accTerm -> accTerm.getAccession().equalsIgnoreCase(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession()))
                                    .findFirst();
                            if(cvParam.isPresent())
                                value = cvParam.get().getValue();
                            return OntologyTerm.builder()
                                    .accession(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession())
                                    .name(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getName())
                                    .cvLabel(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getCvLabel())
                                    .value(value)
                                    .build();
                        })
                        .collect(Collectors.toList())
            );
        }

        return new HttpEntity<>(dataset);
    }

}
