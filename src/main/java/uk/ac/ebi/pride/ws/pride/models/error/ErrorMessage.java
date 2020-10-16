package uk.ac.ebi.pride.ws.pride.models.error;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorMessage {

    String detail;
    int status;
    String title;
    String type;
}
