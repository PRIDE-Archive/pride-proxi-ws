package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.model.ArchiveSpectrum;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.models.molecules.Psm;

import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class TransformerPsm {

    public static Psm apply(PrideMongoPsmSummaryEvidence psmMongo) {
        return Psm.builder()
                .usi(psmMongo.getUsi())
                .peptideSequence(psmMongo.getPeptideSequence())
                .build();
    }

    public static Psm apply(PrideMongoPsmSummaryEvidence psmMongo, PSMProvider readPSM) {
        ArchiveSpectrum spectrum = (ArchiveSpectrum) readPSM;

        Set<OntologyTerm> attributes = spectrum.getAttributes().stream()
                .map(x -> OntologyTerm.builder()
                        .accession(x.getAccession())
                        .name(x.getName())
                        .value(x.getValue())
                        .build()).collect(Collectors.toSet());

        return Psm.builder()
                .datasetIdentifier(psmMongo.getProjectAccession())
                .usi(psmMongo.getUsi())
                .peptideSequence(psmMongo.getPeptideSequence())
//                .ptms() //TODO
                .searchEngineScore(attributes)//TODO remove the ones that are not 'search engine scores'
                .retentionTime(spectrum.getRetentionTime())
                .charge(psmMongo.getCharge())
//                .proteinAccessions() //TODO
                .build();
    }

}
