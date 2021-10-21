package com.intuit.dev.build.ibp;

import hudson.Extension;
import hudson.model.Queue;
import jenkins.util.JenkinsJVM;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension(ordinal = 1)
public class SecretsMaskTaskListenerDecorator extends TaskListenerDecorator {
    private static final Logger LOGGER = Logger.getLogger(SecretsMaskTaskListenerDecorator.class.getName());

    private static final long serialVersionUID = 1L;
    String runName;

    public SecretsMaskTaskListenerDecorator() {
        runName = "";
    }

    public SecretsMaskTaskListenerDecorator(WorkflowRun run) {
        this.runName = run.getFullDisplayName();
    }

    @Nonnull
    @Override
    public OutputStream decorate(@Nonnull OutputStream outputStream) throws IOException {
        if (!JenkinsJVM.isJenkinsJVM()) {
            return outputStream;
        }
        return new SecretsMaskOutputStream(outputStream, runName);
    }

    @Extension
    public static final class Factory implements TaskListenerDecorator.Factory{

        public TaskListenerDecorator of(@Nonnull FlowExecutionOwner flowExecutionOwner) {
            try {
                Queue.Executable executable = flowExecutionOwner.getExecutable();
                if (executable instanceof WorkflowRun) {
                    WorkflowRun run = (WorkflowRun) executable;
                    return new SecretsMaskTaskListenerDecorator(run);
                }
            } catch (IOException x) {
                LOGGER.log(Level.WARNING, "Factory encountered error when creating TaskListenerDecorator", x);
            }
            return null;
        }
    }
}
