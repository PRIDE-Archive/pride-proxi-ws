package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.spectra.model.ArchiveSpectrum;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.models.molecules.Modification;
import uk.ac.ebi.pride.ws.pride.models.molecules.Psm;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
public class TransformerPsm {

    private static final Set<String> ALLOW_SEARCH_ENGINES = new HashSet<>(Arrays.asList(
            CvTermReference.MS_PIA_PEPTIDE_FDR.getAccession(),
            CvTermReference.MS_PIA_PEPTIDE_QVALUE.getAccession(),
            CvTermReference.MS_PIA_PSM_LEVEL_FDRSCORE.getAccession(),
            CvTermReference.MS_PIA_PSM_LEVEL_QVALUE.getAccession(),
            CvTermReference.MS_XTANDEM_EXPECTANCY_SCORE.getAccession(),
            CvTermReference.MS_XTANDEM_HYPERSCORE.getAccession(),
            CvTermReference.MS_PEPTIDESHAKER_PSM_SCORE.getAccession(),
            CvTermReference.MS_PEPTIDESHAKER_PSM_CONFIDENCE.getAccession(),
            CvTermReference.MS_MSGF_EVALUE.getAccession(),
            CvTermReference.MS_MSGF_QVALUE.getAccession(),
            CvTermReference.MS_MSGF_DENOVOSCORE.getAccession(),
            CvTermReference.MS_MSGF_RAWSCORE.getAccession(),
            CvTermReference.MS_MSGF_SPECEVALUE.getAccession(),
            CvTermReference.MS_MSGF_PEPQVALUE.getAccession(),
            CvTermReference.MS_SEQUEST_DELTA_CN.getAccession(),
            CvTermReference.MS_SEQUEST_XCORR.getAccession()
    ));

    public static Psm apply(PrideMongoPsmSummaryEvidence psmMongo) {
        return Psm.builder()
                .usi(psmMongo.getUsi())
                .peptideSequence(psmMongo.getPeptideSequence())
                .build();
    }

    public static Psm apply(PrideMongoPsmSummaryEvidence psmMongo, PSMProvider readPSM) {
        ArchiveSpectrum spectrum = (ArchiveSpectrum) readPSM;

        Set<OntologyTerm> attributes = spectrum.getAttributes().stream()
                .map(x -> {
                   return OntologyTerm.builder()
                           .accession(((CvParamProvider) x).getAccession())
                           .name(((CvParamProvider) x).getName())
                           .value(((CvParamProvider) x).getValue())
                           .build();
                }).collect(Collectors.toSet());

        Set<OntologyTerm> searchEngineScores = attributes.stream().filter(x -> ALLOW_SEARCH_ENGINES.contains(x.getAccession())).collect(Collectors.toSet());
        List<Modification> mods = new ArrayList<>();
        if(readPSM.getModifications() != null && readPSM.getModifications().size() > 0 ){
            for(IdentifiedModificationProvider mod: readPSM.getModifications()){
                for(Tuple<Integer, Set<? extends CvParamProvider>> tuplePosition: mod.getPositionMap()){
                    List<OntologyTerm> scores = new ArrayList<>();
                    if(tuplePosition.getValue() != null){
                      scores = tuplePosition.getValue()
                              .stream()
                              .map( x->
                                      OntologyTerm.builder()
                                              .accession(((CvParamProvider) x).getAccession())
                                              .name(((CvParamProvider) x).getName())
                                              .value(((CvParamProvider) x).getValue())
                                              .build())
                              .collect(Collectors.toList());
                    }
                    Modification newMod = Modification.builder()
                            .accession(mod.getModificationCvTerm().getAccession())
                            .position(tuplePosition.getKey())
                            .scores(scores)
                            .build();

                    mods.add(newMod);
                }
            }
        }
        return Psm.builder()
                .datasetIdentifier(psmMongo.getProjectAccession())
                .usi(psmMongo.getUsi())
                .peptideSequence(psmMongo.getPeptideSequence())
                .ptms(mods)
                .searchEngineScore(searchEngineScores)
                .retentionTime(spectrum.getRetentionTime())
                .charge(psmMongo.getCharge())
//                .proteinAccessions() //TODO
                .build();
    }

}
