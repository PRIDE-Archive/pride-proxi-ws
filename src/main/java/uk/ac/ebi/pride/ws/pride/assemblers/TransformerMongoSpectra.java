package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.model.ArchiveSpectrum;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.ws.pride.models.ISpectrum;
import uk.ac.ebi.pride.ws.pride.models.Spectrum;
import uk.ac.ebi.pride.ws.pride.models.SpectrumStatus;

/**
 *
 */
@Slf4j
public class TransformerMongoSpectra {

    private SpectrumStatus spectrumStatus;

    public TransformerMongoSpectra(SpectrumStatus spectrumStatus) {
        this.spectrumStatus = spectrumStatus;
    }

    public ISpectrum apply(PrideMongoPsmSummaryEvidence psmProvider) {
        return Spectrum.builder()
                .usi(psmProvider.getUsi())
                .spectrumStatus(spectrumStatus)
                .build();
    }

    public ISpectrum apply(PSMProvider readPSM) {
        ArchiveSpectrum spectrum = (ArchiveSpectrum) readPSM;
        return Spectrum.builder()
                .spectrumStatus(spectrumStatus)
                .usi(spectrum.getUsi())
                .intensities(spectrum.getIntensities())
                .mzs(spectrum.getMasses())
                .build();
    }
}
