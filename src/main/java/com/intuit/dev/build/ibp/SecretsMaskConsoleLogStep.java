package com.intuit.dev.build.ibp;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.log.TaskListenerDecorator;
import org.jenkinsci.plugins.workflow.steps.AbstractStepExecutionImpl;
import org.jenkinsci.plugins.workflow.steps.BodyExecutionCallback;
import org.jenkinsci.plugins.workflow.steps.BodyInvoker;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.logging.Logger;

public class SecretsMaskConsoleLogStep extends Step {
    private static final Logger LOGGER = Logger.getLogger(SecretsMaskConsoleLogStep.class.getName());

    @DataBoundConstructor
    public SecretsMaskConsoleLogStep() {
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ExecutionImpl(context);
    }

    @Extension(optional = true)
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getFunctionName() {
            return "maskSecretsConsoleLog";
        }

        /**
         * {@inheritDoc}
         */
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Mask secrets from global secrets regex patterns";
        }
    }

    public static class ExecutionImpl extends AbstractStepExecutionImpl {
        public ExecutionImpl(StepContext context) {
            super(context);
        }

        @Override
        public boolean start() throws Exception {
            //refer to WithContextStep implementation
            StepContext context = getContext();
            Run run = context.get(Run.class);
            BodyInvoker invoker = context.newBodyInvoker().withCallback(new BodyExecutionCallbackConsole());
            invoker.withContext(
                    TaskListenerDecorator.merge(
                            context.get(TaskListenerDecorator.class),
                            new SecretsMaskTaskListenerDecorator((WorkflowRun) run)));
            invoker.start();
            return false;
        }

        @Override
        public void stop(@Nonnull Throwable cause) throws Exception {
            getContext().onFailure(cause);
        }
    }

    public static class BodyExecutionCallbackConsole extends BodyExecutionCallback.TailCall {
        private static final long serialVersionUID = 1L;

        @Override
        protected void finished(StepContext stepContext) throws Exception {
        }
    }
}
