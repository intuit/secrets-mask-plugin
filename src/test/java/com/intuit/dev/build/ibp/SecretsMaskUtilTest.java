package com.intuit.dev.build.ibp;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SecretsMaskUtilTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Before
    public void setup() {
        List<SecretPattern> sps = new ArrayList<>();
        sps.add(new SecretPattern("AWS Json Secrets", "['\"]+(?:(?i:SecretAccessKey)|(?i:AccessKeyId)|(?i:SessionToken))['\"]+:[']?\\s*['\"]+\\s*([a-zA-Z0-9\\/=+]*)['\"]?"));
        sps.add(new SecretPattern("AWS Secret Access Key or Session Token Env Vars", "(?:(?i:AWS_SECRET_ACCESS_KEY)|(?i:AWS_SESSION_TOKEN))=([\\S]+)"));
        sps.add(new SecretPattern("AWS Access Key ID Env Var", "(?i:AWS_ACCESS_KEY_ID)=([\\w]+)"));
        sps.add(new SecretPattern("SSN", "\"?(?i:SSN)\"?\\s*[=\\\\:]?\\s*\"?([0-9]{3}-?[0-9]{2}-?[0-9]{4})\"?"));
        sps.add(new SecretPattern("BAN", "\" ( ? i : ban)\":\\s*(\"[0 - 9] + \")"));

        GlobalSecretPatternsConfig g = GlobalSecretPatternsConfig.get();
        g.setMaskDisabled(true);
        g.setSecretPatterns(sps);
    }

    @Test
    public void testAwsMaskJson() throws IOException {
        String input = FileUtils.readFileToString(new File(getClass().getResource("echoAwsJson.txt").getFile()), "UTF-8");
        String maskedString =  FileUtils.readFileToString(new File(getClass().getResource("echoAwsJsonMasked.txt").getFile()), "UTF-8");

        String output = SecretsMaskUtil.secretsMaskGlobal(input, null);

        assertEquals(maskedString, output);
    }

    @Test
    public void testAwsMaskEnvVar() throws Exception {
        String input = FileUtils.readFileToString(new File(getClass().getResource("awsEnvVar.txt").getFile()), "UTF-8");
        String[] inputLines = input.split("\n");
        String maskedString =  FileUtils.readFileToString(new File(getClass().getResource("awsEnvVarMasked.txt").getFile()), "UTF-8");
        String[] maskedLines = maskedString.split("\n");
        String[] outputLines = new String[inputLines.length];

        for (int i = 0; i < inputLines.length; i++) {
            outputLines[i] = SecretsMaskUtil.secretsMaskGlobal(inputLines[i], null);
        }

        assertArrayEquals(maskedLines, outputLines);
    }

    @Test
    // Test usecase where Regex doesn't contain grouping and want to match entire string
    public void testEntireRegex() throws Exception {
        String expectStr = "AWS_SECRET_ACCESS_KEY=4KJOMHUs8BHcILmZ4KlLfKLjuIuSINfExPy4oZIC";
        String input = "export " + expectStr;
        List<String> expect = new ArrayList<>(Arrays.asList(expectStr));
        Pattern p = Pattern.compile("AWS_SECRET_ACCESS_KEY=[\\S]+");
        assertEquals(expect, SecretsMaskUtil.patternMatch(p, input));
    }

    @Test
    // Test where Regex pattern has multiple matches in a single String
    public void testMultipleMatches() throws Exception {
        String expect1 = "1234";
        String expect2 = "5678";
        List<String> expect = new ArrayList<>(Arrays.asList(expect1, expect2));
        String input = String.format("Secret = %s, Secret = %s", expect1, expect2);
        Pattern p = Pattern.compile("Secret = ([(0-9]*)");
        assertEquals(expect, SecretsMaskUtil.patternMatch(p, input));
    }
}