package uk.gov.justice.framework.tools.fraction.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;

import static java.lang.String.format;
import static org.wildfly.swarm.Swarm.artifact;

/**
 * Temporary solution to avoid conflicts between framework libraries brought in by the listener war
 * and the ones deployed directly into swarm
 */
public class FrameworkLibraries {

    private final String[] artifacts;

    public FrameworkLibraries(final String... artifacts) {
        for (String artifact : artifacts) {
            if (artifact.indexOf(':') == -1) {
                throw new IllegalArgumentException(format("Incorrect artifact name: %s", artifact));
            }
        }
        this.artifacts = artifacts;
    }


    public Archive[] shrinkWrapArchives() throws Exception {
        final Archive[] archives = new Archive[artifacts.length];
        for (int i = 0; i < artifacts.length; i++) {
            archives[i] = artifact(artifacts[i]);
        }
        return archives;
    }

    public Filter<ArchivePath> exclusionFilter() {
        return new ExclusionFilter();
    }

    private class ExclusionFilter implements Filter<ArchivePath> {

        @Override
        public boolean include(final ArchivePath archivePath) {
            for (String artifact : artifacts) {
                if (archivePath.get().contains(format("/WEB-INF/lib/%s", artifact.split(":")[1]))) {
                    return false;
                }
            }
            return true;
        }
    }
}
