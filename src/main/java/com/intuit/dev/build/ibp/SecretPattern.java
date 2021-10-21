package com.intuit.dev.build.ibp;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SecretPattern implements Describable<SecretPattern> {
    private transient static final Logger LOGGER = Logger.getLogger(SecretPattern.class.getName());
    String name;
    private Pattern pattern;

    @DataBoundConstructor
    public SecretPattern(String name, String regexString) {
        this(name, Pattern.compile(regexString));
    }

    public SecretPattern(String name, Pattern pattern) {
        this.name = name;
        this.pattern = pattern;
    }
    public Descriptor<SecretPattern> getDescriptor() {
        return Jenkins.get().getDescriptor(getClass());
    }

    public String getName() {
        return name;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getRegexString() {
        return pattern.pattern(); //regexString;
    }

    @DataBoundSetter
    public void setRegexString(String regexString) {
        this.pattern = Pattern.compile(regexString);
    }

    public Pattern getPattern() {
        return pattern;
    }

    void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String toString() {
        return "[Name: " + name + ", Regex: " + pattern.pattern() + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (!this.getClass().equals(other.getClass())) {
            return false;
        } else {
            SecretPattern spOther = (SecretPattern) other;
            return (this.getName().equals(spOther.getName())) && (this.getRegexString().equals(spOther.getRegexString()));
        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecretPattern> {
        @Override
        public String getDisplayName() {
            return "SecretPattern";
        }

        public FormValidation doCheckName(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error("Name cannot be empty");
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation doCheckRegexString(@QueryParameter String value) {
            if (value == null || value.isEmpty()) {
                return FormValidation.error("Must specify regex");
            }
            try {
                Pattern.compile(value);
                return FormValidation.ok();
            } catch (PatternSyntaxException e) {
                return FormValidation.error("Must provide valid regex. Error: " + e.getMessage());
            }
        }

    }
}
