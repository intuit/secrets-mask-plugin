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
                image: 'maven:3.6.2-jdk-8'
                command:
                - cat
                tty: true
            """
        }
    }
    environment {
        CODE_COV_TOKEN = credentials('ibp-codecov-token')
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
                        mvn clean deploy -B
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
                        mvn -Dusername=${GITHUB_USER} -Dpassword=${GITHUB_TOKEN} release:clean release:prepare release:perform -B;
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
}
