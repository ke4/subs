package uk.ac.ebi.subs.util;


import uk.ac.ebi.subs.data.FullSubmission;
import uk.ac.ebi.subs.data.component.Team;
import uk.ac.ebi.subs.data.submittable.Sample;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of helper methods to use in tests.
 */
public class Helpers {

    public static Sample generateTestSample() {
        Sample sample = new Sample();
        sample.setAccession("S0");
        sample.setDescription("Test sample 0.");

        return sample;
    }

    public static List<Sample> generateTestSamples() {
        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setAccession("S1");
        sample1.setDescription("Test sample 1.");
        samples.add(sample1);

        Sample sample2 = new Sample();
        sample2.setAccession("S2");
        sample2.setDescription("Test sample 2.");
        samples.add(sample2);

        Sample sample3 = new Sample();
        sample3.setAccession("S3");
        samples.add(sample3);
        sample3.setDescription("Test sample 3.");

        Team d = new Team();
        d.setName("sampleTestTeam");

        samples.forEach(s -> s.setTeam(d));

        return samples;
    }

    public static FullSubmission generateTestSubmission() {
        FullSubmission sub = new FullSubmission();
        sub.setSamples(generateTestSamples());
        sub.getSubmitter().setEmail("test@ebi.ac.uk");
        sub.getTeam().setName("test-subs");
        return sub;
    }

}
