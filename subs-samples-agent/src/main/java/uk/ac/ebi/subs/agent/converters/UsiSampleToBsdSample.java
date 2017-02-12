package uk.ac.ebi.subs.agent.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.models.Attribute;
import uk.ac.ebi.biosamples.models.Sample;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
@ConfigurationProperties()
public class UsiSampleToBsdSample implements Converter<uk.ac.ebi.subs.data.submittable.Sample, Sample> {

    @Autowired
    UsiAttributeToBsdAttribute toBsdAttribute;
    @Autowired
    UsiRelationshipToBsdRelationship toBsdRelationship;

    private String ncbiBaseUrl = "http://purl.obolibrary.org/obo/NCBITaxon_";

    @Override
    public Sample convert(uk.ac.ebi.subs.data.submittable.Sample usiSample) {
        Set<Attribute> attributeSet;

        LocalDateTime release = null;
        LocalDateTime update = null;

        if(usiSample.getAttributes() != null) {
            for (uk.ac.ebi.subs.data.component.Attribute att : usiSample.getAttributes()) {
                if("release".equals(att.getName())) {
                    release = LocalDateTime.parse(att.getValue());
                }
                if("update".equals(att.getName())) {
                    update = LocalDateTime.parse(att.getValue());
                }
            }

            List<uk.ac.ebi.subs.data.component.Attribute> attributeList = new ArrayList<>(usiSample.getAttributes());
            attributeList.removeIf(attribute -> "release".equals(attribute.getName()) || "update".equals(attribute.getName()));
            attributeSet = toBsdAttribute.convert(attributeList);

        } else {
            attributeSet = new TreeSet<>();
        }

        if(usiSample.getTitle() != null) {
            Attribute att = Attribute.build("title", usiSample.getTitle());
            attributeSet.add(att);
        }
        if(usiSample.getTaxon() != null) {
            Attribute att = Attribute.build("taxon", usiSample.getTaxon(), ncbiBaseUrl + usiSample.getTaxonId(), null);
            attributeSet.add(att);
        }
        if(usiSample.getDescription() != null) {
            Attribute att = Attribute.build("description", usiSample.getDescription());
            attributeSet.add(att);
        }

        // Archive for samples is BioSamples

        Sample bioSample = Sample.build(
                usiSample.getAlias(),                                           // name
                usiSample.getAccession(),                                       // accession
                release,                                                        // release date
                update,                                                         // update date
                attributeSet,                                                   // attributes
                toBsdRelationship.convert(usiSample.getSampleRelationships())   // relationships
        );

        return bioSample;
    }

    public String getNcbiBaseUrl() {
        return ncbiBaseUrl;
    }

    public void setNcbiBaseUrl(String ncbiBaseUrl) {
        this.ncbiBaseUrl = ncbiBaseUrl;
    }
}
