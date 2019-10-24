package uk.ac.ebi.pride.ws.pride.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

/**
 * @author ypriverol
 */

@Data
@XmlRootElement(name = "dataset")
@JsonRootName("dataset")
@JsonTypeName("dataset")
@Relation(collectionRelation = "datasets")
public class CompactDataset implements Serializable, PrideArchiveAPIField, IDataset {

    @XmlElement
    @JsonProperty(PRIDE_PROJECT_ACCESSION)
    private List<OntologyTerm> accession;

    @JsonProperty(PRIDE_PROJECT_TITLE)
    private String title;

    @JsonProperty(PRIDE_PROJECT_CONTACTS)
    private Collection<Contact> contacts = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_INSTRUMENTS)
    private Collection<OntologyTerm> instruments = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_SPECIES)
    private Collection<? extends OntologyTerm> species = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_PUBLICATIONS)
    private Set<Publication> publications = new HashSet<>();

    public CompactDataset() {
    }

    public CompactDataset(List<OntologyTerm> accession, String title, Collection<Contact> contacts, Collection<OntologyTerm> instruments, Collection<? extends OntologyTerm> species, Set<Publication> publications) {
        this.accession = accession;
        this.title = title;
        this.contacts = contacts;
        this.instruments = instruments;
        this.species = species;
        this.publications = publications;
    }
}
