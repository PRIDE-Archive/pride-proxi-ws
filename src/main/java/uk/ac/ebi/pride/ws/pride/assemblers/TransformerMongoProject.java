package uk.ac.ebi.pride.ws.pride.assemblers;

import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.ws.pride.models.Dataset;

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
public class TransformerMongoProject implements Function<MongoPrideProject, Dataset> {

    @Override
    public Dataset apply(MongoPrideProject mongoPrideProject) {
        Dataset dataset = new Dataset();
        dataset.setSummary(mongoPrideProject.getDescription());
        dataset.setAccession(mongoPrideProject.getAccession());
        dataset.setTitle(mongoPrideProject.getTitle());
        return  dataset;
    }
}
