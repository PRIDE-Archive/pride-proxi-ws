package uk.ac.ebi.pride.ws.pride.assemblers;

import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.models.CompactDataset;
import uk.ac.ebi.pride.ws.pride.models.Dataset;
import uk.ac.ebi.pride.ws.pride.models.IDataset;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
        return dataset;

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
