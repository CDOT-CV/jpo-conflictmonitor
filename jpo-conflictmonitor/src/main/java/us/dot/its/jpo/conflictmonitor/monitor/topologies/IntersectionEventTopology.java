package us.dot.its.jpo.conflictmonitor.monitor.topologies;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.kstream.Produced;

import us.dot.its.jpo.conflictmonitor.ConflictMonitorProperties;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.connection_of_travel.ConnectionOfTravelAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.connection_of_travel.ConnectionOfTravelParameters;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.lane_direction_of_travel.LaneDirectionOfTravelAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.lane_direction_of_travel.LaneDirectionOfTravelParameters;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.signal_state_vehicle_crosses.SignalStateVehicleCrossesAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.signal_state_vehicle_crosses.SignalStateVehicleCrossesParameters;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.signal_state_vehicle_stops.SignalStateVehicleStopsAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.signal_state_vehicle_stops.SignalStateVehicleStopsParameters;
import us.dot.its.jpo.conflictmonitor.monitor.models.VehicleEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.Intersection.Intersection;
import us.dot.its.jpo.conflictmonitor.monitor.models.Intersection.VehiclePath;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmTimestampExtractor;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.ConnectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.LaneDirectionOfTravelEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateStopEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.spat.SpatAggregator;
import us.dot.its.jpo.conflictmonitor.monitor.models.spat.SpatTimestampExtractor;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.model.OdeBsmData;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;

public class IntersectionEventTopology {

    public static String getBsmID(OdeBsmData value){
        return ((J2735Bsm)value.getPayload().getData()).getCoreData().getId();
    }

    public static BsmAggregator getBsmsByTimeVehicle(ReadOnlyWindowStore<String, OdeBsmData> bsmWindowStore, Instant start, Instant end, String id){

        Instant timeFrom = start.minusSeconds(60);
        Instant timeTo = start.plusSeconds(60);

        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();

        KeyValueIterator<Windowed<String>, OdeBsmData> bsmRange = bsmWindowStore.fetchAll(timeFrom, timeTo);

        BsmAggregator agg = new BsmAggregator();

        
        while(bsmRange.hasNext()){
            KeyValue<Windowed<String>, OdeBsmData> next = bsmRange.next();
            long ts = BsmTimestampExtractor.getBsmTimestamp(next.value);
            //System.out.println(getBsmID(next.value));
            if(startMillis <= ts && endMillis >= ts && getBsmID(next.value).equals(id)){
                agg.add(next.value);
            }
            
        }

        bsmRange.close();
        agg.sort();

        return agg;
    }

    public static SpatAggregator getSpatByTime(ReadOnlyWindowStore<String, ProcessedSpat> spatWindowStore, Instant start, Instant end){

        Instant timeFrom = start.minusSeconds(60);
        Instant timeTo = start.plusSeconds(60);

        long startMillis = start.toEpochMilli();
        long endMillis = end.toEpochMilli();

        KeyValueIterator<Windowed<String>, ProcessedSpat> spatRange = spatWindowStore.fetchAll(timeFrom, timeTo);

        //System.out.println("Start Millis: " + startMillis + "End Millis: " + endMillis);

        SpatAggregator spatAggregator = new SpatAggregator();
        while(spatRange.hasNext()){
            KeyValue<Windowed<String>, ProcessedSpat> next = spatRange.next();
            long ts = SpatTimestampExtractor.getSpatTimestamp(next.value);
            

            //if(startMillis <= ts && endMillis >= ts){ Add this back in later once geojson converter timestamps are fixed
                spatAggregator.add(next.value);
            //}
        }
        spatRange.close();
        spatAggregator.sort();

        return spatAggregator;
    }


    public static ProcessedMap getMap(ReadOnlyKeyValueStore<String, ProcessedMap> mapStore, String key){
        return (ProcessedMap) mapStore.get(key);
    }


