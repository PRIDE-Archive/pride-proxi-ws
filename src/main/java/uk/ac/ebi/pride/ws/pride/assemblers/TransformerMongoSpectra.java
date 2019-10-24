package uk.ac.ebi.pride.ws.pride.assemblers;

import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.ws.pride.models.ISpectrum;
import uk.ac.ebi.pride.ws.pride.models.Spectrum;

import java.util.function.Function;

/**
 *
 */
public class TransformerMongoSpectra implements Function<PSMProvider, ISpectrum> {

    @Override
    public ISpectrum apply(PSMProvider psmProvider) {
        return Spectrum.builder()
                .usi(psmProvider.getUsi())
                .build();
    }
}
