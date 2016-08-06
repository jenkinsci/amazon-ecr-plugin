/*
 * The MIT License
 *
 *  Copyright (c) 2015, CloudBees, Inc.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 */

package com.cloudbees.jenkins.plugins.amazonecr;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ecr.AmazonECRClient;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.List;

/**
 * This new kind of credential provides an embedded {@link com.amazonaws.auth.AWSCredentials}
 * when a credential for Amazon ECS Registry end point is needed.
 */
public class AmazonECSRegistryCredential extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {

    private final String credentialsId;

    private final Regions region;

    public AmazonECSRegistryCredential(@CheckForNull CredentialsScope scope, String credentialsId) {
        super(scope, "ecr:"+credentialsId, "Amazon ECR Registry");
        this.credentialsId = credentialsId;
        region = Regions.US_EAST_1;
    }

    public AmazonECSRegistryCredential(@CheckForNull CredentialsScope scope, String credentialsId, Regions region) {
        super(scope, "ecr:" + region.getName() + ":" + credentialsId, "Amazon ECR Registry");
        this.credentialsId = credentialsId;
        this.region = region;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public AmazonWebServicesCredentials getCredentials() {
        List<AmazonWebServicesCredentials> credentials = CredentialsProvider.lookupCredentials(
            AmazonWebServicesCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections.EMPTY_LIST);

        if (credentials.isEmpty()) {
            return null;
        }

        for (AmazonWebServicesCredentials awsCredentials : credentials) {
            if (awsCredentials.getId().equals(this.credentialsId)) {
                return awsCredentials;
            }
        }
        return  null;
    }

    public String getDescription() {
        return CredentialsNameProvider.name(getCredentials());
    }

    @NonNull
    @Override
    public Secret getPassword() {
        final AmazonWebServicesCredentials credentials = getCredentials();
        if (credentials == null) throw new IllegalStateException("Invalid credentials");

        final AmazonECRClient client = new AmazonECRClient(credentials.getCredentials(), new ClientConfiguration());
        client.setRegion(Region.getRegion(region));

        final GetAuthorizationTokenResult authorizationToken = client.getAuthorizationToken(new GetAuthorizationTokenRequest());
        final List<AuthorizationData> authorizationData = authorizationToken.getAuthorizationData();
        if (authorizationData == null || authorizationData.isEmpty()) {
            throw new IllegalStateException("Failed to retreive authorization token for Amazon ECR");
        }
        return Secret.fromString(authorizationData.get(0).getAuthorizationToken());
    }

    @NonNull
    @Override
    public String getUsername() {
        return "AWS";
    }

    public String getEmail() {
        return "nobody@example.com";
    }
}
