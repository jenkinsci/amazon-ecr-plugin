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

import com.cloudbees.jenkins.plugins.awscredentials.AmazonWebServicesCredentials;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.security.ACL;
import org.acegisecurity.Authentication;

import java.util.Collections;
import java.util.List;

/**
 * This class automatically wraps existing {@link AmazonWebServicesCredentials} instances
 * into a username password credential type that is compatible with Docker
 * remote API client plugin.
 */
@Extension
public class AmazonECSRegistryCredentialsProvider extends CredentialsProvider {


    @NonNull
    @Override
    public <C extends Credentials> List<C> getCredentials(@NonNull Class<C> type, @Nullable ItemGroup itemGroup, @Nullable Authentication authentication) {

        if (!type.isAssignableFrom(AmazonECSRegistryCredential.class)) {
            return ImmutableList.of();
        }

        List<C> derived = Lists.newLinkedList();

        final List<AmazonWebServicesCredentials> list = lookupCredentials(AmazonWebServicesCredentials.class, itemGroup,
                ACL.SYSTEM, Collections.EMPTY_LIST);

        for (AmazonWebServicesCredentials credentials : list) {
            derived.add((C) new AmazonECSRegistryCredential(
                    credentials.getScope(),
                    credentials.getId()));
        }

        return derived;
    }
}
