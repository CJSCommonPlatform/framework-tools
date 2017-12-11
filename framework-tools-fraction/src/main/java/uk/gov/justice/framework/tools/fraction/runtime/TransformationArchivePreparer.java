package uk.gov.justice.framework.tools.fraction.runtime;

import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;

import java.io.File;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.wildfly.swarm.spi.api.DeploymentProcessor;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.runtime.annotations.DeploymentScoped;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.undertow.WARArchive;

@DeploymentScoped
public class TransformationArchivePreparer implements DeploymentProcessor {

    public static final String TRANSFORMATION_JAR_PROPERTY_NAME = "transformation.impl.library";

    public static final String TRANSFORMATION_WAR_PROPERTY_NAME = "transformation.web.archive.name";

    //private static final Logger LOGGER = LoggerFactory.getLogger(TransformationArchivePreparer.class);

    private final Archive<?> archive;

    @Inject
    @ConfigurationValue(TRANSFORMATION_JAR_PROPERTY_NAME)
    private String extraJarName;

    @Inject
    @ConfigurationValue(TRANSFORMATION_WAR_PROPERTY_NAME)
    private String transformationWarName;

    @Inject
    public TransformationArchivePreparer(Archive archive) {
        this.archive = archive;
        //LOGGER.info("TransformationArchivePreparer.init [archive={}]", archive);
        System.err.println("TransformationArchivePreparer.init [archive=" + archive + "]");

        System.err.println("SYS PROP TRANSFORMATION_JAR_PROPERTY_NAME=" + System.getProperty(TRANSFORMATION_JAR_PROPERTY_NAME));
        System.err.println("SYS PROP TRANSFORMATION_WAR_PROPERTY_NAME=" + System.getProperty(TRANSFORMATION_WAR_PROPERTY_NAME));
    }



    @Override
    public void process() throws Exception {
//        LOGGER.info("process() [archive={}]", archive.getClass().getName());

        if (transformationWarName != null && extraJarName != null) {

            if (transformationWarName.equals(archive.getName())) {
//                LOGGER.info("process() [archive={}], adding transformation lib", archive.getClass().getName());

                WARArchive war = archive.as(WARArchive.class);
                war.addAsLibrary(loadTransformationJar());
            }

        }
        else {
//            LOGGER.info("Not processing as one or both of {} and {} System properties are not set",
//                         TRANSFORMATION_WAR_PROPERTY_NAME, TRANSFORMATION_JAR_PROPERTY_NAME);
        }

    }


    private JARArchive loadTransformationJar() {
//        LOGGER.info("loadTransformationJar() loading [{}]",extraJarName);
        JARArchive lib = createFromZipFile(JARArchive.class, new File(extraJarName));

        return lib;

    }

}
