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

import hudson.Extension;
import hudson.util.Secret;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import jenkins.authentication.tokens.api.AuthenticationTokenException;
import jenkins.authentication.tokens.api.AuthenticationTokenSource;
import org.jenkinsci.plugins.docker.commons.credentials.DockerRegistryToken;

/** @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a> */
@Extension
public class AmazonECSRegistryTokenSource
    extends AuthenticationTokenSource<DockerRegistryToken, AmazonECSRegistryCredential> {

  private static final Logger LOG = Logger.getLogger(AmazonECSRegistryTokenSource.class.getName());

  public AmazonECSRegistryTokenSource() {
    super(DockerRegistryToken.class, AmazonECSRegistryCredential.class);
  }

  @Nonnull
  @Override
  public DockerRegistryToken convert(@Nonnull AmazonECSRegistryCredential credential)
      throws AuthenticationTokenException {
    LOG.log(
        Level.FINE,
        "Converting credential to Docker registry token : {0}",
        credential.getCredentialsId());
    return new DockerRegistryToken(
        credential.getEmail(), Secret.toString(credential.getPassword()));
  }
}
