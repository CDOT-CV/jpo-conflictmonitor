package us.dot.its.jpo.conflictmonitor.monitor.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.LineString;

import java.util.stream.Stream;

/**
 * Unit tests for {@link LaneConnection#alignInputLanes()} and {@link LaneConnection#getConnectingLineString()}.
 */
public class LaneConnection_AlignmentTest {

    @ParameterizedTest
    @MethodSource("getParams")
    public void testAlignInputLanes(LineString ingressPath, LineString egressPath,
        LineString expectedAlignedIngress, LineString expectedAlignedEgress) {
        LaneConnection laneConnection = new LaneConnection();
        laneConnection.setIngressPath(ingressPath);
        laneConnection.setEgressPath(egressPath);
        laneConnection.alignInputLanes();
        assertThat(laneConnection.getIngressPath(), equalTo(expectedAlignedIngress));
        assertThat(laneConnection.getEgressPath(), equalTo(expectedAlignedEgress));
    }

    @ParameterizedTest
    @MethodSource("getParams")
    public void testGetConnectingLineString(LineString ingressPath, LineString egressPath) {
        LaneConnection laneConnection = new LaneConnection();
        laneConnection.setIngressPath(ingressPath);
        laneConnection.setEgressPath(egressPath);
        laneConnection.alignInputLanes();
        laneConnection.getConnectingLineString();
        assertThat(laneConnection.getConnectingPath(), notNullValue());
        var connectingPath = laneConnection.getConnectingPath();
        assertThat(connectingPath.getCoordinates(), arrayWithSize(LaneConnection.DEFAULT_INTERPOLATION_POINTS + 2));
    }

    static Stream<Arguments> getParams() {
        return Stream.of(
            Arguments.of(getLineString(0, 1), getLineString(3, 4), getLineString(0, 1), getLineString(3, 4)),
            Arguments.of(getLineString(1, 0), getLineString(3, 4), getLineString(0, 1), getLineString(3, 4)),
            Arguments.of(getLineString(0, 1), getLineString(4, 3), getLineString(0, 1), getLineString(3, 4)),
            Arguments.of(getLineString(1, 0), getLineString(4, 3), getLineString(0, 1), getLineString(3, 4))
        );
    }

    // Get a line string with fixed x coordinate
    private static LineString getLineString(double y1, double y2) {
        var x = 2;
        var coordinates = new Coordinate[] {new CoordinateXY(x, y1), new CoordinateXY(x, y2)};
        var geometryFactory = JTSFactoryFinder.getGeometryFactory();
        return geometryFactory.createLineString(coordinates);
    }
}
