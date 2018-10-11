package uk.ac.ebi.pride.ws.pride.assemblers;

import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.reference.ReferenceProvider;
import uk.ac.ebi.pride.archive.dataprovider.utils.Tuple;
import uk.ac.ebi.pride.mongodb.archive.model.param.MongoCvParam;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.models.*;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
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
     * @param mongoPrideProject
     * @return
     */
    @Override
    public IDataset apply(MongoPrideProject mongoPrideProject) {
        IDataset dataset;
        if(resultType == WsContastants.ResultType.Full)
            dataset = transformFull(mongoPrideProject);
        else
            dataset = transformCompact(mongoPrideProject);
        return  dataset;
    }

    /**
     * The {@link uk.ac.ebi.pride.ws.pride.utils.WsContastants.ResultType} Compact Dataset
     * @param mongoPrideProject mongo pride project
     * @return IDataset
     */
    private IDataset transformCompact(MongoPrideProject mongoPrideProject){
        CompactDataset dataset = new CompactDataset();
        dataset.setAccession(mongoPrideProject.getAccession());
        dataset.setTitle(mongoPrideProject.getTitle());
        return dataset;
    }

    /**
     * The {@link uk.ac.ebi.pride.ws.pride.utils.WsContastants.ResultType} Full Dataset
     * @param mongoPrideProject mongo pride project
     * @return IDataset
     */
    private IDataset transformFull(MongoPrideProject mongoPrideProject){
        Dataset dataset = new Dataset();
        dataset.setSummary(mongoPrideProject.getDescription());
        dataset.setTitle(mongoPrideProject.getTitle());
        dataset.setAccession(mongoPrideProject.getAccession());
        dataset.setKeywords(transformKeywords(mongoPrideProject.getKeywords(), mongoPrideProject.getProjectTags()));
        dataset.setModifications(transformModifications(mongoPrideProject.getPtmList()));
        dataset.setDatasetLink(transformDatasetLink(mongoPrideProject.getAccession()));
        dataset.setInstruments(transformInstruments(mongoPrideProject.getInstrumentsCvParams()));
        dataset.setPublications(transformPublications(mongoPrideProject.getCompleteReferences()));
        dataset.setSpecies(transformSpecies(mongoPrideProject.getSamplesDescription()));
        return dataset;

    }

    private Collection<? extends OntologyTerm> transformSpecies(List<Tuple<MongoCvParam, List<MongoCvParam>>> samplesDescription) {
        List<OntologyTerm> species = new ArrayList<>();
        samplesDescription.stream().forEach(x -> {
            if(x.getKey().getAccession() == CvTermReference.EFO_ORGANISM.getAccession()){
                species.addAll(x.getValue().stream().map(y -> OntologyTerm.builder()
                        .accession(y.getAccession())
                        .name(y.getName())
                        .cvLabel(y.getCvLabel())
                        .value(y.getValue())
                        .build()).collect(Collectors.toList()));
            }
        });
        return null;
    }

    private Set<Publication> transformPublications(Collection<? extends ReferenceProvider> completeReferences) {
        return completeReferences
                .stream()
                .map( x -> Publication
                        .builder()
                        .publicationProperties(transformPublicationProperties(x))
                        .build()
                )
                .collect(Collectors.toSet());
    }

    private List<OntologyTerm> transformPublicationProperties(ReferenceProvider x) {
        List<OntologyTerm> terms = new ArrayList<>();
        if(x != null){
            if(x.getPubmedId() != -1)
                terms.add(OntologyTerm.builder()
                        .accession(CvTermReference.MS_PUBLICATION_PUBMED_IDENTIFIER.getAccession())
                        .name(CvTermReference.MS_PUBLICATION_PUBMED_IDENTIFIER.getName())
                        .cvLabel(CvTermReference.MS_PUBLICATION_PUBMED_IDENTIFIER.getCvLabel())
                        .value(String.valueOf(x.getPubmedId()))
                        .build());
            if(x.getReferenceLine() != null && x.getReferenceLine().isEmpty())
                terms.add(OntologyTerm.builder()
                        .accession(CvTermReference.MS_PUBLICATION_REFERENCE.getAccession())
                        .name(CvTermReference.MS_PUBLICATION_REFERENCE.getName())
                        .cvLabel(CvTermReference.MS_PUBLICATION_REFERENCE.getCvLabel())
                        .value(String.valueOf(x.getReferenceLine()))
                        .build());
            if(x.getDoi() != null && x.getDoi().isEmpty())
                terms.add(OntologyTerm.builder()
                        .accession(CvTermReference.MS_PUBLICATION_DOI.getAccession())
                        .name(CvTermReference.MS_PUBLICATION_DOI.getName())
                        .cvLabel(CvTermReference.MS_PUBLICATION_DOI.getCvLabel())
                        .value(String.valueOf(x.getReferenceLine()))
                        .build());

        }
        return terms;
    }

    private Collection<OntologyTerm> transformInstruments(Collection<? extends CvParamProvider> instrumentsCvParams) {
        return instrumentsCvParams.stream()
                .map( x-> OntologyTerm.builder()
                        .accession(x.getAccession())
                        .name(x.getName())
                        .cvLabel(x.getCvLabel())
                        .value(x.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private OntologyTerm transformDatasetLink(String accession) {
        return OntologyTerm.builder()
                .accession(CvTermReference.MS_DATASET_LINK_HTTP.getAccession())
                .name(CvTermReference.MS_DATASET_LINK_HTTP.getName()).cvLabel(CvTermReference.MS_DATASET_LINK_HTTP.getCvLabel())
                .value(WsContastants.DATASET_LINK_HTTP + accession)
                .build();
    }

    private Set<OntologyTerm> transformModifications(Collection<MongoCvParam> ptmList) {
        return ptmList.stream().map(x -> OntologyTerm.builder()
                .accession(x.getAccession())
                .name(x.getName())
                .cvLabel(x.getCvLabel())
                .value(x.getValue()).build()).collect(Collectors.toSet());
    }

    /**
     * Transform list keywords to {@link OntologyTerm} list
     * @param keywords keywords
     * @param projectTags tags
     * @return List of {@link OntologyTerm}
     */
    private Collection<OntologyTerm> transformKeywords(Collection<String> keywords, Collection<String> projectTags) {
        List<OntologyTerm> terms = new ArrayList<>();
        StringBuilder keyword = new StringBuilder();
        if(!keywords.isEmpty())
            for(String value: keywords)
                keyword.append(value).append(", ");
        StringBuilder tag = new StringBuilder();
        if(!projectTags.isEmpty())
            for(String value: projectTags)
                tag.append(value).append(", ");

        String keywordString = keyword.toString().trim();
        String tagString = tag.toString().trim();

        if(keywordString.isEmpty()){
            OntologyTerm keywordTerm = OntologyTerm.builder()
                    .accession(CvTermReference.MS_KEYWORD_SUBMITTER.getAccession())
                    .name(CvTermReference.MS_KEYWORD_SUBMITTER.getName())
                    .cvLabel(CvTermReference.MS_KEYWORD_SUBMITTER.getCvLabel())
                    .value(keywordString)
                    .build();
            terms.add(keywordTerm);
        }

        if(tagString.isEmpty()){
            OntologyTerm tagTerm = OntologyTerm.builder()
                    .accession(CvTermReference.MS_KEYWORD_CURATOR.getAccession())
                    .name(CvTermReference.MS_KEYWORD_CURATOR.getName())
                    .cvLabel(CvTermReference.MS_KEYWORD_CURATOR.getCvLabel())
                    .value(tagString)
                    .build();
            terms.add(tagTerm);
        }

        return terms;
    }


}
