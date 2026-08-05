package us.dot.its.jpo.conflictmonitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KafkaStreams.State;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.app_health.KafkaStreamsStateChangeEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.app_health.KafkaStreamsAnomalyNotification;
import us.dot.its.jpo.geojsonconverter.DateJsonMapper;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StateChangeHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class StateChangeHandlerTest {

    private static final Logger logger = LoggerFactory.getLogger(StateChangeHandlerTest.class);

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    CompletableFuture<SendResult<String, String>> mockSendResult;

    final String topic = "testTopic";
    final String notificationTopic = "testNotificationTopic";
    final String topology = "testTopology";
    
    @Captor
    ArgumentCaptor<String> eventCaptor;

    @Captor
    ArgumentCaptor<String> notificationKeyCaptor;

    @Captor
    ArgumentCaptor<String> notificationCaptor;

    final ObjectMapper mapper = DateJsonMapper.getInstance();

    static Stream<Arguments> getParams() {
        return Stream.of(
            Arguments.of(State.NOT_RUNNING, State.RUNNING),
            Arguments.of(State.RUNNING, State.ERROR),
            Arguments.of(State.RUNNING, State.REBALANCING)
        );
    }

    @ParameterizedTest
    @MethodSource("getParams")
    public void testOnChange(State oldState, State newState) throws JsonProcessingException {
        when(kafkaTemplate.send(eq(topic), eq(topology), anyString())).thenReturn(mockSendResult);
        // Only exercised when newState is ERROR; lenient to avoid unnecessary-stubbing failures for other parameters
        lenient().when(kafkaTemplate.send(eq(notificationTopic), anyString(), anyString())).thenReturn(mockSendResult);
       
        
        StateChangeHandler handler = new StateChangeHandler(kafkaTemplate, topology, topic, notificationTopic);
        assertThat(handler, notNullValue());
        
        handler.onChange(newState, oldState);
        
        // Verify that both an event and notification were sent
        verify(kafkaTemplate, times(1)).send(eq(topic), eq(topology), 
            eventCaptor.capture());
        
        
        String event = eventCaptor.getValue();
        assertThat(event, notNullValue());
        logger.info("Event: {}", event);
        
        // Deserialize and validate the event
        var eventObj = mapper.readValue(event, KafkaStreamsStateChangeEvent.class);
        assertThat(eventObj, notNullValue());
        assertThat(eventObj.getOldState(), equalTo(oldState.toString()));
        assertThat(eventObj.getNewState(), equalTo(newState.toString()));
        assertThat(eventObj.getTopology(), equalTo(topology));

        // Notification expected if new state is ERROR
        if (State.ERROR.equals(newState)) {
            verify(kafkaTemplate, times(1)).send(eq(notificationTopic), 
                                notificationKeyCaptor.capture(), notificationCaptor.capture());
            // Deserialize and validate the notification
            String notificationKey = notificationKeyCaptor.getValue();
            String notification = notificationCaptor.getValue();
            assertThat(notificationKey, notNullValue());
            assertThat(notification, notNullValue());
            logger.info("Notification Key: {}", notificationKey);
            logger.info("Notification: {}" , notification);
            var notificationObj = mapper.readValue(notification, KafkaStreamsAnomalyNotification.class);
            assertThat(notificationObj, notNullValue());
            assertThat(notificationObj.getUniqueId(), equalTo(notificationKey));
        } else {
            // Not an error, notification should not have been sent
            verify(kafkaTemplate, times(0)).send(eq(notificationTopic),
                                anyString(), anyString());
        }
    }

    
    
    
}
