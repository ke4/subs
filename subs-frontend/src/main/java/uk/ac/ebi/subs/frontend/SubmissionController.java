package uk.ac.ebi.subs.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.subs.data.Submission;
import uk.ac.ebi.subs.repository.SubmissionRepository;

import static uk.ac.ebi.subs.frontend.SubsPostHelper.postCreatedResponse;

@RestController
@BasePathAwareController
@RequestMapping("/domains/{domainName}/submissions")
public class SubmissionController {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);


    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    ResourceAssembler<Submission, Resource<Submission>> submissionResourceAssembler;

    @Autowired
    PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<Resource<Submission>> getSome(@PathVariable String domainName, Pageable pageable){
        Page<Submission> submissions = submissionRepository.findByDomainName(domainName,pageable);
        return pagedResourcesAssembler.toResource(submissions,submissionResourceAssembler);

    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> postOneNew(@PathVariable String domainName, @Validated @RequestBody Submission submission){
        submissionRepository.insert(submission);

        Resource<Submission> submissionResource= submissionResourceAssembler.toResource(submission);

        return postCreatedResponse(submissionResource);
    }

    @RequestMapping("/{submissionId}")
    public Resource<Submission> getOne(@PathVariable String domainName, @PathVariable String submissionId){
        return submissionResourceAssembler.toResource(submissionRepository.findOneByIdAndDomainName(submissionId,domainName));
    }

    @RequestMapping(path="/{submissionId}",method=RequestMethod.PUT)
    public Resource<Submission> putOne(@PathVariable String domainName, @PathVariable String submissionId, @Validated @RequestBody Submission submission){
        return submissionResourceAssembler.toResource(submissionRepository.findOneByIdAndDomainName(submissionId,domainName));
    }

}