package us.dot.its.jpo.conflictmonitor.monitor.algorithms.aggregation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ProcessingTimePeriod;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;


@Slf4j
public class AggregationParameters_AggTimePeriodTest {

    @ParameterizedTest
    @MethodSource("getParams")
    public void testAggTimePeriod(int interval, ChronoUnit intervalUnits, long timestampMs,
                                   long expectBeginTimestamp, long expectEndTimestamp) {
        var aggParams = new AggregationParameters();
        aggParams.setInterval(interval);
        aggParams.setIntervalUnits(intervalUnits);
        ProcessingTimePeriod timePeriod = aggParams.aggTimePeriod(timestampMs);
        assertThat(timePeriod, hasProperty("beginTimestamp", equalTo(expectBeginTimestamp)));
        assertThat(timePeriod, hasProperty("endTimestamp", equalTo(expectEndTimestamp)));
    }

    static Stream<Arguments> getParams() {
        var params = new ArrayList<Arguments>();
        addParams(params, 30, MINUTES, "10:15:00", "10:00:00", "10:30:00");
        // Prelim design details: "An event that occurs at the exact time boundary between two aggregation periods
        // will be associated with the later period."
        addParams(params, 30, MINUTES, "10:00:00", "10:00:00", "10:30:00");
        addParams(params, 30, MINUTES, "10:30:00", "10:30:00", "11:00:00");
        addParams(params, 30, MINUTES, "10:29:59", "10:00:00", "10:30:00");
        addParams(params, 30, SECONDS, "10:00:17", "10:00:00", "10:00:30");
        addParams(params, 30, SECONDS, "10:00:00", "10:00:00", "10:00:30");
        addParams(params, 30, SECONDS, "10:00:29", "10:00:00", "10:00:30");
        addParams(params, 30, SECONDS, "10:00:29.999", "10:00:00", "10:00:30");
        addParams(params, 20, SECONDS, "10:00:21", "10:00:20", "10:00:40");
        addParams(params, 20, SECONDS, "10:00:47", "10:00:40", "10:01:00");
        addParams(params, 1, HOURS, "10:00:00", "10:00:00", "11:00:00");
        addParams(params, 1, HOURS, "10:15:46", "10:00:00", "11:00:00");
        addParams(params, 1, HOURS, "10:46:11", "10:00:00", "11:00:00");
        addParams(params, 1, HOURS, "11:00:00", "11:00:00", "12:00:00");
        addParams(params, 8, HOURS, "07:00:00", "00:00:00", "08:00:00");
        addParams(params, 8, HOURS, "10:00:00", "08:00:00", "16:00:00");
        return params.stream();
    }

    private static void addParams(ArrayList<Arguments> params, int interval, ChronoUnit intervalUnits,
                                  String time, String expectBeginTime,
                                  String expectEndTime) {
        long timestampMs = getTimestamp(time);
        long expectBeginTimestamp = getTimestamp(expectBeginTime);
        long expectEndTimestamp = getTimestamp(expectEndTime);
        params.add(Arguments.of(interval, intervalUnits, timestampMs, expectBeginTimestamp, expectEndTimestamp));
    }

    private static long getTimestamp(String time) {
        String dtStr = String.format("2024-10-01T%sZ", time);
        final var formatter = DateTimeFormatter.ISO_DATE_TIME;
        return ZonedDateTime.parse(dtStr, formatter).toInstant().toEpochMilli();
    }

}
