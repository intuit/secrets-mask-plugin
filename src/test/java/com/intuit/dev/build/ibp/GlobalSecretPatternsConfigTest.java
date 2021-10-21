package com.intuit.dev.build.ibp;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



public class GlobalSecretPatternsConfigTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @After
    public void cleanup() {
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        config.clearSecretPatterns();
    }

    @Test
    public void defaultConfigsTest() {
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        assertFalse(config.isMaskDisabled());
        assertTrue(config.getSecretPatterns().isEmpty());
    }

    @Test
    public void roundTripTest() throws Exception {
        SecretPattern sp = new SecretPattern("secret1", "password1234");
        SecretPattern sp2 = new SecretPattern("secret2", "1234secretpassword");
        List<SecretPattern> patterns = Arrays.asList(sp, sp2);

        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();

        config.addSecretPattern(sp);
        config.addSecretPattern(sp2);

        config.setMaskDisabled(true);

        r.configRoundtrip();

        assertEquals(patterns, config.getSecretPatterns());
        assertTrue(config.isMaskDisabled());
    }

    @Test
    public void overridePatternTest() throws Exception {
        SecretPattern sp = new SecretPattern("secret1", "password1234");
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        config.addSecretPattern(sp);

        SecretPattern spNew = new SecretPattern("secret1", "differentpassword");
        config.addSecretPattern(spNew);

        assertEquals(1, config.getSecretPatterns().size());
        assertTrue(config.getSecretPatterns().contains(spNew));
        assertFalse(config.getSecretPatterns().contains(sp));

        r.configRoundtrip();

        assertEquals(1, config.getSecretPatterns().size());
        assertTrue(config.getSecretPatterns().contains(spNew));
        assertFalse(config.getSecretPatterns().contains(sp));
    }

    @Test
    public void removeSecretPatternTest() throws Exception {
        SecretPattern sp = new SecretPattern("secret1", "password1234");
        SecretPattern sp2 = new SecretPattern("secret2", "1234secretpassword");
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        config.addSecretPattern(sp);
        config.addSecretPattern(sp2);

        assertEquals(2, config.getSecretPatterns().size());

        // remove nonexistent pattern
        config.removeSecretPattern("not real", "password");
        assertEquals(2, config.getSecretPatterns().size());

        // remove actual pattern
        config.removeSecretPattern(sp);
        assertEquals(1, config.getSecretPatterns().size());
        assertTrue(config.getSecretPatterns().contains(sp2));
    }

}
