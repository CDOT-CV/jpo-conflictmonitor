package us.dot.its.jpo.conflictmonitor.monitor.serialization.deserialization;

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.stream.Stream;

public abstract class BaseGenericJsonDeserializerTest {

    public static Stream<Arguments> getParams() {
        var params = new ArrayList<Arguments>();
        params.add(Arguments.of(getConfig(10, "java.lang.Integer"), 10, "java.lang.Integer"));
        params.add(Arguments.of(getConfig(10.5, "java.lang.Double"), 10.5, "java.lang.Double"));
        params.add(Arguments.of(getConfig("\"test\"", "java.lang.String"), "test", "java.lang.String"));
        params.add(Arguments.of(getConfig(true, "java.lang.Boolean"), true, "java.lang.Boolean"));
        params.add(Arguments.of(getConfig(Long.MAX_VALUE, "java.lang.Long"), Long.MAX_VALUE, "java.lang.Long"));
        return params.stream();
    }

    // Test for method: public T deserialize(String topic, byte[] data)
    public abstract void testDeserialize(String configString, Object expectedValue, String expectedType);

    final static String configTemplate = """
        {
          "key": "key",
          "category": "category",
          "value": %s,
          "type": "%s",
          "units": "SECONDS",
          "description": "description",
          "updateType": "INTERSECTION"
        }
        """;


    static String getConfig(Object value, String type) {
        return String.format(configTemplate, value, type);
    }



}
