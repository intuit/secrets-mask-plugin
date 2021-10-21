package com.intuit.dev.build.ibp;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.tasks.SimpleBuildWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

public class SecretsMaskBuildWrapper extends SimpleBuildWrapper {
    private static final Logger LOGGER = Logger.getLogger(SecretsMaskBuildWrapper.class.getName());

    public SecretsMaskBuildWrapper() {
    }

    public boolean requiresWorkspace() {
        return false;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath filePath, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
        // no setup needed, but need to override parent abstract method
    }
    
    @Override
    public ConsoleLogFilter createLoggerDecorator(Run<?, ?> build) {
        return new FilterImpl();
    }

    public static final class FilterImpl extends ConsoleLogFilter implements Serializable {
        private static final long serialVersionUID = 1L;

        public FilterImpl() {
        }

        @Override
        public OutputStream decorateLogger(Run build, OutputStream logger) {
            return new SecretsMaskOutputStream(logger, build.getDisplayName());
        }

    }
}
