package uk.gov.justice.framework.tools.replay.wildfly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WildflyLogger {

    private boolean run = true;

    private static final Object MUT_EX = new Object();

    public void sendOutputOfWildflyToTerminal(final Process exec) {
        new Thread(() -> {
            try {
                final InputStream inputStream = exec.getInputStream();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while (isRunning() && (line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (final IOException e) {
                throw new RuntimeException("Error getting output of external process", e);
            }
        }).start();
    }

    public void shutdown() {
        synchronized (MUT_EX) {
            run = false;
        }
    }

    private boolean isRunning() {
        synchronized (MUT_EX) {
            return run;
        }
    }
}
