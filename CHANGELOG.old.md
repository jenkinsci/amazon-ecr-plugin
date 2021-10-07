# Old Changelog

This is the old changelog imported from the Jenkins wiki, for newer changes see
[GitHub Releases](https://github.com/jenkinsci/amazon-ecr-plugin/releases).

## 1.6 (2017-05-16)

- [JENKINS-34437](https://issues.jenkins-ci.org/browse/JENKINS-34437) Enable
  amazon-ecr-plugin behind proxy
- Performance improvements
- Set minor version to 1.642.1
- Upgrade Credentials Plugin
- Upgrade AWS Java SDK Plugin
- Upgrade AWS Credentials Plugin
- Upgrade Docker Commons Plugin
- improve log

## 1.5 - Burned

## 1.4 (2016-10-29)

- [JENKINS-38465](https://issues.jenkins-ci.org/browse/JENKINS-38465) ECR
  Plugin now it is compatible with credential stored into folders
- [JENKINS-36127](https://issues.jenkins-ci.org/browse/JENKINS-36127) Resolved
  a NPE when attempt to configure docker build and publish
- [JENKINS-34958](https://issues.jenkins-ci.org/browse/JENKINS-34958) New
  credential format that contains the region. For example, by specifying the
  following credentials: ecr:us-west-2:credential-id, the provider will set the
  Region of the AWS Client to us-west-2, when requesting for Authorisation
  token.

## 1.3 (2016-06-06)

- 1.2 Release failed to upload the artifact - so just release again to
  correctly upload the artifact.

NOTE: This release doesn't contain any update.

## 1.2 (2016-06-03)

- Update parent pom

## 1.1 (2016-05-30)

- [JENKINS-35220](http://localhost:8085/display/JENKINS/Amazon+ECR#)
  Correctly display the credentials

## 1.0 (2016-01-12)

- Replace custom ECR API client with aws-java-sdk

## 1.0-beta-1 (2015-12-22)

- Initial release
