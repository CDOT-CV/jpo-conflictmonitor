package us.dot.its.jpo.conflictmonitor.monitor.models.events;

import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
@Document("CmSignalGroupAlignment")
@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
@Generated
public class SignalGroupAlignmentEvent extends Event{
    String source;
    long timestamp;
    Set<Integer> spatSignalGroupIds;
    Set<Integer> mapSignalGroupIds;

    public SignalGroupAlignmentEvent(){
        super("SignalGroupAlignment");
    }

}
