package uk.ac.ebi.pride.ws.pride.models.molecules;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;

@Builder
@Data
public class Modification {
    private String accession;
    private Integer position;
    private OntologyTerm[] scores;
}
