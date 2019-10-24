package uk.ac.ebi.pride.ws.pride.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "spectrum")
@JsonRootName("spectrum")
@JsonTypeName("spectrum")
@Relation(collectionRelation = "spectra")
@Builder
public class Spectrum implements ISpectrum{

    @JsonProperty("usi")
    String usi;

    @JsonProperty("status")
    SpectrumStatus spectrumStatus;

    @JsonProperty("mzs")
    Double[] mzs;

    @JsonProperty("intensities")
    Double[] intensities;


    @Override
    public String getUsi() {
        return usi;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public SpectrumStatus getStatus() {
        return spectrumStatus;
    }
}
