package com.intuit.dev.build.ibp;

import hudson.Extension;
import hudson.console.ConsoleLogFilter;
import hudson.model.Run;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

@Extension
public class SecretsMaskConsoleLogFilter extends ConsoleLogFilter implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(SecretsMaskConsoleLogFilter.class.getName());

    private static final long serialVersionUID = 1L;

    public SecretsMaskConsoleLogFilter() {
    }

    @Override
    public OutputStream decorateLogger(Run build, OutputStream logger) {
        return new SecretsMaskOutputStream(logger, build.getFullDisplayName());
    }
}
