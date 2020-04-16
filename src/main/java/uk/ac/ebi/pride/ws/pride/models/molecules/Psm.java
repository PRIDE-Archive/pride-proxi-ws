package uk.ac.ebi.pride.ws.pride.models.molecules;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.models.ProteinIdentification;

@Builder
@Data
public class Psm {
    private String accession;
    private String usi;
    private String peptideSequence;
    private Modification[] ptms;
    private OntologyTerm[] searchEngineScore;
    private Double retentionTime;
    private Integer charge;
    private ProteinIdentification[] proteinAccessions;
}
