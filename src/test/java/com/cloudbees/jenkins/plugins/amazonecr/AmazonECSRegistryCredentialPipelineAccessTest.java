package com.cloudbees.jenkins.plugins.amazonecr;

import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class AmazonECSRegistryCredentialPipelineAccessTest {
    @Test
    @EnabledIfEnvironmentVariable(named = "AWS_ACCESS_KEY_ID", matches = ".{10,}")
    void pipelineCanLoginWithCredential(JenkinsRule r) throws Exception {
        SystemCredentialsProvider.getInstance()
                .getCredentials()
                .add(new AWSCredentialsImpl(
                        CredentialsScope.GLOBAL,
                        "test",
                        System.getenv("AWS_ACCESS_KEY_ID"),
                        System.getenv("AWS_SECRET_ACCESS_KEY"),
                        "test"));

        String script =
                "docker.withRegistry('https://" + System.getenv("AWS_REGISTRY_HOST") + "', 'ecr:us-east-1:test') {}";

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testJob");
        p.setDefinition(new CpsFlowDefinition(script, true));

        r.assertBuildStatusSuccess(p.scheduleBuild2(0));
    }
}
