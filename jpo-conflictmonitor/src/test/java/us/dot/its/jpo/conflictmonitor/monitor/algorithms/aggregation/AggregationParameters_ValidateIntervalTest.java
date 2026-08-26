package us.dot.its.jpo.conflictmonitor.monitor.algorithms.aggregation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
public class AggregationParameters_ValidateIntervalTest {

    @ParameterizedTest
    @MethodSource("getParams")
    public void testValidateInterval(int interval, ChronoUnit units, boolean expectValid) {
        var params = new AggregationParameters();
        params.setInterval(interval);
        params.setIntervalUnits(units);
        assertThat(String.format("params: %s", params), params.validateInterval(), equalTo(expectValid));
    }

    static Stream<Arguments> getParams() {
        var params = new ArrayList<Arguments>();
        for (int i : validSecondsMinutes) {
            addParams(params, i, SECONDS, true);
            addParams(params, i, MINUTES, true);
        }
        for (int i : invalidSecondsMinutes) {
            addParams(params, i, SECONDS, false);
            addParams(params, i, MINUTES, false);
        }
        for (int i : validHours) {
            addParams(params, i, HOURS, true);
        }
        for (int i : invalidHours) {
            addParams(params, i, HOURS, false);
        }
        return params.stream();
    }

    private static void addParams(ArrayList<Arguments> params, int interval, ChronoUnit units, boolean expectValid) {
        params.add(Arguments.of(interval, units, expectValid));
    }

    private static final int[] validSecondsMinutes = new int[] { 1, 2, 3, 4, 5, 6, 10, 12, 15, 20, 30, 60 };

    private static final int[] invalidSecondsMinutes = new int[] { -1, -60, 61, 120, 11, 25 };

    private static final int[] validHours = new int[] { 1, 2, 3, 4, 6, 8, 24 };

    private static final int[] invalidHours = new int[] { -1, -24, 25, 5, 7, 48 };
}
