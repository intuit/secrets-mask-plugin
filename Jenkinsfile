def docker_url = 'docker.intuit.com/docker-rmt'
def ibpMavenSettingsFileCredential = 'ibp-maven-settings-file'
def assetId = '3821321876124013868'
def pluginVersion = ''

pipeline {
    agent {
        kubernetes {
            label "secrets-mask-plugin-${UUID.randomUUID().toString()}"
            defaultContainer "maven"
            yaml """
            apiVersion: v1
            kind: Pod
            spec:
              containers:
              - name: maven
                image: '${docker_url}/maven:3.6.2-jdk-8'
                command:
                - cat
                tty: true
            """
        }
    }
    environment {
        CODE_COV_TOKEN = credentials('ibp-codecov-token')
        IBP_MAVEN_SETTINGS_FILE = credentials("${ibpMavenSettingsFileCredential}")
    }
    options {
        buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '50', artifactNumToKeepStr: '30'))
        timestamps()
    }

    stages {
        stage('deploy') {
            when {
                beforeInput true
                allOf {
                    not { changelog '.*^\\[maven-release-plugin\\].+$' }
                    not { branch 'master' }
                }
            }
            steps {
                container('maven') {
                    sh '''
                        export M3_HOME=${MAVEN_HOME}/bin
                        mvn -s ${IBP_MAVEN_SETTINGS_FILE} clean deploy -B
                    '''
                }
            }
            post {
                success {
                    sh '''
                        curl -s https://codecov.tools.a.intuit.com/bash | bash -s - -U '-k' -C ${GIT_COMMIT} -t ${CODE_COV_TOKEN}
                    '''
                }
            }
        }
        stage('release') {
            when {
                beforeInput true
                allOf {
                    not { changelog '.*^\\[maven-release-plugin\\].+$' }
                    branch 'master'
                }
            }
            environment {
                GITHUB_CREDENTIALS = credentials("ibp-dev")
                GITHUB_USER = "${env.GITHUB_CREDENTIALS_USR}"
                GITHUB_TOKEN = "${env.GITHUB_CREDENTIALS_PSW}"
            }
            steps {
                container('maven') {
                    sh '''
                        echo "https://${GITHUB_USER}:${GITHUB_TOKEN}@github.intuit.com" >> /tmp/gitcredfile
                        git config --global user.name "${GITHUB_USER}"
                        git config --global user.email "first_last@intuit.com"
                        git config --global credential.helper "store --file=/tmp/gitcredfile"
                        export M3_HOME=${MAVEN_HOME}/bin
                        mvn -s ${IBP_MAVEN_SETTINGS_FILE} -Dusername=${GITHUB_USER} -Dpassword=${GITHUB_TOKEN} release:clean release:prepare release:perform -B -Dsettings_file=${IBP_MAVEN_SETTINGS_FILE};
                    '''
                    script {
                        pluginVersion = sh(returnStdout: true, script: "mvn -q -Dexec.executable=echo -Dexec.args='\${project.version}' --non-recursive exec:exec 2>/dev/null").trim()
                    }
                }
            }
            post {
                success {
                    sh '''
                        curl -s https://codecov.tools.a.intuit.com/bash | bash -s - -U '-k' -C ${GIT_COMMIT} -t ${CODE_COV_TOKEN}
                    '''
                }
            }
        }
        stage('coverage') {
            when {
                beforeInput true
                not { changelog '.*^\\[maven-release-plugin\\].+$' }
            }
            steps {
                container('maven') {
                    nexusPolicyEvaluation iqApplication: "${assetId}", iqStage: 'build'
                }
            }
        }
    }
    post {
        success {
            script {
                try {
                    if (env.GIT_BRANCH == "master" || env.GIT_BRANCH == "origin/master") {
                        def startTime = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        def commitId = "${env.GIT_COMMIT}"
                        if (!env.GIT_COMMIT) {
                            echo "No COMMIT_ID environment variable, using git log to get last commit ID..."
                            commitId = sh(returnStdout: true, script: "git log --format='%H' -n 1").trim()
                        }
                        if (pluginVersion) {
                            def json = """{"applicationName":"IBP","applicationVersion":"${pluginVersion}","commitId":"${commitId}","deployStartTime":"${startTime}","assetId":"${assetId}","jobUrl":"${env.BUILD_URL}","envName":"prd"}"""
                            echo "JSON: ${json}"
                            sh """
                                curl \
                                 -s \
                                 -X POST "https://eventbus.intuit.com/v2/ip-opmetrics-deployment-event-prd" \
                                 -H "Content-Type: application/json" \
                                 -d '${json}'
                            """
                        } else {
                            echo "Could not get plugin version. Please try again to allow OpMetrics deployment."
                        } 
                    }
                } catch (curlErr) {
                    echo "ERROR posting deploy metrics: $curlErr"
                    currentBuild.result = 'FAILURE'
                }
            }
        }
    }
}
