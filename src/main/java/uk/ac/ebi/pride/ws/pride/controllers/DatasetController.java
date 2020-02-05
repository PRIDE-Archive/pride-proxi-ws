package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectField;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectFieldEnum;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.pride.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.TransformerMongoProject;
import uk.ac.ebi.pride.ws.pride.models.Dataset;
import uk.ac.ebi.pride.ws.pride.models.IDataset;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectResource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class DatasetController {

    private final SolrProjectService solrProjectService;
    private final PrideFileMongoService mongoFileService;
    private final PrideProjectMongoService mongoProjectService;

    @Autowired
    public DatasetController(
            SolrProjectService solrProjectService, PrideFileMongoService mongoFileService,
            PrideProjectMongoService mongoProjectService) {
        this.solrProjectService = solrProjectService;
        this.mongoFileService = mongoFileService;
        this.mongoProjectService = mongoProjectService;
    }

    @ApiOperation(notes = "List of datasets in the respository", value = "datasets", nickname = "listDatasets", tags = {"datasets"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 400, message = "Bad request. The requested paramters are not supported.", response = APIError.class),
            @ApiResponse(code = 401, message = "Authorization information is missing or invalid.", response = APIError.class),
            @ApiResponse(code = 404, message = "No data found for the specified paramters.", response = APIError.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = APIError.class)

    })
    @RequestMapping(value = "/datasets", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<List<IDataset>> listDatasets(@RequestParam(value = "pageSize", defaultValue = "100", required = false) Integer pageSize,
                                                   @RequestParam(value = "pageNumber", defaultValue = "1", required = false) Integer pageNumber,
                                                   @RequestParam(value = "resultType", defaultValue = WsContastants.COMPACT, required = false) WsContastants.ResultType resultType,
                                                   @RequestParam(value = "species", required = false) String speciesStr,
                                                   @RequestParam(value = "accession", required = false) String accessionsStr,
                                                   @RequestParam(value = "instrument", required = false) String instrumentsStr,
                                                   @RequestParam(value = "contact", required = false) String contact,
                                                   @RequestParam(value = "modification", required = false) String modification,
                                                   @RequestParam(value = "publication", required = false) String publicationsStr,
                                                   @RequestParam(value = "search", required = false) String searchStr) {

        pageNumber = pageNumber - 1; //1st page as per proxi-api spec is 1 but for solr it's 0
        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(pageNumber, pageSize);
        pageNumber = facetPageParams.getKey();
        pageSize = facetPageParams.getValue();

        String sortFields = PrideProjectField.PROJECT_SUBMISSION_DATE;
        Sort.Direction sortDirection = Sort.Direction.DESC;
        String dateGap = "";
        List<String> keywords = new ArrayList<>(Collections.singleton("*:*"));
        if (searchStr != null) {
            keywords = Arrays.asList(searchStr.trim().split(","));
        }

        String FILTER_SEPARATOR = "==";
        String FILTER_CONCATINATOR = ",";

        List<String> filters = new ArrayList<>();

        if (accessionsStr != null) {
            List<String> accessions = Arrays.stream(accessionsStr.split(",")).map(x ->
                    PrideProjectFieldEnum.ACCESSION.getFilterField() + FILTER_SEPARATOR + x.trim())
                    .collect(Collectors.toList());

            filters.addAll(accessions);
        }

        if (speciesStr != null) {
            List<String> species = Arrays.stream(speciesStr.split(",")).map(x ->
                    PrideProjectFieldEnum.ORGANISM.getFilterField() + FILTER_SEPARATOR + x.trim())
                    .collect(Collectors.toList());

            filters.addAll(species);
        }

        if (instrumentsStr != null) {
            List<String> instruments = Arrays.stream(instrumentsStr.split(",")).map(x ->
                    PrideProjectFieldEnum.INSTRUMENTS.getFilterField() + FILTER_SEPARATOR + x.trim())
                    .collect(Collectors.toList());

            filters.addAll(instruments);
        }

        if (publicationsStr != null) {
            List<String> publications = Arrays.stream(publicationsStr.split(",")).map(x ->
                    PrideProjectFieldEnum.PROJECT_REFERENCES.getFilterField() + FILTER_SEPARATOR + x.trim())
                    .collect(Collectors.toList());

            filters.addAll(publications);
        }

        if (contact != null) {
            //Have to choose between LAB_HEAD or SUBMITTER.. trying to filter by both will result in 'AND' condition
            //filters.add(PrideProjectFieldEnum.PROJECT_PI_NAMES.getFilterField() + FILTER_SEPARATOR + contact.trim());
            filters.add(PrideProjectFieldEnum.PROJECT_SUBMITTER.getFilterField() + FILTER_SEPARATOR + contact.trim());
        }

        if (modification != null) {
            filters.add(PrideProjectFieldEnum.PROJECT_IDENTIFIED_PTM_STRING.getFilterField() + FILTER_SEPARATOR + modification.trim());
        }

        String filterQuery = String.join(FILTER_CONCATINATOR, filters);

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keywords, filterQuery, PageRequest.of(pageNumber, pageSize, sortDirection, sortFields.split(",")), dateGap);

        List<String> prjAccessions = solrProjects.stream().map(PrideSolrProject::getAccession).collect(Collectors.toList());

        List<IDataset> datasets = mongoProjectService.findByMultipleAccessions(prjAccessions).stream()
                .map(new TransformerMongoProject(resultType)).collect(Collectors.toList());

        if (resultType == WsContastants.ResultType.full) {
            List<List<OntologyTerm>> accessions = datasets.stream().map(IDataset::getAccession).collect(Collectors.toList());
            List<String> accessionsStrs = accessions.stream().flatMap(List::stream).map(OntologyTerm::getValue).collect(Collectors.toList());
            List<MongoPrideFile> mongoFiles = mongoFileService.findFilesByProjectAccessions(accessionsStrs);
            datasets.forEach(x -> {
                List<MongoPrideFile> files = mongoFiles.stream().filter(y -> y.getProjectAccessions().contains(x.getAccession().get(0).getValue())).collect(Collectors.toList());
                ((Dataset) x).setDataFiles(files.stream()
                        .map(file -> {
                            String value = "";
                            Optional<CvParam> cvParam = file.getPublicFileLocations()
                                    .stream()
                                    .filter(accTerm -> accTerm.getAccession().equalsIgnoreCase(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession()))
                                    .findFirst();
                            if (cvParam.isPresent())
                                value = cvParam.get().getValue();
                            return OntologyTerm.builder()
                                    .accession(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession())
                                    .name(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getName())
                                    .value(value)
                                    .build();
                        })
                        .collect(Collectors.toList())
                );
            });
        }

        return new HttpEntity<>(datasets);
    }

    @ApiOperation(notes = "Get dataset by Accession", value = "dataset", nickname = "getDataset", tags = {"datasets"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 400, message = "Bad request. The requested paramters are not supported.", response = APIError.class),
            @ApiResponse(code = 401, message = "Authorization information is missing or invalid.", response = APIError.class),
            @ApiResponse(code = 404, message = "No data found for the specified paramters.", response = APIError.class),
            @ApiResponse(code = 500, message = "Unexpected error", response = APIError.class)

    })
    @RequestMapping(value = "/datasets/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<IDataset> listDatasets(@PathVariable(name = "accession", required = true) String accession) {

        Optional<MongoPrideProject> mongoPrideProject = mongoProjectService.findByAccession(accession);
        IDataset dataset = null;
        if (mongoPrideProject.isPresent())
            dataset = new TransformerMongoProject(WsContastants.ResultType.full).apply(mongoPrideProject.get());

        if (dataset != null) {
            List<MongoPrideFile> mongoFiles = mongoFileService.findFilesByProjectAccession(dataset.getAccession().get(0).getValue());
            IDataset finalDataset = dataset;
            List<MongoPrideFile> files = mongoFiles.stream().filter(y -> y.getProjectAccessions().contains(finalDataset.getAccession().get(0).getValue())).collect(Collectors.toList());
            ((Dataset) dataset).setDataFiles(files.stream()
                    .map(file -> {
                        String value = "";
                        Optional<CvParam> cvParam = file.getPublicFileLocations()
                                .stream()
                                .filter(accTerm -> accTerm.getAccession().equalsIgnoreCase(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession()))
                                .findFirst();
                        if (cvParam.isPresent())
                            value = cvParam.get().getValue();
                        return OntologyTerm.builder()
                                .accession(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getAccession())
                                .name(CvTermReference.PRIDE_FTP_PROTOCOL_URL.getName())
                                .value(value)
                                .build();
                    })
                    .collect(Collectors.toList())
            );
        }

        return new HttpEntity<>(dataset);
    }

}
