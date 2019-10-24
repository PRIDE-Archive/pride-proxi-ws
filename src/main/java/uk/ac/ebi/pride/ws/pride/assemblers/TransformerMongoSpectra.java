package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.model.ArchiveSpectrum;
import uk.ac.ebi.pride.ws.pride.models.ISpectrum;
import uk.ac.ebi.pride.ws.pride.models.Spectrum;

import java.util.function.Function;

/**
 *
 */
@Slf4j
public class TransformerMongoSpectra implements Function<PSMProvider, ISpectrum> {

    @Override
    public ISpectrum apply(PSMProvider psmProvider) {
        ArchiveSpectrum spectrum = ((ArchiveSpectrum) psmProvider);
        Double[] mzs = spectrum.getMasses();
        Double[] intensities = spectrum.getIntensities();
        log.debug("Spectra retrirved -- " + spectrum.getUsi());

        return Spectrum.builder()
                .usi(spectrum.getUsi())
                .mzs(mzs)
                .intensities(intensities)
                .build();
    }
}
