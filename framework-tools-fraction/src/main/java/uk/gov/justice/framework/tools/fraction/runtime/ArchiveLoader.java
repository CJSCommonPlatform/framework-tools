package uk.gov.justice.framework.tools.fraction.runtime;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;

import javax.inject.Inject;
import java.nio.file.Paths;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;


@DeploymentScoped
public class ArchiveLoader implements DeploymentProcessor {


    private static final String EVENT_LISTENER_WAR = "event.listener.war";

    private final Archive<?> archive;

    //Add multiple additional archives to the CMD, including dependencies
    //jar files need to be added in as CMD arguments

    @Inject
    @ConfigurationValue(EVENT_LISTENER_WAR)
    private String library;

    @Inject
    public ArchiveLoader(final Archive archive) {
        this.archive = archive;
    }


    @Override
    public void process() {

            final WebArchive webArchive = createFromZipFile(WebArchive.class, Paths.get(library).toFile());

            final FrameworkLibraries frameworkLibraries = new FrameworkLibraries(
                    "uk.gov.justice.services:event-repository-jdbc",
                    "uk.gov.justice.services:framework-api-core",
                    "uk.gov.justice.services:core",
                    "uk.gov.justice.services:persistence-jdbc",
                    "uk.gov.justice.services:event-buffer-core");


            final WebArchive excludeGeneratedApiClasses = create(WebArchive.class, "ExcludeGeneratedApiClasses")
                    .merge(webArchive, frameworkLibraries.exclusionFilter());

            final WARArchive war = archive.as(WARArchive.class);

            war.merge(excludeGeneratedApiClasses);
    }
}
