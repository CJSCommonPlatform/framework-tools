package uk.gov.justice.framework.tools.fraction.runtime;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;

import java.nio.file.Paths;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.undertow.WARArchive;


@DeploymentScoped
public class TransformationArchivePreparer implements DeploymentProcessor {


    private static final String TRANSFORMATION_WAR_PROPERTY_NAME = "transformation.web.archive.name";

    private static final String VIEW_STORE_LISTENER_PROPERTY_NAME = "view.store.archive.name";

    private final Archive<?> archive;

    @Inject
    @ConfigurationValue(VIEW_STORE_LISTENER_PROPERTY_NAME)
    private String library;

    @Inject
    @ConfigurationValue(TRANSFORMATION_WAR_PROPERTY_NAME)
    private String transformationWarName;

    @Inject
    public TransformationArchivePreparer(final Archive archive) {
        this.archive = archive;
    }


    @Override
    public void process() throws IllegalArgumentException {

        if (transformationWarName != null && transformationWarName.equals(archive.getName())) {
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
        }else {
            throw new IllegalArgumentException(String.format("You must enter valid transformation.web.archive.name parameter: %s", transformationWarName));
        }
    }
}
