package uk.ac.ebi.pride.ws.pride.models.molecules;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.models.ProteinIdentification;

import java.util.List;
import java.util.Set;

@Builder
@Data
public class Psm {
    private String datasetIdentifier; //project accession
    private String usi;
    private String peptideSequence;
    private List<Modification> ptms;
    private Set<OntologyTerm> searchEngineScore;
    private Double retentionTime;
    private Integer charge;
    private ProteinIdentification[] proteinAccessions;
}
