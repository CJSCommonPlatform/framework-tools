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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@DeploymentScoped
public class TransformationArchivePreparer implements DeploymentProcessor {

    public static final String TRANSFORMATION_JAR_PROPERTY_NAME = "transformation.impl.library";

    public static final String TRANSFORMATION_WAR_PROPERTY_NAME = "transformation.web.archive.name";

    public static final String VIEW_STORE_LISTENER_PROPERTY_NAME = "view.store.archive.name";

    //private static final Logger LOGGER = LoggerFactory.getLogger(TransformationArchivePreparer.class);

    private final Archive<?> archive;

    @Inject
    @ConfigurationValue(VIEW_STORE_LISTENER_PROPERTY_NAME)
    private String library;

    @Inject
    @ConfigurationValue(TRANSFORMATION_JAR_PROPERTY_NAME)
    private String extraJarName;

    @Inject
    @ConfigurationValue(TRANSFORMATION_WAR_PROPERTY_NAME)
    private String transformationWarName;

    @Inject
    public TransformationArchivePreparer(Archive archive) {
        this.archive = archive;

       // LOGGER.info("-------------- HELLO MARTIN -------------!");
    }


    @Override
    public void process() throws Exception {

        if (transformationWarName.equals(archive.getName()) && transformationWarName != null) {
            final WebArchive webArchive = createFromZipFile(WebArchive.class, Paths.get(library).toFile());

            final FrameworkLibraries frameworkLibraries = new FrameworkLibraries(
                    "uk.gov.justice.services:event-repository-jdbc:2.2.1",
                    "uk.gov.justice.services:framework-api-core:2.2.1",
                    "uk.gov.justice.services:core:2.2.1",
                    "uk.gov.justice.services:persistence-jdbc:2.2.1",
                    "uk.gov.justice.services:event-buffer-core:2.2.1");


            final WebArchive excludeGeneratedApiClasses = create(WebArchive.class, "ExcludeGeneratedApiClasses")
                    .merge(webArchive, frameworkLibraries.exclusionFilter());

            WARArchive war = archive.as(WARArchive.class);

            war.addAsLibraries(frameworkLibraries.shrinkWrapArchives())
                    .merge(excludeGeneratedApiClasses);
        }
    }


}
