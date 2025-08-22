/*
 * The MIT License
 *
 *  Copyright (c) 2015, CloudBees, Inc.
 *  Copyright (c) 2021, TobiX
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

import com.amazonaws.regions.Regions;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ProxyConfiguration;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.Secret;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.AuthorizationData;
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest;
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse;

/**
 * This new kind of credential provides an embedded {@link software.amazon.awssdk.auth.credentials.AwsCredentials} when a
 * credential for Amazon ECS Registry end point is needed.
 */
public class AmazonECSRegistryCredential extends BaseStandardCredentials
        implements StandardUsernamePasswordCredentials {
    private static final Logger LOG = Logger.getLogger(AmazonECSRegistryCredential.class.getName());

    private final String credentialsId;

    private final String region;

    private final ItemGroup itemGroup;

    public AmazonECSRegistryCredential(
            CredentialsScope scope, @NonNull String credentialsId, String description, ItemGroup itemGroup) {
        this(scope, credentialsId, Region.US_EAST_1, description, itemGroup);
    }

    @Deprecated
    public AmazonECSRegistryCredential(
            @CheckForNull CredentialsScope scope,
            @NonNull String credentialsId,
            Regions region,
            String description,
            ItemGroup itemGroup) {
        this(scope, credentialsId, Region.of(region.getName()), description, itemGroup);
    }

    public AmazonECSRegistryCredential(
            @CheckForNull CredentialsScope scope,
            @NonNull String credentialsId,
            Region region,
            String description,
            ItemGroup itemGroup) {
        super(
                scope,
                "ecr:" + region.id() + ":" + credentialsId,
                "Amazon ECR Registry:"
                        + (StringUtils.isNotBlank(description) ? description : credentialsId)
                        + "-"
                        + region);
        this.credentialsId = credentialsId;
        this.region = region.id();
        this.itemGroup = itemGroup;
    }

    @NonNull
    public String getCredentialsId() {
        return credentialsId;
    }

    public @CheckForNull AmazonWebServicesCredentials getCredentials() {
        LOG.log(Level.FINE, "Looking for Amazon web credentials ID: {0} Region: {1}", new Object[] {
            this.credentialsId, this.region
        });
        List<AmazonWebServicesCredentials> credentials = CredentialsProvider.lookupCredentialsInItemGroup(
                AmazonWebServicesCredentials.class, itemGroup, ACL.SYSTEM2);

        if (LOG.isLoggable(Level.FINEST)) {
            String fullStackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(new Throwable());
            LOG.log(Level.FINEST, "Trace: {0}", fullStackTrace);
        }

        if (credentials.isEmpty()) {
            LOG.fine("ID not found");
            return null;
        }

        for (AmazonWebServicesCredentials awsCredentials : credentials) {
            if (awsCredentials.getId().equals(this.credentialsId)) {
                LOG.log(Level.FINE, "ID found {0}", this.credentialsId);
                return awsCredentials;
            }
        }
        LOG.fine("ID not found");
        return null;
    }

    @NonNull
    @Override
    public String getDescription() {
        String description = super.getDescription();
        LOG.finest(description);
        return description;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        final AmazonWebServicesCredentials credentials = getCredentials();
        if (credentials == null) throw new IllegalStateException("Invalid credentials");
        LOG.log(Level.FINE, "Get password for {0} region : {1}", new Object[] {credentials.getDisplayName(), region});
        if (LOG.isLoggable(Level.ALL)) {
            String fullStackTrace = org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(new Throwable());
            LOG.log(Level.ALL, "Trace: {0}", fullStackTrace);
        }
        ApacheHttpClient.Builder builder = ApacheHttpClient.builder();
        Jenkins instance = Jenkins.getInstanceOrNull();
        ProxyConfiguration proxy = instance != null ? instance.proxy : null;
        if (proxy != null) {
            software.amazon.awssdk.http.apache.ProxyConfiguration.Builder proxyConfiguration =
                    software.amazon.awssdk.http.apache.ProxyConfiguration.builder()
                            .endpoint(URI.create(String.format("http://%s:%s", proxy.name, proxy.port)));
            if (proxy.getUserName() != null) {
                proxyConfiguration.username(proxy.getUserName());
                proxyConfiguration.password(Secret.toString(proxy.getSecretPassword()));
            }
            List<Pattern> patterns = proxy.getNoProxyHostPatterns();
            if (patterns != null && !patterns.isEmpty()) {
                proxyConfiguration.nonProxyHosts(
                        patterns.stream().map(Pattern::pattern).collect(Collectors.toSet()));
            }
            builder.proxyConfiguration(proxyConfiguration.build());
        }

        try (EcrClient client = EcrClient.builder()
                .httpClientBuilder(builder)
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .build()) {

            GetAuthorizationTokenRequest request =
                    GetAuthorizationTokenRequest.builder().build();
            final GetAuthorizationTokenResponse authorizationToken = client.getAuthorizationToken(request);
            final List<AuthorizationData> authorizationData = authorizationToken.authorizationData();
            if (authorizationData == null || authorizationData.isEmpty()) {
                throw new IllegalStateException("Failed to retrieve authorization token for Amazon ECR");
            }
            LOG.fine("Success");
            if (LOG.isLoggable(Level.ALL)) {
                LOG.finest("Auth token: " + authorizationToken);
                LOG.finest("Request: " + request);
            }
            return Secret.fromString(authorizationData.get(0).authorizationToken());
        }
    }

    @NonNull
    @Override
    public String getUsername() {
        return "AWS";
    }

    @NonNull
    public String getEmail() {
        return "nobody@example.com";
    }
}
