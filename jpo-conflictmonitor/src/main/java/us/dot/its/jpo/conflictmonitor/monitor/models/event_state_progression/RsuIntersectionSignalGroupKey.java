package us.dot.its.jpo.conflictmonitor.monitor.models.event_state_progression;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;

/**
 * Key that includes signal group
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class RsuIntersectionSignalGroupKey extends RsuIntersectionKey {

    int signalGroup;

    public RsuIntersectionSignalGroupKey() {}

    public RsuIntersectionSignalGroupKey(RsuIntersectionKey intersectionKey) {
        this.setIntersectionId(intersectionKey.getIntersectionId());
        this.setRegion(intersectionKey.getRegion());
        this.setRsuId(intersectionKey.getRsuId());
    }

    public RsuIntersectionSignalGroupKey(RsuIntersectionKey intersectionKey, int signalGroup) {
        this(intersectionKey);
        this.signalGroup = signalGroup;
    }
}
