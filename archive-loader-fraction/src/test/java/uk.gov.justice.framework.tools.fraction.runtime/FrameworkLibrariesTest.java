package uk.gov.justice.framework.tools.fraction.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.impl.base.path.BasicPath;
import org.junit.Test;

public class FrameworkLibrariesTest {

    @Test
    public void shouldReturnShrinkwrapArchives() throws Exception {
        FrameworkLibraries frameworkLibraries = new FrameworkLibraries(
                "uk.gov.justice.event-store:event-repository-jdbc:",
                "uk.gov.justice.framework-api:framework-api-core");

        Archive[] archives = frameworkLibraries.shrinkWrapArchives();

        assertThat(archives[0].getName(), startsWith("event-repository-jdbc"));
        assertThat(archives[1].getName(), startsWith("framework-api-core"));
    }

    @Test
    public void shouldReturnExclusionFilter() {
        Filter<ArchivePath> filter = new FrameworkLibraries(
                "uk.gov.justice.event-store:event-repository-jdbc",
                "uk.gov.justice.framework-api:framework-api-core")
                .exclusionFilter();

        assertThat(filter.include(new BasicPath("/WEB-INF/lib/event-repository-jdbc")), is(false));
        assertThat(filter.include(new BasicPath("/WEB-INF/lib/framework-api-core")), is(false));
        assertThat(filter.include(new BasicPath("/WEB-INF/lib/other")), is(true));

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgExceptionIfArtifactNotInCorrectFormat() {
        new FrameworkLibraries(
                "uk.gov.justice.event-store:event-repository-jdbc",
                "aaaa");
    }
}
