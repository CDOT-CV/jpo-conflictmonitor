package us.dot.its.jpo.conflictmonitor.monitor.processors;

import java.util.ArrayList;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.Point;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.bsm.ProcessedBsm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static us.dot.its.jpo.conflictmonitor.testutils.BsmTestUtils.validProcessedBsm;

/**
 * Unit tests for {@link BsmEventProcessor#validateBSM}
 */
@Slf4j
public class BsmEventProcessor_validateBSMTest {

    @ParameterizedTest(name = "{index}: {0}, expected: {2}")
    @MethodSource("getParams")
    public void testValidateBSM(String description, ProcessedBsm<Point> bsm, boolean expected) {
        log.info("{}:", description);
        var actual = BsmEventProcessor.validateBSM(bsm);
        assertThat(description, actual, equalTo(expected));
    }

    static Stream<Arguments> getParams() {

        var bsms = new ArrayList<Arguments>();

        bsms.add(Arguments.of("null BSM", null, false));

        final ProcessedBsm<Point> emptyBsm = new ProcessedBsm<Point>(null, null, null);
        bsms.add(Arguments.of("empty BSM", emptyBsm, false));


        final ProcessedBsm<Point> validBsm = validProcessedBsm();
        bsms.add(Arguments.of("Valid BSM", validBsm, true));

        final ProcessedBsm<Point> bsmWithNullId = validProcessedBsm();
        bsmWithNullId.getProperties().setId(null);
        bsms.add(Arguments.of("BSM with null id", bsmWithNullId, false));

        final ProcessedBsm<Point> bsmWithNullSecMark = validProcessedBsm();
        bsmWithNullSecMark.getProperties().setSecMark(null);
        bsms.add(Arguments.of("BSM with null secMark", bsmWithNullSecMark, false));

        final ProcessedBsm<Point> bsmWithNullSpeed = validProcessedBsm();
        bsmWithNullSpeed.getProperties().setSpeed(null);
        bsms.add(Arguments.of("BSM with null speed", bsmWithNullSpeed, false));

        final ProcessedBsm<Point> bsmWithNullHeading = validProcessedBsm();
        bsmWithNullHeading.getProperties().setHeading(null);
        bsms.add(Arguments.of("BSM with null heading", bsmWithNullHeading, false));

        final ProcessedBsm<Point> bsmWithNullSource = validProcessedBsm();
        bsmWithNullSource.getProperties().setOriginIp(null);
        bsmWithNullSource.getProperties().setLogName(null);
        bsms.add(Arguments.of("BSM with null source", bsmWithNullSource, false));


        final ProcessedBsm<Point> bsmWithNullRecordGeneratedAt = validProcessedBsm();
        bsmWithNullRecordGeneratedAt.getProperties().setTimeStamp(null);
        bsms.add(Arguments.of("BSM with null recordGeneratedAt", bsmWithNullRecordGeneratedAt, false));

        final ProcessedBsm<Point> bsmWithNullOdeReceivedAt = validProcessedBsm();
        bsmWithNullOdeReceivedAt.getProperties().setOdeReceivedAt(null);
        bsms.add(Arguments.of("BSM with null odeReceivedAt", bsmWithNullOdeReceivedAt, false));


        return bsms.stream();
    }



    
}
