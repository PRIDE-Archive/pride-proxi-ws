package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.model.ArchiveSpectrum;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.ws.pride.models.OntologyTerm;
import uk.ac.ebi.pride.ws.pride.models.molecules.ISpectrum;
import uk.ac.ebi.pride.ws.pride.models.molecules.Spectrum;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumStatus;

import java.util.ArrayList;
import java.util.List;

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
        List<OntologyTerm> attributes = new ArrayList<>();
        if(spectrum != null){
            if (spectrum.getPrecursorCharge() != null)
                attributes.add(OntologyTerm.builder().accession("MS:1000041").name("charge state").value(String.valueOf(spectrum.getPrecursorCharge())).build());
            if (spectrum.getPeptideSequence() != null)
                attributes.add(OntologyTerm.builder().accession("MS:1000888").name("unmodified peptide sequence").value(spectrum.getPeptideSequence()).build());
        }
        return Spectrum.builder()
                .spectrumStatus(spectrumStatus)
                .usi(spectrum.getUsi())
                .intensities(spectrum.getIntensities())
                .mzs(spectrum.getMasses())
                .attributes(attributes)
                .build();
    }
}
