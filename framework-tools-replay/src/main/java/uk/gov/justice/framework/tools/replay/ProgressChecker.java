package uk.gov.justice.framework.tools.replay;

public class ProgressChecker {

    private static final int PROGRESS_INTERVAL = 100;

    public boolean shouldLogProgress(final int index) {
        return index % PROGRESS_INTERVAL == 0;
    }
}