    public static Topology build(
        ConflictMonitorProperties conflictMonitorProps, 
        ReadOnlyWindowStore<String, OdeBsmData> bsmWindowStore, 
        ReadOnlyWindowStore<String, ProcessedSpat> spatWindowStore, 
        ReadOnlyKeyValueStore<String, ProcessedMap> mapStore,
        LaneDirectionOfTravelAlgorithm laneDirectionOfTravelAlgorithm,
        LaneDirectionOfTravelParameters laneDirectionOfTravelParams,
        ConnectionOfTravelAlgorithm connectionOfTravelAlgorithm,
        ConnectionOfTravelParameters connectionOfTravelParams,
        SignalStateVehicleCrossesAlgorithm signalStateVehicleCrossesAlgorithm,
        SignalStateVehicleCrossesParameters signalStateVehicleCrossesParameters,
        SignalStateVehicleStopsAlgorithm signalStateVehicleStopsAlgorithm,
        SignalStateVehicleStopsParameters signalStateVehicleStopsParameters) {
        
        StreamsBuilder builder = new StreamsBuilder();

        
        KStream<String, BsmEvent> bsmEventStream = 
            builder.stream(
                conflictMonitorProps.getKafkaTopicCmBsmEvent(), 
                Consumed.with(
                    Serdes.String(),
                    JsonSerdes.BsmEvent())
                );


        // Join Spats, Maps and BSMS
        KStream<String, VehicleEvent> vehicleEventsStream = bsmEventStream.flatMap(
            (key, value)->{

                
                List<KeyValue<String, VehicleEvent>> result = new ArrayList<KeyValue<String, VehicleEvent>>();

                
                

                if(value.getStartingBsm() == null || value.getEndingBsm() == null){
                    System.out.println("Detected BSM Event is Missing Start or End BSM Exiting.");
                    return result;
                }

                String vehicleId = getBsmID(value.getStartingBsm());
                

                Instant firstBsmTime = Instant.ofEpochMilli(BsmTimestampExtractor.getBsmTimestamp(value.getStartingBsm()));
                Instant lastBsmTime = Instant.ofEpochMilli(BsmTimestampExtractor.getBsmTimestamp(value.getEndingBsm()));

                ProcessedMap map = null;
                BsmAggregator bsms = getBsmsByTimeVehicle(bsmWindowStore, firstBsmTime, lastBsmTime, vehicleId);
                SpatAggregator spats = getSpatByTime(spatWindowStore, firstBsmTime, lastBsmTime);

                

                if(spats.getSpats().size() > 0){
                    ProcessedSpat firstSpat = spats.getSpats().get(0);
                    String ip = firstSpat.getOriginIp();
                    int intersectionId = firstSpat.getIntersectionId();
                    // RsuIntersectionKey rsuIntersectionKey = new RsuIntersectionKey(ip, intersectionId);
                    // String mapLookupKey = rsuIntersectionKey.toString();
                    String mapLookupKey = "{\"rsuId\":\""+ip+"\",\"intersectionId\":"+intersectionId+"}";
                    // String mapLookupKey = ip +":"+ intersectionId;
                    System.out.println(mapLookupKey);
                    map = getMap(mapStore, mapLookupKey);

                    

                    if(map != null){
                        
                        Intersection intersection = Intersection.fromProcessedMap(map);
                        VehicleEvent event = new VehicleEvent(bsms, spats, intersection);

                        String vehicleEventKey = intersection.getIntersectionId() + "_" + vehicleId;
                        result.add(new KeyValue<>(vehicleEventKey, event));
    
                        
                    }else{
                        System.out.println("Map was Null");
                    }

                }


                System.out.println("Detected Vehicle Event");
                System.out.println("Vehicle ID: " + ((J2735Bsm)value.getStartingBsm().getPayload().getData()).getCoreData().getId());
                System.out.println("Captured Bsms:  " + bsms.getBsms().size());
                System.out.println("Captured Spats: " + spats.getSpats().size());
                return result;
            }
        );


        // Perform Analytics on Lane direction of Travel Events
        KStream<String, LaneDirectionOfTravelEvent> laneDirectionOfTravelEventStream = vehicleEventsStream.flatMap(
            (key, value)->{
                VehiclePath path = new VehiclePath(value.getBsms(), value.getIntersection());

                List<KeyValue<String, LaneDirectionOfTravelEvent>> result = new ArrayList<KeyValue<String, LaneDirectionOfTravelEvent>>();
                ArrayList<LaneDirectionOfTravelEvent> events = laneDirectionOfTravelAlgorithm.getLaneDirectionOfTravelEvents(laneDirectionOfTravelParams, path);
                
                for(LaneDirectionOfTravelEvent event: events){
                    result.add(new KeyValue<>(event.getKey(), event));
                }

                return result;
            }
        );

        laneDirectionOfTravelEventStream.to(
            conflictMonitorProps.getKafkatopicCmLaneDirectionOfTravelEvent(), 
            Produced.with(Serdes.String(),
                    JsonSerdes.LaneDirectionOfTravelEvent()));

        

        // Perform Analytics on Lane direction of Travel Events
        KStream<String, ConnectionOfTravelEvent> connectionTravelEventsStream = vehicleEventsStream.flatMap(
            (key, value)->{
                VehiclePath path = new VehiclePath(value.getBsms(), value.getIntersection());

                List<KeyValue<String, ConnectionOfTravelEvent>> result = new ArrayList<KeyValue<String, ConnectionOfTravelEvent>>();
                ConnectionOfTravelEvent event = connectionOfTravelAlgorithm.getConnectionOfTravelEvent(connectionOfTravelParams, path);
                if(event != null){
                    result.add(new KeyValue<>(event.getKey(), event));
                }
                return result;
            }
        );

        connectionTravelEventsStream.to(
            conflictMonitorProps.getKafkaTopicCmConnectionOfTravelEvent(), 
            Produced.with(Serdes.String(),
                    JsonSerdes.ConnectionOfTravelEvent()));


        // Perform Analytics of Signal State Vehicle Crossing Intersection
        KStream<String, SignalStateEvent> signalStateVehicleCrossingEventsStream = vehicleEventsStream.flatMap(
            (key, value)->{
                VehiclePath path = new VehiclePath(value.getBsms(), value.getIntersection());

                List<KeyValue<String, SignalStateEvent>> result = new ArrayList<KeyValue<String, SignalStateEvent>>();
                SignalStateEvent event = signalStateVehicleCrossesAlgorithm.getSignalStateEvent(signalStateVehicleCrossesParameters, path, value.getSpats());
                if(event != null){
                    result.add(new KeyValue<>(event.getKey(), event));
                }

                System.out.println("Signal State Event Vehicle Crossing: " + result.size());
                return result;
            }
        );

        signalStateVehicleCrossingEventsStream.to(
            conflictMonitorProps.getKafkaTopicCmSignalStateEvent(), 
            Produced.with(Serdes.String(),
                    JsonSerdes.SignalStateEvent()));



        // Perform Analytics of Signal State Vehicle Crossing Intersection
        KStream<String, SignalStateStopEvent> signalStateVehicleStopEventsStream = vehicleEventsStream.flatMap(
            (key, value)->{

                VehiclePath path = new VehiclePath(value.getBsms(), value.getIntersection());

                List<KeyValue<String, SignalStateStopEvent>> result = new ArrayList<KeyValue<String, SignalStateStopEvent>>();
                SignalStateStopEvent event = signalStateVehicleStopsAlgorithm.getSignalStateStopEvent(signalStateVehicleStopsParameters, path, value.getSpats());
                if(event != null){
                    result.add(new KeyValue<>(event.getKey(), event));
                }

                
                return result;
            }
        );

        signalStateVehicleStopEventsStream.to(
            conflictMonitorProps.getKafakTopicCmVehicleStopEvent(), 
            Produced.with(Serdes.String(),
                    JsonSerdes.SignalStateVehicleStopsEvent()));
 
        return builder.build();
    }
}
