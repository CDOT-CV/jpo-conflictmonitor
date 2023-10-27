package us.dot.its.jpo.conflictmonitor.monitor.topologies;

import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.BaseStreamsBuilder;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.BaseStreamsTopology;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.BaseTopologyBuilder;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.message_ingest.MessageIngestAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.message_ingest.MessageIngestParameters;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.message_ingest.MessageIngestStreamsAlgorithm;
import us.dot.its.jpo.conflictmonitor.monitor.models.VehicleEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmEventIntersectionKey;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmIntersectionKey;
import us.dot.its.jpo.conflictmonitor.monitor.models.bsm.BsmTimestampExtractor;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapBoundingBox;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.MapIndex;
import us.dot.its.jpo.conflictmonitor.monitor.models.map.store.MapSpatiallyIndexedStateStoreSupplier;
import us.dot.its.jpo.conflictmonitor.monitor.models.spat.SpatTimestampExtractor;
import us.dot.its.jpo.conflictmonitor.monitor.processors.DiagnosticProcessor;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIdPartitioner;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.LineString;
import us.dot.its.jpo.geojsonconverter.pojos.geojson.map.ProcessedMap;
import us.dot.its.jpo.geojsonconverter.pojos.spat.ProcessedSpat;
import us.dot.its.jpo.ode.model.OdeBsmData;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static us.dot.its.jpo.conflictmonitor.monitor.algorithms.message_ingest.MessageIngestConstants.DEFAULT_MESSAGE_INGEST_ALGORITHM;

