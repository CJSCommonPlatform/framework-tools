package uk.gov.justice.framework.tools.replay.wildfly;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class WildflyRunner {

    private static final String WILDFLY_PROCESS_LOCKFILE = "src/test/resources/processFile";

    private final WildflyLogger wildflyLogger = new WildflyLogger();

    public boolean run(
            final long timeoutInSeconds,
            final boolean shouldLogOutputToConsole,
            final boolean enableRemoteDebugging) {

        final String command = createStartWildflyCommand(enableRemoteDebugging);

        System.out.println(format("Running wildfly command '%s'", command));

        final Process exec = execute(command);

        if (shouldLogOutputToConsole) {
            wildflyLogger.sendOutputOfWildflyToTerminal(exec);
        }

        createProcessLockfile();

        System.out.println("Wildfly started successfully. Running replay...");
        return waitUntilDone(exec, timeoutInSeconds);
    }

    private boolean waitUntilDone(final Process exec, final long timeoutInSeconds) {
        // Kill the process if timeout exceeded
        boolean processTerminated = waitForProcessTermination(exec, timeoutInSeconds);

        wildflyLogger.shutdown();

        if (!processTerminated) {
            killWIldfly(exec, timeoutInSeconds);
        } else {
            System.out.println("WildFly completed successfully.");
            return true;
        }
        return false;
    }

    private void killWIldfly(final Process exec, final long timeoutInSeconds) {
        final boolean processTerminated;
        System.err.println(format("WildFly Swarm process failed to terminate after %s seconds!", timeoutInSeconds));
        exec.destroyForcibly();

        processTerminated = waitForProcessTermination(exec, 10L);
        if (!processTerminated) {
            System.err.println("Failed to forcibly terminate WildFly Swarm process!");
        } else {
            System.err.println("WildFly Swarm process forcibly terminated.");
        }
    }

    private String createStartWildflyCommand(final boolean enableRemoteDebugging) {
        final String replayJarLocation = getResource("framework-tools-replay*.jar");
        final String standaloneDSLocation = getResource("standalone-ds.xml");
        final String listenerLocation = getResource("replay-tool-it-example-listener*.war");

        String debug = "";

        if (enableRemoteDebugging) {
            debug = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005";
        }

        return commandFrom(debug, replayJarLocation, standaloneDSLocation, listenerLocation);
    }


    private boolean waitForProcessTermination(final Process exec, final long timeout) {
        try {
            return exec.waitFor(timeout, SECONDS);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }

        return false;
    }

    private Process execute(final String command) {
        try {
            return Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException(format("Failed to execute external process '%s'", command), e);
        }
    }

    private void createProcessLockfile() {
        try {
            Runtime.getRuntime().exec(format("touch %s", WILDFLY_PROCESS_LOCKFILE));
        } catch (final IOException e) {
            throw new RuntimeException(format("Failed to touch file '%s'", WILDFLY_PROCESS_LOCKFILE), e);
        }
    }


    private String commandFrom(final String debugString,
                               final String replayJarLocation,
                               final String standaloneDSLocation,
                               final String listenerLocation) {
        return format("java %s -Dorg.wildfly.swarm.mainProcessFile=%s -Devent.listener.war=%s -jar %s -c %s -Dswarm.logging=DEBUG",
                debugString,
                WILDFLY_PROCESS_LOCKFILE,
                listenerLocation,
                replayJarLocation,
                standaloneDSLocation);
    }

    @SuppressWarnings("ConstantConditions")
    private String getResource(final String pattern) {
        final File dir = new File(getClass().getClassLoader().getResource("").getPath());
        final FileFilter fileFilter = new WildcardFileFilter(pattern);
        return requireNonNull(dir.listFiles(fileFilter))[0].getAbsolutePath();
    }
}
