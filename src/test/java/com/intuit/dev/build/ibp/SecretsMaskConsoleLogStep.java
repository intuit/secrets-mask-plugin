package com.intuit.dev.build.ibp;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SecretsMaskConsoleLogStep {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    final String secret = "1234";

    @Before
    public void setup() {
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        config.addSecretPattern("secret", secret);
    }

    @After
    public void teardown() {
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        config.clearSecretPatterns();
    }

    @Test
     public void testSecretMaskedStep () throws Exception {
        WorkflowJob project = r.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("echo \"this is a secret password: 1234\"", true));

        WorkflowRun build = r.buildAndAssertSuccess(project);
        r.assertLogNotContains(secret, build);
    }

}
