package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.Psm;

/**
 *
 */
@Slf4j
public class TransformerMongoPsm {

    public static Psm apply(PrideMongoPsmSummaryEvidence psmMongo) {
        return Psm.builder()
                .usi(psmMongo.getUsi())
                .peptideSequence(psmMongo.getPeptideSequence())
                .build();
    }

//    public ISpectrum apply(PSMProvider readPSM) {
//        ArchiveSpectrum spectrum = (ArchiveSpectrum) readPSM;
//        return Spectrum.builder()
//                .spectrumStatus(spectrumStatus)
//                .usi(spectrum.getUsi())
//                .intensities(spectrum.getIntensities())
//                .mzs(spectrum.getMasses())
//                .build();
//    }
}
