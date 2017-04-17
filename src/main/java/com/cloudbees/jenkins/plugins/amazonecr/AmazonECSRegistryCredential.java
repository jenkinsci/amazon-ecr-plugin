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
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This new kind of credential provides an embedded {@link com.amazonaws.auth.AWSCredentials}
 * when a credential for Amazon ECS Registry end point is needed.
 */
public class AmazonECSRegistryCredential extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {
    private static final Logger LOG = Logger.getLogger(AmazonECSRegistryCredential.class.getName());

    private final String credentialsId;

    private final Regions region;

    private final ItemGroup itemGroup;

    public AmazonECSRegistryCredential(CredentialsScope scope, @Nonnull String credentialsId,
                                       String description, ItemGroup itemGroup) {
        this(scope, credentialsId, Regions.US_EAST_1, description, (ItemGroup<?>)itemGroup);
    }

    public AmazonECSRegistryCredential(@CheckForNull CredentialsScope scope, @Nonnull String credentialsId,
                                       Regions region, String description, ItemGroup itemGroup) {
        super(scope, "ecr:" + region.getName() + ":" + credentialsId, "Amazon ECR Registry : "
                + (StringUtils.isNotBlank(description) ? description + " - " : "" ) + region);
        this.credentialsId = credentialsId;
        this.region = region;
        this.itemGroup = itemGroup;
    }


    @Nonnull
    public String getCredentialsId() {
        return credentialsId;
    }

    public @CheckForNull AmazonWebServicesCredentials getCredentials() {
        LOG.fine("Looking for Amazon web credentials");
        List<AmazonWebServicesCredentials> credentials = CredentialsProvider.lookupCredentials(
            AmazonWebServicesCredentials.class, itemGroup, ACL.SYSTEM, Collections.EMPTY_LIST);

        if (credentials.isEmpty()) {
            LOG.fine("ID Not found");
            return null;
        }

        for (AmazonWebServicesCredentials awsCredentials : credentials) {
            if (awsCredentials.getId().equals(this.credentialsId)) {
                LOG.log(Level.FINE,"ID found {0}" , this.credentialsId);
                return awsCredentials;
            }
        }
        LOG.fine("ID Not found");
        return  null;
    }

    @Nonnull
    public String getDescription() {
        final AmazonWebServicesCredentials credentials = getCredentials();
        return credentials == null ? "No Valid Credential" : CredentialsNameProvider.name(credentials)
                + " " + (StringUtils.isNotBlank(credentials.getDescription()) ? region : super.getDescription());
    }

    @Nonnull
    @Override
    public Secret getPassword() {
        final AmazonWebServicesCredentials credentials = getCredentials();
        if (credentials == null) throw new IllegalStateException("Invalid credentials");
        LOG.log(Level.FINE,"Password for {0} region : {1}", new Object[]{credentials.getCredentials() , region});
        com.amazonaws.AmazonECRClientFactory factory = new com.amazonaws.AmazonECRClientFactory();
        final AmazonECRClient client = factory.getAmazonECRClientWithProxy(credentials.getCredentials());
        client.setRegion(Region.getRegion(region));

        final GetAuthorizationTokenResult authorizationToken = client.getAuthorizationToken(new GetAuthorizationTokenRequest());
        final List<AuthorizationData> authorizationData = authorizationToken.getAuthorizationData();
        if (authorizationData == null || authorizationData.isEmpty()) {
            throw new IllegalStateException("Failed to retreive authorization token for Amazon ECR");
        }
        LOG.fine("Success");
        return Secret.fromString(authorizationData.get(0).getAuthorizationToken());
    }

    @Nonnull
    @Override
    public String getUsername() {
        return "AWS";
    }

    @Nonnull
    public String getEmail() {
        return "nobody@example.com";
    }
}
