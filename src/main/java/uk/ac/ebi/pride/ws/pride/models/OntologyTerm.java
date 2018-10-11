package uk.ac.ebi.pride.ws.pride.models;

import lombok.Builder;
import lombok.Data;

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
@Data
@Builder
public class OntologyTerm {

    String accession;
    String name;
    String value;
    String cvLabel;
}
