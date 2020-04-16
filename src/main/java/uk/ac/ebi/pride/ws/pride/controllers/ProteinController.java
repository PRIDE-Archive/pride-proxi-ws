package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.ws.pride.models.molecules.Psm;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.Collection;

@RestController
@Slf4j
public class ProteinController {

    @ApiOperation(notes = "The protein entrey point returns protein statistics across an entire resource.", value = "getProteins", nickname = "getProteins", tags = {"molecules"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 422, message = "UNPROCESSABLE_ENTITY"),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteins", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Collection<? extends Psm>> getProteins() {
        return new ResponseEntity("Not implemented", HttpStatus.NOT_IMPLEMENTED);
    }
}
