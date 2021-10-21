import jenkins.model.Jenkins

import java.util.logging.Logger

Logger logger = Logger.getLogger('')
logger.info('GlobalSecretPatternsConfig.groovy: Configuring secret patterns.')

Object globalSecretsConfig = Jenkins.get().getDescriptorByName("com.intuit.dev.build.ibp.GlobalSecretPatternsConfig")

// remove old non-prefixed patterns
globalSecretsConfig.removeSecretPattern("AWS Json Secrets", '"(?:(?i:SecretAccessKey)|(?i:AccessKeyId)|(?i:SessionToken))":\\s*"(\\S*)".*')
globalSecretsConfig.removeSecretPattern("AWS Secret Access Key or Session Token Env Vars", "(?:(?i:AWS_SECRET_ACCESS_KEY)|(?i:AWS_SESSION_TOKEN))=([\\S]+)")
globalSecretsConfig.removeSecretPattern("AWS Access Key ID Env Var", "(?i:AWS_ACCESS_KEY_ID)=([\\w]+)")
globalSecretsConfig.removeSecretPattern("SSN", '"?(?i:SSN)"?\\s*[=\\:]?\\s*"?([0-9]{3}-?[0-9]{2}-?[0-9]{4})"?')
globalSecretsConfig.removeSecretPattern("BAN", '"(?i:ban)":\\s*("[0-9]+")')

// add updated patterns with IBP prefix
globalSecretsConfig.addSecretPattern("IBP - AWS Json Secrets", '[\'"]+(?:(?i:SecretAccessKey)|(?i:AccessKeyId)|(?i:SessionToken))[\'"]+:[\']?\\s*[\'"]+\\s*([a-zA-Z0-9\\/=+]*)[\'"]?')
globalSecretsConfig.addSecretPattern("IBP - AWS Secret Access Key or Session Token Env Vars", "(?:(?i:AWS_SECRET_ACCESS_KEY)|(?i:AWS_SESSION_TOKEN))=([\\S]+)")
globalSecretsConfig.addSecretPattern("IBP - AWS Access Key ID Env Var", "(?i:AWS_ACCESS_KEY_ID)=([\\w]+)")
globalSecretsConfig.addSecretPattern("IBP - SSN", '[\'"]?(?i:SSN)[\'"]?\\s*[=\\:]?\\s*[\'"]?([0-9]{3}-?[0-9]{2}-?[0-9]{4})[\'"]?')
globalSecretsConfig.addSecretPattern("IBP - BAN", '[\'"](?i:ban)[\'"]:\\s*([\'"][0-9]+[\'"])')

logger.info('GlobalSecretPatternsConfig.groovy: Configured patterns: ' + globalSecretsConfig.getSecretPatterns().toString())