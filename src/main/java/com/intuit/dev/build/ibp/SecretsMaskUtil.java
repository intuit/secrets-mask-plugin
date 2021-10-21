package com.intuit.dev.build.ibp;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecretsMaskUtil {
    private static final Logger LOGGER = Logger.getLogger(SecretsMaskUtil.class.getName());
    public static final String maskString = "*****";
    public static Matcher patternMatcher(Pattern p, String s) {
        return p.matcher(s);
    }

    public static List<String> patternMatch(List<Pattern> ps, String s) {
        List<String> ret = new ArrayList<>();
        for (Pattern p: ps) {
            Matcher m = p.matcher(s);
            while (m.find()) { // Regex matches
                if (m.groupCount() > 0) { // Regex contains group(s)
                    for (int i = 1; i <= m.groupCount(); i++) {
                        ret.add(m.group(i));
                    }
                } else { // Regex doesn't contain groups, match entire Regex string
                    ret.add(m.group(0));
                }
            }
        }
        return ret;
    }

    public static List<String> patternMatch(Pattern p, String s) {
        return patternMatch(Arrays.asList(p), s);
    }

    public static String secretsMask(List<String> secrets, String s, String runName) {
        if (secrets != null && secrets.size() > 0) {
            for (String secret: secrets) {
                s = s.replaceAll(Pattern.quote(secret),maskString + ((secret.length() > 1) ? secret.substring(secret.length()-2) : ""));
            }
            LOGGER.info(String.format("SecretsMaskUtil.secretsMask() [%s]: %s", runName, StringUtils.strip(s)));
        }
        return s;
    }

    public static List<String> patternMatchGlobal(String s) {
        List<Pattern> globalPatterns = new ArrayList<>();
        GlobalSecretPatternsConfig.get().getSecretPatterns().forEach( (sp) -> globalPatterns.add(sp.getPattern()));

        return patternMatch(globalPatterns, s);
    }

    public static String secretsMaskGlobal(String s, String runName) {
        List<String> secrets = patternMatchGlobal(s);
        if (secrets != null) {
            s = secretsMask(secrets, s, runName);
        }
        return s;
    }
}

