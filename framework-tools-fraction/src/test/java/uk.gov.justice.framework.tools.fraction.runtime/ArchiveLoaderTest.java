package uk.gov.justice.framework.tools.fraction.runtime;

import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArchiveLoaderTest {
    @Mock
    private Archive archive;

    @InjectMocks
    private ArchiveLoader archiveLoader;

    //TODO: Positive tests

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenArchiveNameIsInvalid() throws Exception {
        final Field privateField = archiveLoader.getClass().getDeclaredField("library");
        privateField.setAccessible(true);
        privateField.set(archiveLoader, "ValidArchiveName");

        when(archive.getName()).thenReturn("InvalidArchiveName");

        archiveLoader.process();
    }
}