@Component(DEFAULT_MESSAGE_INGEST_ALGORITHM)
public class MessageIngestTopology
        extends BaseStreamsBuilder<MessageIngestParameters>
        implements MessageIngestStreamsAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(MessageIngestTopology.class);
    private int count = 0;
    
    public StreamsBuilder buildTopology(StreamsBuilder builder) {

        //StreamsBuilder builder = new StreamsBuilder();
        
        /*
         * 
         * 
         *  BSM MESSAGES
         * 
         */

        //BSM Input Stream
        KStream<BsmIntersectionKey, OdeBsmData> bsmJsonStream =
            builder.stream(
                parameters.getBsmTopic(), 
                Consumed.with(
                    JsonSerdes.BsmIntersectionKey(),
                    JsonSerdes.OdeBsm())
                    .withTimestampExtractor(new BsmTimestampExtractor())
                );

        // bsmJsonStream.print(Printed.toSysOut());
        

        //Group up all of the BSM's based upon the new ID.
        KGroupedStream<BsmIntersectionKey, OdeBsmData> bsmKeyGroup =
                bsmJsonStream.groupByKey(Grouped.with(JsonSerdes.BsmIntersectionKey(), JsonSerdes.OdeBsm()));

        // KStream<BsmIntersectionKey, OdeBsmData> vehicleEventsStream = bsmKeyGroup.flatMap(
        //     (key, value)->{

        //     count +=1;
        //     System.out.println(count);
        //     System.out.println(key);

        //     List<KeyValue<BsmIntersectionKey, OdeBsmData>> result = new ArrayList<>();
        //     return result;

        // });

        // bsmKeyGroup.print(Printed.toSysOut());


        Map<String, String> loggingConfig = new HashMap<>();
        // Set the directory where state store logs will be stored
        // loggingConfig.put("directory", "/statestore.logs");
        // // Set the retention time for the state store logs
        // loggingConfig.put("retention.ms", "86400000");

        //Take the BSM's and Materialize them into a Temporal Time window. The length of the time window shouldn't matter much
        //but enables kafka to temporally query the records later. If there are duplicate keys, the more recent value is taken.
        var bsmWindowed = bsmKeyGroup.windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMillis(1), Duration.ofMillis(60000)))
        .reduce(
            (oldValue, newValue)->{
                System.out.println("Overwriting BSM");
                return newValue;
            },
            Materialized.<BsmIntersectionKey, OdeBsmData, WindowStore<Bytes, byte[]>>as(parameters.getBsmStoreName())
                    .withKeySerde(JsonSerdes.BsmIntersectionKey())
                    .withValueSerde(JsonSerdes.OdeBsm())
                    .withCachingDisabled()
                    // .withLoggingEnabled(loggingConfig)
                    .withLoggingDisabled()
                    .withRetention(Duration.ofMinutes(10))
        );

        // Check partition of windowed data
        bsmWindowed.toStream().process(() -> new DiagnosticProcessor<>("Windowed BSMs", logger));


         /*
          *
          *
          *  SPAT MESSAGES
          *
          */



        // SPaT Input Stream
        KStream<RsuIntersectionKey, ProcessedSpat> processedSpatStream =
            builder.stream(
                parameters.getSpatTopic(), 
                Consumed.with(
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey(),
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedSpat())
                    .withTimestampExtractor(new SpatTimestampExtractor())
                )   // Filter out null SPATs
                    .filter((key, value) -> {
                        if (value == null) {
                            logger.error("Encountered null SPAT");
                            return false;
                        } else {
                            return true;
                        }
                    });

        processedSpatStream.process(() -> new DiagnosticProcessor<>("ProcessedSpats", logger));

        // Group up all of the Spats's based upon the new key. Generally speaking this shouldn't change anything as the Spats's have unique keys
        KGroupedStream<RsuIntersectionKey, ProcessedSpat> spatKeyGroup =
                processedSpatStream.groupByKey(
                        Grouped.with(
                                us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey(),
                                us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedSpat()));



        // //Take the Spats's and Materialize them into a Temporal Time window. The length of the time window shouldn't matter much
        // //but enables kafka to temporally query the records later. If there are duplicate keys, the more recent value is taken.
        var spatWindowed = spatKeyGroup.windowedBy(TimeWindows.ofSizeAndGrace(Duration.ofMillis(1), Duration.ofMillis(10000)))
        .reduce(
            (oldValue, newValue)->{
                    return newValue;
            },
            Materialized.<RsuIntersectionKey, ProcessedSpat, WindowStore<Bytes, byte[]>>as(parameters.getSpatStoreName())
                    .withKeySerde(us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey())
                    .withValueSerde(us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedSpat())
                    .withCachingDisabled()
                    .withLoggingDisabled()
                    .withRetention(Duration.ofMinutes(5))
        );

        spatWindowed.toStream().process(() -> new DiagnosticProcessor<>("Windowed SPATs", logger));

        //
        //  MAP MESSAGES
        //

        // Create MAP table for bounding boxes
        builder.table(
                parameters.getMapTopic(),
                Consumed.with(
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey(),
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.ProcessedMapGeoJson()),
                    Materialized.<RsuIntersectionKey, ProcessedMap<LineString>, KeyValueStore<Bytes, byte[]>>as(parameters.getMapStoreName())
            ).mapValues(
                    map -> new MapBoundingBox(map)
            ).toStream()
                .to(parameters.getMapBoundingBoxTopic(),
                        Produced.with(
                                us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey(),
                                JsonSerdes.MapBoundingBox(),
                                new RsuIdPartitioner<RsuIntersectionKey, MapBoundingBox>()));

        // TODO: Repartition MAPs by Intersection/Region


        // Read Map Bounding Box Topic into GlobalKTable with spatially indexed state store
        builder.globalTable(parameters.getMapBoundingBoxTopic(),
                Consumed.with(
                        us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey(),
                        JsonSerdes.MapBoundingBox()),
                Materialized.as(new MapSpatiallyIndexedStateStoreSupplier(
                        parameters.getMapSpatialIndexStoreName(),
                        mapIndex,
                        parameters.getMapBoundingBoxTopic()))
        );





        return builder;
    }



    @Override
    protected Logger getLogger() {
        return logger;
    }




    @Override
    public ReadOnlyWindowStore<BsmIntersectionKey, OdeBsmData> getBsmWindowStore(KafkaStreams streams) {
        return streams.store(StoreQueryParameters.fromNameAndType(
            parameters.getBsmStoreName(), QueryableStoreTypes.windowStore()));
    }

    @Override
    public ReadOnlyWindowStore<RsuIntersectionKey, ProcessedSpat> getSpatWindowStore(KafkaStreams streams) {
        return streams.store(StoreQueryParameters.fromNameAndType(
            parameters.getSpatStoreName(), QueryableStoreTypes.windowStore()));
    }

    @Override
    public ReadOnlyKeyValueStore<RsuIntersectionKey, ProcessedMap<LineString>> getMapStore(KafkaStreams streams) {
        return streams.store(StoreQueryParameters.fromNameAndType(
            parameters.getMapStoreName(), QueryableStoreTypes.keyValueStore()));
    }




    private MapIndex mapIndex;


    @Override
    public MapIndex getMapIndex() {
        return mapIndex;
    }

    @Override
    public void setMapIndex(MapIndex mapIndex) {
        this.mapIndex = mapIndex;
    }

//    @Override
//    protected void validate() {
//        super.validate();
//        if (mapIndex == null) {
//            throw new IllegalArgumentException("MapIndex is not set");
//        }
//    }
}
