package uk.ac.ebi.subs.frontend.validators;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.ac.ebi.subs.data.submittable.SampleGroup;
import uk.ac.ebi.subs.repository.repos.SampleGroupRepository;

@Component
public class SampleGroupValidator implements Validator {

    @Autowired
    private CoreSubmittableValidationHelper coreSubmittableValidationHelper;
    @Autowired
    private SampleGroupRepository repository;


    @Override
    public boolean supports(Class<?> clazz) {
        return SampleGroup.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {
        SampleGroup target = (SampleGroup) object;
        coreSubmittableValidationHelper.validate(target, repository, errors);
    }
}