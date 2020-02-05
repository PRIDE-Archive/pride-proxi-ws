package uk.ac.ebi.pride.ws.pride.assemblers;

import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.reference.ReferenceProvider;
import uk.ac.ebi.pride.archive.dataprovider.user.ContactProvider;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.models.*;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 10/10/2018.
 */
public class TransformerMongoProject implements Function<MongoPrideProject, IDataset> {

    private WsContastants.ResultType resultType;

    public TransformerMongoProject(WsContastants.ResultType resultType) {
        this.resultType = resultType;
    }

    /**
     * Apply, Transform the
     *
     * @param mongoPrideProject
     * @return
     */
    @Override
    public IDataset apply(MongoPrideProject mongoPrideProject) {
        IDataset dataset;
        if (resultType == WsContastants.ResultType.full)
            dataset = transformFull(mongoPrideProject);
        else
            dataset = transformCompact(mongoPrideProject);
        return dataset;
    }

    /**
     * The {@link uk.ac.ebi.pride.ws.pride.utils.WsContastants.ResultType} Compact Dataset
     *
     * @param mongoPrideProject mongo pride project
     * @return IDataset
     */
    private IDataset transformCompact(MongoPrideProject mongoPrideProject) {
        CompactDataset dataset = new CompactDataset();
        dataset.setAccession(createAccession(mongoPrideProject.getAccession()));
        dataset.setTitle(mongoPrideProject.getTitle());
        dataset.setSpecies(transformSpecies(mongoPrideProject.getSamplesDescription()));
        dataset.setInstruments(transformInstruments(mongoPrideProject.getInstrumentsCvParams()));
        dataset.setContacts(transformContacts(mongoPrideProject.getSubmittersContacts(), mongoPrideProject.getLabHeadContacts()));
        dataset.setPublications(transformPublications(mongoPrideProject.getCompleteReferences()));
        return dataset;
    }

    /**
     * The {@link uk.ac.ebi.pride.ws.pride.utils.WsContastants.ResultType} Full Dataset
     *
     * @param mongoPrideProject mongo pride project
     * @return IDataset
     */
    private IDataset transformFull(MongoPrideProject mongoPrideProject) {
        Dataset dataset = new Dataset();
        dataset.setSummary(mongoPrideProject.getDescription());
        dataset.setTitle(mongoPrideProject.getTitle());
        dataset.setAccession(createAccession(mongoPrideProject.getAccession()));
        dataset.setKeywords(transformKeywords(mongoPrideProject.getKeywords(), mongoPrideProject.getProjectTags()));
        dataset.setModifications(transformModifications(mongoPrideProject.getPtmList()));
        dataset.setDatasetLink(transformDatasetLink(mongoPrideProject.getAccession()));
        return dataset;

    }

    private List<OntologyTerm> createAccession(String accession) {
        OntologyTerm ontologyTerm = OntologyTerm.builder()
                .name("ProteomeXchange accession number")
                .accession("MS:1001919")
                .value(accession)
                .build();
        ArrayList<OntologyTerm> ontologyTerms = new ArrayList<>();
        ontologyTerms.add(ontologyTerm);
        return ontologyTerms;
    }

    private Collection<? extends OntologyTerm> transformSpecies(List<Tuple<CvParam, Set<CvParam>>> samplesDescription) {
        List<OntologyTerm> species = new ArrayList<>();
        samplesDescription.forEach(x -> {
            if (x.getKey().getAccession().equals(CvTermReference.EFO_ORGANISM.getAccession())) {
                species.addAll(x.getValue().stream().map(y -> OntologyTerm.builder()
                        .accession(y.getAccession())
                        .name(y.getName())
                        .value(y.getValue())
                        .build()).collect(Collectors.toList()));
            }
        });
        return species;
    }

    private Set<Publication> transformPublications(Collection<? extends ReferenceProvider> completeReferences) {
        return completeReferences
                .stream()
                .map(x -> Publication
                        .builder()
                        .publicationProperties(transformPublicationProperties(x))
                        .build()
                )
                .collect(Collectors.toSet());
    }

    private Collection<Contact> transformContacts(Collection<? extends ContactProvider> submitters, Collection<? extends ContactProvider> labHeads) {
        Collection<Contact> contacts = new ArrayList<>();

        addContact(submitters, contacts, CvTermReference.CONTACT_ROLE_SUBMITTER);
        addContact(labHeads, contacts, CvTermReference.CONTACT_ROLE_LAB_HEAD);
        return contacts;
    }

