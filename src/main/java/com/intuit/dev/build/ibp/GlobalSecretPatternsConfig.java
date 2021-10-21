package com.intuit.dev.build.ibp;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@Extension
public class GlobalSecretPatternsConfig extends GlobalConfiguration {
    private transient static final Logger LOGGER = Logger.getLogger(GlobalSecretPatternsConfig.class.getName());
    private boolean maskDisabled;
    private List<SecretPattern> secretPatterns;
    private HashMap<String, SecretPattern> secretPatternsMap; // Just for keeping a mapping of patterns based on Name to override duplicate Names

    @DataBoundConstructor
    public GlobalSecretPatternsConfig(boolean maskDisabled, List<SecretPattern> patterns) {
        this.maskDisabled = maskDisabled;
        this.secretPatterns = new ArrayList<>();
        this.secretPatternsMap = new HashMap<>();
        if (patterns != null) {
            patterns.forEach( sp -> {
                secretPatterns.add(sp);
                secretPatternsMap.put(sp.getName(), sp);
            } );
        }
    }

    public GlobalSecretPatternsConfig() {
        super.load();
        if (secretPatterns != null) {
            if (secretPatternsMap != null)
                secretPatternsMap.clear();
            else
                secretPatternsMap = new HashMap<>();
            secretPatterns.forEach( sp -> secretPatternsMap.put(sp.getName(), sp));
        }
    }

    public boolean isMaskDisabled() {
        return maskDisabled;
    }

    @DataBoundSetter
    public void setMaskDisabled(@Nonnull boolean maskDisabled) {
        if (this.maskDisabled != maskDisabled) {
            LOGGER.info("Secrets masking has been " + (maskDisabled ? "disabled" : "enabled"));
            this.maskDisabled = maskDisabled;
            this.save();
        }
    }

    public List<SecretPattern> getSecretPatterns() {
        if (secretPatterns == null) {
            this.secretPatterns = new ArrayList<>();
            if (this.secretPatternsMap == null)
                this.secretPatternsMap = new HashMap<>();
        }
        return this.secretPatterns;
    }

    @DataBoundSetter
    public void setSecretPatterns(List<SecretPattern> patterns) {
        if (secretPatterns == null) {
            secretPatterns = new ArrayList<>();
        } else {
            this.secretPatterns.clear();
            this.secretPatternsMap.clear();
        }
        if (patterns != null) {
            patterns.forEach( sp -> this.addSecretPattern(sp));
        } else {
            LOGGER.info("Patterns was null and so empty set...");
        }
        this.save();
    }

    public void removeSecretPatterns(Collection<SecretPattern> patterns) {
        for (SecretPattern p: patterns) {
            this.removeSecretPattern(p);
        }
        this.save();
    }

    private boolean secretPatternsIsNull() {
        return this.secretPatternsMap == null || this.secretPatterns == null;
    }

    public void removeSecretPattern(SecretPattern sp) {
        if (!secretPatternsIsNull()) {
            this.secretPatterns.remove(sp);
            this.secretPatternsMap.remove(sp.getName());
            this.save();
        }
    }

    public void removeSecretPattern(String name, String regex) {
        SecretPattern toDeletePattern = new SecretPattern(name, regex);
        if (!secretPatternsIsNull() && secretPatternsMap.get(name) != null && secretPatternsMap.get(name).equals(toDeletePattern)) {
            removeSecretPattern(toDeletePattern);
        }
    }

    public void addSecretPattern(SecretPattern sp) {
        if (secretPatterns == null)
            this.secretPatterns = new ArrayList<>();
        if (secretPatternsMap == null)
            this.secretPatternsMap = new HashMap<>();

        if (secretPatternsMap.containsKey(sp.getName()))
            removeSecretPattern(secretPatternsMap.get(sp.getName()));

        secretPatterns.add(sp);
        secretPatternsMap.put(sp.getName(), sp);

        this.save();
    }

    public void addSecretPattern(String name, String regex) {
        SecretPattern sp = new SecretPattern(name, regex);
        addSecretPattern(sp);
    }

    public void clearSecretPatterns() {
        this.secretPatterns.clear();
        this.secretPatternsMap.clear();
    }

    public static GlobalSecretPatternsConfig get() {
        return (GlobalSecretPatternsConfig) Jenkins.getInstance().getDescriptor(GlobalSecretPatternsConfig.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        if (!json.has("secretPatterns")) {
            json.put("secretPatterns", new JSONArray());
        }
        req.bindJSON(this, json);
        save();
        return true;
    }
}
