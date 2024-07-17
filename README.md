# Amazon ECR Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/amazon-ecr.svg)](https://plugins.jenkins.io/amazon-ecr)
[![GitHub release](https://img.shields.io/github/release/jenkinsci/amazon-ecr-plugin.svg?label=release)](https://github.com/jenkinsci/amazon-ecr-plugin/releases/latest)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/amazon-ecr.svg?color=blue)](https://plugins.jenkins.io/amazon-ecr)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Famazon-ecr-plugin%2Fmain)](https://ci.jenkins.io/job/Plugins/job/amazon-ecr-plugin/job/main/)
[![GitHub license](https://img.shields.io/github/license/jenkinsci/amazon-ecr-plugin.svg)](https://github.com/jenkinsci/amazon-ecr-plugin/blob/main/LICENSE.txt)
[![Maintenance](https://img.shields.io/maintenance/yes/2024.svg)]()

This plugin offers integration with [Amazon Container Registry
(ECR)](https://aws.amazon.com/ecr/) as a [DockerRegistryToken] source to convert
Amazon Credentials into a Docker CLI Authentication Token.

[DockerRegistryToken]: https://github.com/jenkinsci/docker-commons-plugin/blob/master/src/main/java/org/jenkinsci/plugins/docker/commons/credentials/DockerRegistryToken.java

## About

Amazon ECR plugin implements a Docker Token producer to convert Amazon
credentials to Jenkins’ API used by (mostly) all Docker-related plugins.

Thanks to this producer, you can select your existing registered Amazon
credentials for various Docker operations in Jenkins, for example using the
Docker Build and Publish plugin:

![](.github/build-and-publish.png)

## Installation

Navigate to the "Plugin Manager" screen, install the "Amazon ECR" plugin and
restart Jenkins.

The plugin will use the proxy configured on Jenkins if it is set.

Recommended logger for troubleshooting, you have to take care where you publish
these logs could contain sensitive information

- com.cloudbees.jenkins.plugins.amazonecr
- com.amazonaws
- org.apache.http.wire
- org.jenkinsci.plugins.docker.workflow

## Docker Pipeline Usage

When using the [Docker Pipeline
Plugin](https://plugins.jenkins.io/docker-workflow/), in order to obtain an ECR
login credential, you must use the ecr provider prefix.

```groovy
docker.withRegistry("https://your.ecr.domain.amazonws.com", "ecr:us-east-1:credential-id") {
  docker.image("your-image-name").push()
}
```

If you experience authentication issues, you would try to remove user
docker configuration files on the agents before to run the docker
commands, something like this pipeline script.

```groovy
node {
  // cleanup current user docker credentials
  sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'

  // configure registry
  docker.withRegistry('https://ID.ecr.eu-west-1.amazonaws.com', 'ecr:eu-west-1:86c8f5ec-1ce1-4e94-80c2-18e23bbd724a') {

    // build image
    def customImage = docker.build("my-image:${env.BUILD_ID}")

    // push image
    customImage.push()
  }
}
```

## Development

### Testing

Unfortunately, testing against AWS isn't very straightforward, since you always
need an AWS account with correct setup, which might incur some costs. Current
tests try to make this as easy as possible. You need a user with read
permission to ECR (AWS IAM policy `AmazonEC2ContainerRegistryReadOnly` should
suffice) and an (empty) container registry. The test expect these details in
the following environment variables:

```shell
export AWS_ACCESS_KEY_ID=<your-key-id-here>
export AWS_SECRET_ACCESS_KEY=<your-secret-access-key-here>
export AWS_REGISTRY_HOST=<some-number>.dkr.ecr.us-east-1.amazonaws.com
```

When those are set correctly, `mvn test` should run those tests successfully.

### Code Style

This plugin uses [Google Java Code Style], which is enforced by the [spotless]
plugin. If the build fails because you were using the "wrong" style, you can
fix it by running:

    $ mvn spotless:apply

to reformat code in the proper style.

[Google Java Code Style]: https://google.github.io/styleguide/javaguide.html
[spotless]: https://github.com/diffplug/spotless