    private void addContact(Collection<? extends ContactProvider> submitters, Collection<Contact> contacts, CvTermReference contactRole) {
        List<OntologyTerm> ontologyTerms = new ArrayList<>();

        submitters.forEach(c -> {
            ontologyTerms.add(OntologyTerm.builder()
                    .accession(contactRole.getAccession())
                    .name(contactRole.getName())
                    .build());

            ontologyTerms.add(OntologyTerm.builder()
                    .accession(CvTermReference.CONTACT_NAME.getAccession())
                    .name(CvTermReference.CONTACT_NAME.getName())
                    .value(String.valueOf(c.getName()))
                    .build());

            ontologyTerms.add(OntologyTerm.builder()
                    .accession(CvTermReference.CONTACT_EMAIL.getAccession())
                    .name(CvTermReference.CONTACT_EMAIL.getName())
                    .value(String.valueOf(c.getEmail()))
                    .build());

            ontologyTerms.add(OntologyTerm.builder()
                    .accession(CvTermReference.CONTACT_ORG.getAccession())
                    .name(CvTermReference.CONTACT_ORG.getName())
                    .value(String.valueOf(c.getAffiliation()))
                    .build());

            Contact contact = Contact.builder().contactProperties(ontologyTerms).build();
            contacts.add(contact);
        });
    }

    private List<OntologyTerm> transformPublicationProperties(ReferenceProvider x) {
        List<OntologyTerm> terms = new ArrayList<>();
        if (x != null) {
            if (x.getPubmedId() != -1)
                terms.add(OntologyTerm.builder()
                        .accession(CvTermReference.MS_PUBLICATION_PUBMED_IDENTIFIER.getAccession())
                        .name(CvTermReference.MS_PUBLICATION_PUBMED_IDENTIFIER.getName())
                        .value(String.valueOf(x.getPubmedId()))
                        .build());
            if (x.getReferenceLine() != null && x.getReferenceLine().isEmpty())
                terms.add(OntologyTerm.builder()
                        .accession(CvTermReference.MS_PUBLICATION_REFERENCE.getAccession())
                        .name(CvTermReference.MS_PUBLICATION_REFERENCE.getName())
                        .value(String.valueOf(x.getReferenceLine()))
                        .build());
            if (x.getDoi() != null && x.getDoi().isEmpty())
                terms.add(OntologyTerm.builder()
                        .accession(CvTermReference.MS_PUBLICATION_DOI.getAccession())
                        .name(CvTermReference.MS_PUBLICATION_DOI.getName())
                        .value(String.valueOf(x.getReferenceLine()))
                        .build());

        }
        return terms;
    }

    private Collection<OntologyTerm> transformInstruments(Collection<? extends CvParamProvider> instrumentsCvParams) {
        return instrumentsCvParams.stream()
                .map(x -> OntologyTerm.builder()
                        .accession(x.getAccession())
                        .name(x.getName())
                        .value(x.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private OntologyTerm transformDatasetLink(String accession) {
        return OntologyTerm.builder()
                .accession(CvTermReference.MS_DATASET_LINK_HTTP.getAccession())
                .name(CvTermReference.MS_DATASET_LINK_HTTP.getName())
                .value(WsContastants.DATASET_LINK_HTTP + accession)
                .build();
    }

    private Set<OntologyTerm> transformModifications(Collection<CvParam> ptmList) {
        return ptmList.stream().map(x -> OntologyTerm.builder()
                .accession(x.getAccession())
                .name(x.getName())
                .value(x.getValue()).build()).collect(Collectors.toSet());
    }

    /**
     * Transform list keywords to {@link OntologyTerm} list
     *
     * @param keywords    keywords
     * @param projectTags tags
     * @return List of {@link OntologyTerm}
     */
    private Collection<OntologyTerm> transformKeywords(Collection<String> keywords, Collection<String> projectTags) {
        List<OntologyTerm> terms = new ArrayList<>();
        String tag = "";
        String keyword = "";
        if (!keywords.isEmpty()) {
            keyword = String.join(", ", keywords);
        }
        if (!projectTags.isEmpty()) {
            tag = String.join(", ", projectTags);
        }

        if (!keyword.isEmpty()) {
            OntologyTerm keywordTerm = OntologyTerm.builder()
                    .accession(CvTermReference.MS_KEYWORD_SUBMITTER.getAccession())
                    .name(CvTermReference.MS_KEYWORD_SUBMITTER.getName())
                    .value(keyword)
                    .build();
            terms.add(keywordTerm);
        }

        if (!tag.isEmpty()) {
            OntologyTerm tagTerm = OntologyTerm.builder()
                    .accession(CvTermReference.MS_KEYWORD_CURATOR.getAccession())
                    .name(CvTermReference.MS_KEYWORD_CURATOR.getName())
                    .value(tag)
                    .build();
            terms.add(tagTerm);
        }

        return terms;
    }


}
