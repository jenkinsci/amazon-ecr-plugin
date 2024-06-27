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

import com.amazonaws.regions.Regions;
import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.ItemGroup;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

/**
 * This class automatically wraps existing {@link AmazonWebServicesCredentials} instances into a
 * username password credential type that is compatible with Docker remote API client plugin.
 */
@Extension
public class AmazonECSRegistryCredentialsProvider extends CredentialsProvider {

    private static final Logger LOG = Logger.getLogger(AmazonECSRegistryCredentialsProvider.class.getName());

    @NonNull
    @Override
    public <C extends Credentials> List<C> getCredentialsInItemGroup(
            @NonNull Class<C> type,
            @Nullable ItemGroup itemGroup,
            @Nullable Authentication authentication,
            @NonNull List<DomainRequirement> domainRequirements) {

        if (!type.isAssignableFrom(AmazonECSRegistryCredential.class)) {
            return Collections.emptyList();
        }

        List<C> derived = new LinkedList<>();

        final List<AmazonWebServicesCredentials> list = lookupCredentialsInItemGroup(
                AmazonWebServicesCredentials.class, itemGroup, authentication, domainRequirements);

        for (AmazonWebServicesCredentials credentials : list) {
            LOG.log(
                    Level.FINE,
                    "Resolving Amazon Web Services credentials of scope {0} with id {1} , itemgroup {2}",
                    new Object[] {credentials.getScope(), credentials.getId(), itemGroup});
            derived.add((C) new AmazonECSRegistryCredential(
                    credentials.getScope(), credentials.getId(), credentials.getDescription(), itemGroup));

            for (Regions region : Regions.values()) {
                LOG.log(
                        Level.FINE,
                        "Resolving Amazon Web Services credentials of scope {0} with id {1} and region {2}",
                        new Object[] {credentials.getScope(), credentials.getId(), region});
                derived.add((C) new AmazonECSRegistryCredential(
                        credentials.getScope(), credentials.getId(), region, credentials.getDescription(), itemGroup));
            }
        }

        return derived;
    }
}
