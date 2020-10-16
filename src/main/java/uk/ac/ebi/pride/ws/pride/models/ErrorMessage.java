package uk.ac.ebi.pride.ws.pride.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class ErrorMessage {

    String detail;
    int status;
    String title;
    String type;
}
