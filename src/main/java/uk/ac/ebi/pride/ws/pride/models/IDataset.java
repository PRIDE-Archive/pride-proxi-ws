package uk.ac.ebi.pride.ws.pride.models;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 11/10/2018.
 */
public interface IDataset {

    /**
     * Get Accession of the Dataset
     *
     * @return Dataset Accession
     */
    List<OntologyTerm> getIdentifiers();

    /**
     * Get contacts related with the dataset
     *
     * @return List of Datasets
     */
    Collection<Contact> getContacts();

    /**
     * Get instruments related to dataset
     *
     * @return List Instruments
     */
    Collection<? extends OntologyTerm> getInstruments();

    /**
     * Get the Species
     *
     * @return List of Species
     */
    Collection<? extends OntologyTerm> getSpecies();

    /**
     * Get the Publications
     *
     * @return List of publications
     */
    Set<Publication> getPublications();


}
