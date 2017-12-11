package uk.gov.justice.framework.tools.replay;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.undertow.WARArchive;
import uk.gov.justice.framework.tools.common.command.ShellCommand;

import java.nio.file.Path;

import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.ShrinkWrap.createFromZipFile;
import static org.wildfly.swarm.Swarm.artifact;


@Parameters(separators = "=", commandDescription = "Replay Event Stream Command")
public class Replay implements ShellCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(Replay.class);

    @Parameter(names = "-l", description = "external library")
    private Path library;

    public void run(final String[] args) {

        try {
            new Swarm(args)
                    .start()
                    .deploy(buildDeploymentArtifact())
                    .stop();
            System.exit(0);
        } catch (Exception e) {

            LOGGER.error("Failed to start Wildfly Swarm and deploy War file", e);
        }
    }

    private WARArchive buildDeploymentArtifact() throws Exception {
        LOGGER.error("-------------After  excludeGeneratedApiClasses-----------------------");
        try {
            return create(WARArchive.class, "replay-tool.war")
                    .addClass(AsyncStreamDispatcher.class)
                    .addClass(TransactionalEnvelopeDispatcher.class)
                    .addClass(StartReplay.class)
                    .addClass(StreamDispatchTask.class);
        } catch (Exception e) {
            LOGGER.error("Missing required libraries, unable to create deployable War", e);
            throw e;
        }

    }
}