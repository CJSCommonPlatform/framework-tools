package uk.gov.justice.framework.tools.replay;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ProgressCheckerTest {

    private final ProgressChecker progressChecker = new ProgressChecker();

    @Test
    public void shouldReturnTrueIfTheIndexIsAMultipleOf100() throws Exception {

        final List<Integer> trueValues = new ArrayList<>();

        for(int index = 0; index < 1000; index++) {

            if(progressChecker.shouldLogProgress(index)) {
                trueValues.add(index);
            }
        }

        assertThat(trueValues.size(), is(10));

        assertThat(trueValues.get(0), is(0));
        assertThat(trueValues.get(1), is(100));
        assertThat(trueValues.get(2), is(200));
        assertThat(trueValues.get(3), is(300));
        assertThat(trueValues.get(4), is(400));
        assertThat(trueValues.get(5), is(500));
        assertThat(trueValues.get(6), is(600));
        assertThat(trueValues.get(7), is(700));
        assertThat(trueValues.get(8), is(800));
        assertThat(trueValues.get(9), is(900));
    }
}
