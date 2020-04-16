package uk.ac.ebi.pride.ws.pride.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProteinIdentification {
    private String proteinAccession;
    private Integer startPosition;
    private Integer endPosition;
}
