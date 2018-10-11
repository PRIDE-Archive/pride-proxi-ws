package uk.ac.ebi.pride.ws.pride.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 25/05/2018.
 */
@Data
@XmlRootElement(name = "dataset")
@JsonRootName("dataset")
@JsonTypeName("dataset")
@Relation(collectionRelation = "datasets")
public class Dataset extends CompactDataset{

    @JsonProperty(PRIDE_PROJECT_SUMMARY)
    private String summary;

    @JsonProperty(PRIDE_PROJECT_PTMS)
    private Set<OntologyTerm> modifications = new HashSet<>();

    @JsonProperty(PRIDE_PROJECT_KEYWORDS)
    private Collection<OntologyTerm> keywords;

    @JsonProperty(PRIDE_PROJECT_DATASETLINK)
    private OntologyTerm datasetLink;

    @JsonProperty(PRIDE_DATA_FILE)
    private List<OntologyTerm> dataFiles = new ArrayList<>();

    /**
     * Default constructor
     */
    public Dataset() {
    }

    /**
     * Dataset constructor with all Dataset attributes
     * @param accession Accession
     * @param title Title
     * @param contacts Constants
     * @param instruments instruments
     * @param species species
     * @param publications publications
     * @param summary summary
     * @param modifications modifications
     * @param keywords keywords
     * @param datasetLink datasetLink
     * @param dataFiles dataFiles
     */
    public Dataset(String accession, String title, Collection<Contact> contacts, Collection<String> instruments, Collection<? extends OntologyTerm> species, Set<Publication> publications, String summary, Set<OntologyTerm> modifications, Collection<OntologyTerm> keywords, OntologyTerm datasetLink, List<OntologyTerm> dataFiles) {
        super(accession, title, contacts, instruments, species, publications);
        this.summary = summary;
        this.modifications = modifications;
        this.keywords = keywords;
        this.datasetLink = datasetLink;
        this.dataFiles = dataFiles;
    }
}
