package uk.ac.ebi.pride.ws.pride.models.molecules;

public interface ISpectrum {

    /**
     * The usi of the spectrum
     * @return
     */
    String getUsi();

    /**
     * Get the status of the spectrum {@link SpectrumStatus}
     * @return
     */
    SpectrumStatus getStatus();

}
