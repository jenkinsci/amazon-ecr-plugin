#!groovy
def recentLTS = '2.414.2'
buildPlugin(
  // Container agents start faster and are easier to administer
  useContainerAgent: true,
  configurations: [
    [platform: 'linux',   jdk: '21'],
    [platform: 'windows', jdk: '17', jenkins: recentLTS],
  ]
)
