## Amazon ECR Plugin

This plugin offers integration with [Amazon ECR](https://aws.amazon.com/ecr/) (Docker registry) as a [DockerRegistryToken](https://github.com/jenkinsci/docker-commons-plugin/blob/master/src/main/java/org/jenkinsci/plugins/docker/commons/credentials/DockerRegistryToken.java) source to convert AWS Credentials into a Docker CLI Authentication Token.

It uses ECR [http://docs.aws.amazon.com/AmazonECR/latest/APIReference/API_GetAuthorizationToken.html](GetAuthorizationToken) API to generate such a token.

See [wiki page](https://wiki.jenkins-ci.org/display/JENKINS/Amazon+ECR) for details
