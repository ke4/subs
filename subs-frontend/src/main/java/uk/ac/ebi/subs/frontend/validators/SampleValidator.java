package uk.ac.ebi.subs.frontend.validators;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.repository.submittable.SampleRepository;

@Component
public class SampleValidator implements Validator {

    @Autowired
    private CoreSubmittableValidationHelper coreSubmittableValidationHelper;
    @Autowired
    private SampleRepository sampleRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return Sample.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Sample sample = (Sample) target;
        coreSubmittableValidationHelper.validate(sample, sampleRepository, errors);
    }
}
