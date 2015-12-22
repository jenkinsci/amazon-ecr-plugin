## Amazon ECR Plugin

This plugin offers integration with Amazon ECR (Docker registry) as a DockerRegistryToken source to convert
Amazon Credentials into a Docker CLI Authentication Token.

It uses ECR GetAuthorizationToken API to generate such a token

As aws-java-sdk does not provide ECR support, a basic client has been implemented in com.amazonaws.services.ecs package.
