package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.TransformerMongoProject;
import uk.ac.ebi.pride.ws.pride.models.Dataset;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
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
    public HttpEntity<List<Dataset>> facets(@RequestParam(value="pageSize", defaultValue = "100", required = false) Integer pageSize,
                                                            @RequestParam(value="pageNumber", defaultValue = "1", required = false ) Integer pageNumber,
                                                            @RequestParam(value="resultType", defaultValue = WsContastants.COMPACT, required = false) String resultType,
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

        List<Dataset> datasets  = mongoProjectService.findAll(PageRequest.of(pageNumber -1, pageSize)).getContent().stream().map( new TransformerMongoProject()).collect(Collectors.toList());

        return new HttpEntity<>(datasets);
    }

}
