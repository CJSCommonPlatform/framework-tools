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
public class TransformationArchivePreparerTest {
    @Mock
    private Archive archive;

    @InjectMocks
    private TransformationArchivePreparer transformationArchivePreparer;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTransformationWarNameIsNull() {
        transformationArchivePreparer.process();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenArchiveNameIsInvalid() throws Exception {
        final Field privateField = transformationArchivePreparer.getClass().getDeclaredField("transformationWarName");
        privateField.setAccessible(true);
        privateField.set(transformationArchivePreparer, "ValidArchiveName");

        when(archive.getName()).thenReturn("InvalidArchiveName");

        transformationArchivePreparer.process();
    }
}
