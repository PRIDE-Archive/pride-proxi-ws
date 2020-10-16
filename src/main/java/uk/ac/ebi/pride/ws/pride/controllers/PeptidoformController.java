package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.ws.pride.models.error.APIError;
import uk.ac.ebi.pride.ws.pride.models.error.ErrorMessage;

@RestController
@Slf4j
public class PeptidoformController {

    @ApiOperation(notes = "The peptidoforms entry point returns global peptidoform statistics across an entire resource. Each peptidoform contains a summary of the statistics of the peptidoform across the entire resource.", value = "getPeptides", nickname = "getPeptides", tags = {"molecules"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 422, message = "UNPROCESSABLE_ENTITY"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptidoforms", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getPeptides() {
        return new ResponseEntity<>(ErrorMessage.builder()
                .detail("Although this is an officially defined PROXI endpoint, it has not yet been implemented at this server").status(HttpStatus.NOT_IMPLEMENTED.value()).title("Endpoint not implemented").type("NotImplemted").build(),HttpStatus.NOT_IMPLEMENTED);
    }

}
