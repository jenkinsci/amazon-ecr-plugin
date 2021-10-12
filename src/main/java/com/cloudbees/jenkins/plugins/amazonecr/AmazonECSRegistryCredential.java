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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.AmazonECRClientBuilder;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import hudson.util.Secret;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

/**
 * This new kind of credential provides an embedded {@link com.amazonaws.auth.AWSCredentials} when a
 * credential for Amazon ECS Registry end point is needed.
 */
public class AmazonECSRegistryCredential extends BaseStandardCredentials
    implements StandardUsernamePasswordCredentials {
  private static final Logger LOG = Logger.getLogger(AmazonECSRegistryCredential.class.getName());

  private final String credentialsId;

  private final Regions region;

  private final ItemGroup itemGroup;

  public AmazonECSRegistryCredential(
      CredentialsScope scope,
      @Nonnull String credentialsId,
      String description,
      ItemGroup itemGroup) {
    this(scope, credentialsId, Regions.US_EAST_1, description, (ItemGroup<?>) itemGroup);
  }

  public AmazonECSRegistryCredential(
      @CheckForNull CredentialsScope scope,
      @Nonnull String credentialsId,
      Regions region,
      String description,
      ItemGroup itemGroup) {
    super(
        scope,
        "ecr:" + region.getName() + ":" + credentialsId,
        "Amazon ECR Registry:"
            + (StringUtils.isNotBlank(description) ? description : credentialsId)
            + "-"
            + region);
    this.credentialsId = credentialsId;
    this.region = region;
    this.itemGroup = itemGroup;
  }

  @Nonnull
  public String getCredentialsId() {
    return credentialsId;
  }

  public @CheckForNull AmazonWebServicesCredentials getCredentials() {
    LOG.log(
        Level.FINE,
        "Looking for Amazon web credentials ID: {0} Region: {1}",
        new Object[] {this.credentialsId, this.region});
    List<AmazonWebServicesCredentials> credentials =
        CredentialsProvider.lookupCredentials(
            AmazonWebServicesCredentials.class, itemGroup, ACL.SYSTEM, Collections.EMPTY_LIST);

    if (LOG.isLoggable(Level.FINEST)) {
      String fullStackTrace =
          org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(new Throwable());
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

  @Nonnull
  public String getDescription() {
    String description = super.getDescription();
    LOG.finest(description);
    return description;
  }

  @Nonnull
  @Override
  public Secret getPassword() {
    final AmazonWebServicesCredentials credentials = getCredentials();
    if (credentials == null) throw new IllegalStateException("Invalid credentials");
    LOG.log(
        Level.FINE,
        "Get password for {0} region : {1}",
        new Object[] {credentials.getDisplayName(), region});
    if (LOG.isLoggable(Level.ALL)) {
      String fullStackTrace =
          org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(new Throwable());
      LOG.log(Level.ALL, "Trace: {0}", fullStackTrace);
    }
    ClientConfiguration conf = new ClientConfiguration();
    Jenkins j = Jenkins.get();
    if (j.proxy != null) {
      conf.setProxyHost(j.proxy.name);
      conf.setProxyPort(j.proxy.port);
      conf.setProxyUsername(j.proxy.getUserName());
      Secret password = j.proxy.getSecretPassword();
      if (password != null) conf.setProxyPassword(password.getPlainText());
    }

    AmazonECRClientBuilder builder = AmazonECRClientBuilder.standard();
    builder.setCredentials(credentials);
    builder.setClientConfiguration(conf);
    builder.setRegion(Region.getRegion(region).getName());
    final AmazonECR client = builder.build();

    GetAuthorizationTokenRequest request = new GetAuthorizationTokenRequest();
    final GetAuthorizationTokenResult authorizationToken = client.getAuthorizationToken(request);
    final List<AuthorizationData> authorizationData = authorizationToken.getAuthorizationData();
    if (authorizationData == null || authorizationData.isEmpty()) {
      throw new IllegalStateException("Failed to retrieve authorization token for Amazon ECR");
    }
    LOG.fine("Success");
    if (LOG.isLoggable(Level.ALL)) {
      LOG.finest("Auth token: " + authorizationToken);
      LOG.finest("Request: " + request);
    }
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
