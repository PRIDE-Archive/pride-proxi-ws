package uk.ac.ebi.pride.ws.pride.models;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "spectrum")
@JsonRootName("spectrum")
@JsonTypeName("spectrum")
@Relation(collectionRelation = "spectra")
@Builder
public class Spectrum implements ISpectrum{

    String usi;
    SpectrumStatus spectrumStatus;

    @Override
    public String getUsi() {
        return null;
    }

    @Override
    public SpectrumStatus getStatus() {
        return spectrumStatus;
    }
}
