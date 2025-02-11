package us.dot.its.jpo.conflictmonitor.monitor.notifications;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Test;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalGroupAlignmentEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.events.SignalStateConflictEvent;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalGroupAlignmentNotification;
import us.dot.its.jpo.conflictmonitor.monitor.models.notifications.SignalStateConflictNotification;
import us.dot.its.jpo.conflictmonitor.monitor.serialization.JsonSerdes;
import us.dot.its.jpo.conflictmonitor.monitor.topologies.MapSpatMessageAssessmentTopology;
import us.dot.its.jpo.conflictmonitor.monitor.algorithms.map_spat_message_assessment.MapSpatMessageAssessmentParameters;
import us.dot.its.jpo.geojsonconverter.partitioner.RsuIntersectionKey;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

// @SpringBootTest
// @RunWith(SpringRunner.class)
public class SignalGroupAlignmentNotificationTopologyTest {
    String kafkaTopicIntersectionReferenceAlignmentEvents = "topic.CmIntersectionReferenceAlignmentEvents";
    String kafkaTopicIntersectionReferenceAlignmentNotifications = "topic.CmIntersectionReferenceAlignmentNotifications";
    String kafkaTopicMapInputTopicName = "topic.ProcessedMap";
    String kafkaTopicSpatInputTopicName = "topic.ProcessedSpat";
    String kafkaTopicSignalGroupAlignmentEventTopicName = "topic.CmSignalGroupAlignmentEvents";
    String kafkaTopicSignalStateConflictEventTopicName = "topic.CmSignalStateConflictEvents";
    String kafkaTopicSignalGroupAlignmentNotificationTopicName = "topic.CmSignalGroupAlignmentNotifications";
    String kafkaTopicSignalStateConflictNotificationTopicName = "topic.CmSignalStateConflictNotification";



    String processedSpatUnaligned = """
            {
                "messageType": "SPAT",
                "odeReceivedAt": "2022-06-17T19:15:13.671068Z",
                "originIp": "10.11.81.12",
                "intersectionId": 12109,
                "cti4501Conformant": false,
                "validationMessages": [
                    {
                        "message": "$.metadata.@class: is missing but it is required",
                        "jsonPath": "$.metadata",
                        "schemaPath": "#/$defs/OdeSpatMetadata/required"
                    },
                    {
                        "message": "$.payload.@class: is missing but it is required",
                        "jsonPath": "$.payload",
                        "schemaPath": "#/$defs/OdeSpatPayload/required"
                    },
                    {
                        "message": "$.payload.data.timeStamp: null found, integer expected",
                        "jsonPath": "$.payload.data.timeStamp",
                        "schemaPath": "#/$defs/J2735MinuteOfTheYear/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].id.region: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].id.region",
                        "schemaPath": "#/$defs/J2735RoadRegulatorID/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime",
                        "schemaPath": "#/$defs/J2735TimeMark/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime",
                        "schemaPath": "#/$defs/J2735TimeMark/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime",
                        "schemaPath": "#/$defs/J2735TimeMark/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime",
                        "schemaPath": "#/$defs/J2735TimeMark/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime",
                        "schemaPath": "#/$defs/J2735TimeMark/type"
                    },
                    {
                        "message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected",
                        "jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime",
                        "schemaPath": "#/$defs/J2735TimeMark/type"
                    }
                ],
                "revision": 0,
                "status": {
                    "manualControlIsEnabled": true,
                    "stopTimeIsActivated": false,
                    "failureFlash": true,
                    "preemptIsActive": false,
                    "signalPriorityIsActive": false,
                    "fixedTimeOperation": false,
                    "trafficDependentOperation": false,
                    "standbyOperation": false,
                    "failureMode": false,
                    "off": false,
                    "recentMAPmessageUpdate": false,
                    "recentChangeInMAPassignedLanesIDsUsed": false,
                    "noValidMAPisAvailableAtThisTime": false,
                    "noValidSPATisAvailableAtThisTime": false
                },
                "utcTimeStamp": "2022-06-17T19:15:13.745Z",
                "enabledLanes": [],
                "states": [
                    {
                        "signalGroup": 3,
                        "stateTimeSpeed": [
                            {
                                "eventState": "STOP_AND_REMAIN",
                                "timing": {
                                    "startTime": null,
                                    "minEndTime": "2022-06-17T19:15:12.5Z",
                                    "maxEndTime": "2022-06-17T19:15:12.5Z",
                                    "likelyTime": null,
                                    "confidence": null,
                                    "nextTime": null
                                }
                            }
                        ]
                    },
                    {
                        "signalGroup": 5,
                        "stateTimeSpeed": [
                            {
                                "eventState": "STOP_AND_REMAIN",
                                "timing": {
                                    "startTime": null,
                                    "minEndTime": "2022-06-17T19:15:12.2Z",
                                    "maxEndTime": "2022-06-17T19:15:12.2Z",
                                    "likelyTime": null,
                                    "confidence": null,
                                    "nextTime": null
                                }
                            }
                        ]
                    },
                    {
                        "signalGroup": 7,
                        "stateTimeSpeed": [
                            {
                                "eventState": "STOP_AND_REMAIN",
                                "timing": {
                                    "startTime": null,
                                    "minEndTime": "2022-06-17T19:15:12.5Z",
                                    "maxEndTime": "2022-06-17T19:15:12.5Z",
                                    "likelyTime": null,
                                    "confidence": null,
                                    "nextTime": null
                                }
                            }
                        ]
                    }
                ]
            }""";

     String processedSpatConflict = """
             {
             	"messageType": "SPAT",
             	"odeReceivedAt": "2022-06-17T19:15:13.671068Z",
             	"originIp": "10.11.81.12",
             	"intersectionId": 12109,
             	"cti4501Conformant": false,
             	"validationMessages": [
             		{
             			"message": "$.metadata.@class: is missing but it is required",
             			"jsonPath": "$.metadata",
             			"schemaPath": "#/$defs/OdeSpatMetadata/required"
             		},
             		{
             			"message": "$.payload.@class: is missing but it is required",
             			"jsonPath": "$.payload",
             			"schemaPath": "#/$defs/OdeSpatPayload/required"
             		},
             		{
             			"message": "$.payload.data.timeStamp: null found, integer expected",
             			"jsonPath": "$.payload.data.timeStamp",
             			"schemaPath": "#/$defs/J2735MinuteOfTheYear/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].id.region: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].id.region",
             			"schemaPath": "#/$defs/J2735RoadRegulatorID/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.startTime",
             			"schemaPath": "#/$defs/J2735TimeMark/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[0].state_time_speed.movementEventList[0].timing.nextTime",
             			"schemaPath": "#/$defs/J2735TimeMark/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.startTime",
             			"schemaPath": "#/$defs/J2735TimeMark/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[1].state_time_speed.movementEventList[0].timing.nextTime",
             			"schemaPath": "#/$defs/J2735TimeMark/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.startTime",
             			"schemaPath": "#/$defs/J2735TimeMark/type"
             		},
             		{
             			"message": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime: null found, integer expected",
             			"jsonPath": "$.payload.data.intersectionStateList.intersectionStatelist[0].states.movementList[2].state_time_speed.movementEventList[0].timing.nextTime",
             			"schemaPath": "#/$defs/J2735TimeMark/type"
             		}
             	],
             	"revision": 0,
             	"status": {
             		"manualControlIsEnabled": true,
             		"stopTimeIsActivated": false,
             		"failureFlash": true,
             		"preemptIsActive": false,
             		"signalPriorityIsActive": false,
             		"fixedTimeOperation": false,
             		"trafficDependentOperation": false,
             		"standbyOperation": false,
             		"failureMode": false,
             		"off": false,
             		"recentMAPmessageUpdate": false,
             		"recentChangeInMAPassignedLanesIDsUsed": false,
             		"noValidMAPisAvailableAtThisTime": false,
             		"noValidSPATisAvailableAtThisTime": false
             	},
             	"utcTimeStamp": "2022-06-17T19:15:13.745Z",
             	"enabledLanes": [],
             	"states": [
             		{
             			"signalGroup": 2,
             			"stateTimeSpeed": [
             				{
             					"eventState": "PROTECTED_MOVEMENT_ALLOWED",
             					"timing": {
             						"startTime": null,
             						"minEndTime": "2022-06-17T19:15:12.5Z",
             						"maxEndTime": "2022-06-17T19:15:12.5Z",
             						"likelyTime": null,
             						"confidence": null,
             						"nextTime": null
             					}
             				}
             			]
             		},
             		{
             			"signalGroup": 4,
             			"stateTimeSpeed": [
             				{
             					"eventState": "STOP_AND_REMAIN",
             					"timing": {
             						"startTime": null,
             						"minEndTime": "2022-06-17T19:15:12.2Z",
             						"maxEndTime": "2022-06-17T19:15:12.2Z",
             						"likelyTime": null,
             						"confidence": null,
             						"nextTime": null
             					}
             				}
             			]
             		},
             		{
             			"signalGroup": 6,
             			"stateTimeSpeed": [
             				{
             					"eventState": "PROTECTED_MOVEMENT_ALLOWED",
             					"timing": {
             						"startTime": null,
             						"minEndTime": "2022-06-17T19:15:12.5Z",
             						"maxEndTime": "2022-06-17T19:15:12.5Z",
             						"likelyTime": null,
             						"confidence": null,
             						"nextTime": null
             					}
             				}
             			]
             		}
             	]
             }
             """;



    final String processedMap = """
            {
            	"properties": {
            		"messageType": "MAP",
            		"odeReceivedAt": "2022-06-17T19:02:12.333377Z",
            		"originIp": "10.11.81.12",
            		"intersectionId": 12109,
            		"msgIssueRevision": 2,
            		"revision": 2,
            		"refPoint": {
            			"latitude": 39.5880413,
            			"longitude": -105.0908854,
            			"elevation": 1691.0
            		},
            		"cti4501Conformant": false,
            		"validationMessages": [
            			{
            				"message": "$.metadata.@class: is missing but it is required",
            				"jsonPath": "$.metadata",
            				"schemaPath": "#/$defs/OdeMapMetadata/required"
            			},
            			{
            				"message": "$.payload.@class: is missing but it is required",
            				"jsonPath": "$.payload",
            				"schemaPath": "#/$defs/OdeMapPayload/required"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].id.region: null found, integer expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].id.region",
            				"schemaPath": "#/$defs/J2735RoadRegulatorID/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[3].connectsTo: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[3].connectsTo",
            				"schemaPath": "#/$defs/J2735ConnectsToList_Wrapper/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[3].maneuvers: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[3].maneuvers",
            				"schemaPath": "#/$defs/J2735AllowedManeuvers/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[4].connectsTo: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[4].connectsTo",
            				"schemaPath": "#/$defs/J2735ConnectsToList_Wrapper/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[4].maneuvers: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[4].maneuvers",
            				"schemaPath": "#/$defs/J2735AllowedManeuvers/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[5].connectsTo: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[5].connectsTo",
            				"schemaPath": "#/$defs/J2735ConnectsToList_Wrapper/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[5].maneuvers: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[5].maneuvers",
            				"schemaPath": "#/$defs/J2735AllowedManeuvers/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[6].connectsTo: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[6].connectsTo",
            				"schemaPath": "#/$defs/J2735ConnectsToList_Wrapper/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[6].maneuvers: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[6].maneuvers",
            				"schemaPath": "#/$defs/J2735AllowedManeuvers/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[8].connectsTo.connectsTo[0].signalGroup: null found, integer expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[8].connectsTo.connectsTo[0].signalGroup",
            				"schemaPath": "#/$defs/J2735SignalGroupID/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[12].connectsTo.connectsTo[0].signalGroup: null found, integer expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[12].connectsTo.connectsTo[0].signalGroup",
            				"schemaPath": "#/$defs/J2735SignalGroupID/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[13].connectsTo: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[13].connectsTo",
            				"schemaPath": "#/$defs/J2735ConnectsToList_Wrapper/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[13].maneuvers: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[13].maneuvers",
            				"schemaPath": "#/$defs/J2735AllowedManeuvers/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[14].connectsTo: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[14].connectsTo",
            				"schemaPath": "#/$defs/J2735ConnectsToList_Wrapper/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[14].maneuvers: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].laneSet.GenericLane[14].maneuvers",
            				"schemaPath": "#/$defs/J2735AllowedManeuvers/type"
            			},
            			{
            				"message": "$.payload.data.intersections.intersectionGeometry[0].speedLimits: null found, object expected",
            				"jsonPath": "$.payload.data.intersections.intersectionGeometry[0].speedLimits",
            				"schemaPath": "#/$defs/J2735SpeedLimitList_Wrapper/type"
            			}
            		],
            		"laneWidth": 366,
            		"mapSource": "RSU",
            		"timeStamp": "2022-06-17T19:02:12.333377Z"
            	},
            	"mapFeatureCollection": {
            		"type": "FeatureCollection",
            		"features": [
            			{
            				"type": "Feature",
            				"id": 1,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907089,
            							39.587905
            						],
            						[
            							-105.0906245,
            							39.5876246
            						],
            						[
            							-105.0905203,
            							39.587281
            						],
            						[
            							-105.0904383,
            							39.5870554
            						],
            						[
            							-105.0903588,
            							39.5868383
            						],
            						[
            							-105.0902622,
            							39.5865865
            						],
            						[
            							-105.0901249,
            							39.5862612
            						],
            						[
            							-105.0900451,
            							39.5860819
            						],
            						[
            							-105.0899283,
            							39.5858283
            						],
            						[
            							-105.0898739,
            							39.5857117
            						],
            						[
            							-105.0895814,
            							39.5851569
            						],
            						[
            							-105.0888764,
            							39.5839527
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								1511,
            								-1514
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								723,
            								-3116
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								892,
            								-3818
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								702,
            								-2507
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								681,
            								-2412
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								827,
            								-2798
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								1176,
            								-3614
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								683,
            								-1992
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								1000,
            								-2818
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								466,
            								-1295
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								2505,
            								-6164
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								6037,
            								-13380
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 70
            						}
            					],
            					"laneId": 1,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 1,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": true,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": false,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 15,
            								"maneuver": {
            									"maneuverStraightAllowed": true,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": false,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 2,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 2,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907462,
            							39.5878956
            						],
            						[
            							-105.090652,
            							39.5875596
            						],
            						[
            							-105.090534,
            							39.5871793
            						],
            						[
            							-105.0903457,
            							39.5866864
            						],
            						[
            							-105.0902123,
            							39.5863581
            						],
            						[
            							-105.0900802,
            							39.5860572
            						],
            						[
            							-105.0898164,
            							39.5855019
            						],
            						[
            							-105.0895409,
            							39.5849856
            						],
            						[
            							-105.088922,
            							39.5839259
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								1192,
            								-1619
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								807,
            								-3733
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 30
            						},
            						{
            							"delta": [
            								1010,
            								-4226
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								1612,
            								-5477
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 30
            						},
            						{
            							"delta": [
            								1142,
            								-3648
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								1131,
            								-3343
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								2259,
            								-6170
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 30
            						},
            						{
            							"delta": [
            								2359,
            								-5737
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 30
            						},
            						{
            							"delta": [
            								5300,
            								-11774
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 50
            						}
            					],
            					"laneId": 2,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 1,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": true,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": false,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 14,
            								"maneuver": {
            									"maneuverStraightAllowed": true,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": false,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 2,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 3,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907914,
            							39.5878879
            						],
            						[
            							-105.090747,
            							39.5877247
            						],
            						[
            							-105.0906498,
            							39.5874141
            						],
            						[
            							-105.0906262,
            							39.5873356
            						],
            						[
            							-105.0905865,
            							39.5872922
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								805,
            								-1704
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								380,
            								-1813
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								832,
            								-3451
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 30
            						},
            						{
            							"delta": [
            								202,
            								-872
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								340,
            								-482
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						}
            					],
            					"laneId": 3,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 1,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": false,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": true,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 10,
            								"maneuver": {
            									"maneuverStraightAllowed": false,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": true,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 2,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 6,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910008,
            							39.5878477
            						],
            						[
            							-105.0909927,
            							39.5878181
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-988,
            								-2151
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 20
            						},
            						{
            							"delta": [
            								69,
            								-329
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 6,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 2,
            					"ingressApproach": 0,
            					"ingressPath": false,
            					"egressPath": true
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 5,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.090959,
            							39.5878557
            						],
            						[
            							-105.0909501,
            							39.5878218
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-630,
            								-2062
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								76,
            								-377
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						}
            					],
            					"laneId": 5,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 2,
            					"ingressApproach": 0,
            					"ingressPath": false,
            					"egressPath": true
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 4,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.090914,
            							39.5878612
            						],
            						[
            							-105.0909051,
            							39.5878298
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-245,
            								-2001
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								76,
            								-349
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 4,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 2,
            					"ingressApproach": 0,
            					"ingressPath": false,
            					"egressPath": true
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 10,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911626,
            							39.5880622
            						],
            						[
            							-105.0912043,
            							39.5880536
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-2374,
            								232
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-357,
            								-96
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 10,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 4,
            					"ingressApproach": 0,
            					"ingressPath": false,
            					"egressPath": true
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 8,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911477,
            							39.587995
            						],
            						[
            							-105.0914565,
            							39.5879427
            						],
            						[
            							-105.0917937,
            							39.5879029
            						],
            						[
            							-105.0922121,
            							39.5878724
            						],
            						[
            							-105.0926509,
            							39.5878748
            						],
            						[
            							-105.0930303,
            							39.5879073
            						],
            						[
            							-105.0932697,
            							39.5879503
            						],
            						[
            							-105.0937243,
            							39.5880569
            						],
            						[
            							-105.0940309,
            							39.5881258
            						],
            						[
            							-105.0943257,
            							39.5881804
            						],
            						[
            							-105.094592,
            							39.5882097
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-2246,
            								-514
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-2644,
            								-581
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-2887,
            								-442
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-3583,
            								-339
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-3757,
            								27
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-3249,
            								361
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-2050,
            								478
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-3893,
            								1184
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-2625,
            								766
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-2524,
            								607
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-2280,
            								325
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						}
            					],
            					"laneId": 8,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 3,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": false,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": true,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 15,
            								"maneuver": {
            									"maneuverStraightAllowed": false,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": true,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 4,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 7,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911442,
            							39.5879589
            						],
            						[
            							-105.0914154,
            							39.5879165
            						],
            						[
            							-105.0916346,
            							39.5878851
            						],
            						[
            							-105.0918433,
            							39.5878639
            						],
            						[
            							-105.0921546,
            							39.5878547
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-2216,
            								-915
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-2322,
            								-471
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-1877,
            								-349
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-1787,
            								-235
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-2666,
            								-102
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						}
            					],
            					"laneId": 7,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 3,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": false,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": false,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": true,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 6,
            								"maneuver": {
            									"maneuverStraightAllowed": false,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": false,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": true,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": null,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 9,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911534,
            							39.5880261
            						],
            						[
            							-105.091436,
            							39.5879812
            						],
            						[
            							-105.0916658,
            							39.5879507
            						],
            						[
            							-105.091881,
            							39.5879277
            						],
            						[
            							-105.0921287,
            							39.5878972
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-2295,
            								-169
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-2420,
            								-499
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-1968,
            								-339
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-1843,
            								-256
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-2121,
            								-339
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 9,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 3,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": false,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": true,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 14,
            								"maneuver": {
            									"maneuverStraightAllowed": false,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": true,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 4,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 12,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910447,
            							39.5881948
            						],
            						[
            							-105.0911481,
            							39.5886317
            						],
            						[
            							-105.091196,
            							39.588862
            						],
            						[
            							-105.0912349,
            							39.5890282
            						],
            						[
            							-105.0912722,
            							39.5893202
            						],
            						[
            							-105.0913306,
            							39.5897261
            						],
            						[
            							-105.0913695,
            							39.5900324
            						],
            						[
            							-105.0914008,
            							39.5903008
            						],
            						[
            							-105.0914893,
            							39.5913099
            						],
            						[
            							-105.091527,
            							39.5923157
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-1364,
            								1705
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-885,
            								4854
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -30
            						},
            						{
            							"delta": [
            								-410,
            								2559
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-333,
            								1847
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-319,
            								3244
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -20
            						},
            						{
            							"delta": [
            								-500,
            								4510
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-333,
            								3403
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -30
            						},
            						{
            							"delta": [
            								-268,
            								2982
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-758,
            								11212
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -30
            						},
            						{
            							"delta": [
            								-323,
            								11176
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -70
            						}
            					],
            					"laneId": 12,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 5,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": true,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": false,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 5,
            								"maneuver": {
            									"maneuverStraightAllowed": true,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": false,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 6,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 13,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910013,
            							39.5881975
            						],
            						[
            							-105.0911059,
            							39.5886309
            						],
            						[
            							-105.091144,
            							39.5888313
            						],
            						[
            							-105.0911829,
            							39.5890442
            						],
            						[
            							-105.0912308,
            							39.5893169
            						],
            						[
            							-105.0912689,
            							39.5895877
            						],
            						[
            							-105.0913005,
            							39.5898143
            						],
            						[
            							-105.0913313,
            							39.5900714
            						],
            						[
            							-105.0913597,
            							39.5902968
            						],
            						[
            							-105.0914461,
            							39.5913017
            						],
            						[
            							-105.0914756,
            							39.592324
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-992,
            								1735
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-896,
            								4816
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -30
            						},
            						{
            							"delta": [
            								-326,
            								2227
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-333,
            								2366
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-410,
            								3030
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -20
            						},
            						{
            							"delta": [
            								-326,
            								3009
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-271,
            								2518
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-264,
            								2857
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -20
            						},
            						{
            							"delta": [
            								-243,
            								2504
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						},
            						{
            							"delta": [
            								-740,
            								11165
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -30
            						},
            						{
            							"delta": [
            								-253,
            								11359
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -70
            						}
            					],
            					"laneId": 13,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 5,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": true,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": false,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": false,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 4,
            								"maneuver": {
            									"maneuverStraightAllowed": true,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": false,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": false,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": 6,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 11,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910891,
            							39.5881859
            						],
            						[
            							-105.0911549,
            							39.5884681
            						],
            						[
            							-105.091196,
            							39.5886783
            						],
            						[
            							-105.091222,
            							39.5888049
            						],
            						[
            							-105.0912401,
            							39.5889649
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								-1744,
            								1607
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-563,
            								3136
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -20
            						},
            						{
            							"delta": [
            								-352,
            								2336
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-223,
            								1407
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": 10
            						},
            						{
            							"delta": [
            								-155,
            								1778
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 11,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 0,
            					"ingressApproach": 5,
            					"ingressPath": true,
            					"egressPath": false,
            					"maneuvers": {
            						"maneuverStraightAllowed": false,
            						"maneuverNoStoppingAllowed": false,
            						"goWithHalt": false,
            						"maneuverLeftAllowed": false,
            						"maneuverUTurnAllowed": false,
            						"maneuverLeftTurnOnRedAllowed": false,
            						"reserved1": false,
            						"maneuverRightAllowed": true,
            						"maneuverLaneChangeAllowed": false,
            						"yieldAllwaysRequired": false,
            						"maneuverRightTurnOnRedAllowed": false,
            						"caution": false
            					},
            					"connectsTo": [
            						{
            							"connectingLane": {
            								"lane": 10,
            								"maneuver": {
            									"maneuverStraightAllowed": false,
            									"maneuverNoStoppingAllowed": false,
            									"goWithHalt": false,
            									"maneuverLeftAllowed": false,
            									"maneuverUTurnAllowed": false,
            									"maneuverLeftTurnOnRedAllowed": false,
            									"reserved1": false,
            									"maneuverRightAllowed": true,
            									"maneuverLaneChangeAllowed": false,
            									"yieldAllwaysRequired": false,
            									"maneuverRightTurnOnRedAllowed": false,
            									"caution": false
            								}
            							},
            							"remoteIntersection": null,
            							"signalGroup": null,
            							"userClass": null,
            							"connectionID": 1
            						}
            					]
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 14,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0908389,
            							39.5882151
            						],
            						[
            							-105.0908478,
            							39.5882471
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								398,
            								1931
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -10
            						},
            						{
            							"delta": [
            								-76,
            								356
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 14,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 6,
            					"ingressApproach": 0,
            					"ingressPath": false,
            					"egressPath": true
            				}
            			},
            			{
            				"type": "Feature",
            				"id": 15,
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907875,
            							39.58822
            						],
            						[
            							-105.0907979,
            							39.5882514
            						]
            					]
            				},
            				"properties": {
            					"nodes": [
            						{
            							"delta": [
            								838,
            								1985
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": -20
            						},
            						{
            							"delta": [
            								-89,
            								349
            							],
            							"stopLine": null,
            							"dwidth": null,
            							"delevation": null
            						}
            					],
            					"laneId": 15,
            					"sharedWith": {
            						"busVehicleTraffic": false,
            						"trackedVehicleTraffic": false,
            						"individualMotorizedVehicleTraffic": false,
            						"taxiVehicleTraffic": false,
            						"overlappingLaneDescriptionProvided": false,
            						"cyclistVehicleTraffic": false,
            						"otherNonMotorizedTrafficTypes": false,
            						"multipleLanesTreatedAsOneLane": false,
            						"pedestrianTraffic": false,
            						"pedestriansTraffic": false
            					},
            					"egressApproach": 6,
            					"ingressApproach": 0,
            					"ingressPath": false,
            					"egressPath": true
            				}
            			}
            		]
            	},
            	"connectingLanesFeatureCollection": {
            		"type": "FeatureCollection",
            		"features": [
            			{
            				"type": "Feature",
            				"id": "1-15",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907089,
            							39.587905
            						],
            						[
            							-105.0907875,
            							39.58822
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 2,
            					"ingressLaneId": 1,
            					"egressLaneId": 15
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "2-14",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907462,
            							39.5878956
            						],
            						[
            							-105.0908389,
            							39.5882151
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 2,
            					"ingressLaneId": 2,
            					"egressLaneId": 14
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "3-10",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0907914,
            							39.5878879
            						],
            						[
            							-105.0911626,
            							39.5880622
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 2,
            					"ingressLaneId": 3,
            					"egressLaneId": 10
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "8-15",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911477,
            							39.587995
            						],
            						[
            							-105.0907875,
            							39.58822
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 4,
            					"ingressLaneId": 8,
            					"egressLaneId": 15
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "7-6",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911442,
            							39.5879589
            						],
            						[
            							-105.0910008,
            							39.5878477
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": null,
            					"ingressLaneId": 7,
            					"egressLaneId": 6
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "9-14",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0911534,
            							39.5880261
            						],
            						[
            							-105.0908389,
            							39.5882151
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 4,
            					"ingressLaneId": 9,
            					"egressLaneId": 14
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "12-5",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910447,
            							39.5881948
            						],
            						[
            							-105.090959,
            							39.5878557
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 6,
            					"ingressLaneId": 12,
            					"egressLaneId": 5
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "13-4",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910013,
            							39.5881975
            						],
            						[
            							-105.090914,
            							39.5878612
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": 6,
            					"ingressLaneId": 13,
            					"egressLaneId": 4
            				}
            			},
            			{
            				"type": "Feature",
            				"id": "11-10",
            				"geometry": {
            					"type": "LineString",
            					"coordinates": [
            						[
            							-105.0910891,
            							39.5881859
            						],
            						[
            							-105.0911626,
            							39.5880622
            						]
            					]
            				},
            				"properties": {
            					"signalGroupId": null,
            					"ingressLaneId": 11,
            					"egressLaneId": 10
            				}
            			}
            		]
            	}
            }
            """;


    @Test
    public void testTopology() {

        MapSpatMessageAssessmentTopology mapSpat = new MapSpatMessageAssessmentTopology();
        MapSpatMessageAssessmentParameters parameters = new MapSpatMessageAssessmentParameters();

        parameters.setDebug(false);
        parameters.setIntersectionReferenceAlignmentEventTopicName(kafkaTopicIntersectionReferenceAlignmentEvents);
        parameters.setIntersectionReferenceAlignmentNotificationTopicName(kafkaTopicIntersectionReferenceAlignmentNotifications);
        parameters.setMapInputTopicName(kafkaTopicMapInputTopicName);
        parameters.setSpatInputTopicName(kafkaTopicSpatInputTopicName);
        parameters.setSignalGroupAlignmentEventTopicName(kafkaTopicSignalGroupAlignmentEventTopicName);
        parameters.setSignalStateConflictEventTopicName(kafkaTopicSignalStateConflictEventTopicName);
        parameters.setSignalGroupAlignmentNotificationTopicName(kafkaTopicSignalGroupAlignmentNotificationTopicName);
        parameters.setSignalStateConflictNotificationTopicName(kafkaTopicSignalStateConflictNotificationTopicName);

        mapSpat.setParameters(parameters);

        Topology topology = mapSpat.buildTopology();
        

        try (TopologyTestDriver driver = new TopologyTestDriver(topology)) {
            
            
            TestInputTopic<RsuIntersectionKey, String> inputMapTopic = driver.createInputTopic(
                kafkaTopicMapInputTopicName,
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey().serializer(),
                    Serdes.String().serializer());

            TestInputTopic<RsuIntersectionKey, String> inputSpatTopic = driver.createInputTopic(
                kafkaTopicSpatInputTopicName,
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey().serializer(),
                    Serdes.String().serializer());


            TestOutputTopic<RsuIntersectionKey, SignalGroupAlignmentNotification> outputNotificationTopic = driver.createOutputTopic(
                kafkaTopicSignalGroupAlignmentNotificationTopicName,
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey().deserializer(),
                    JsonSerdes.SignalGroupAlignmentNotification().deserializer());


            final String rsuIp = "10.11.81.12";
            final var mapSpatKey = new RsuIntersectionKey(rsuIp, 12109);
            inputMapTopic.pipeInput(mapSpatKey, processedMap);
            inputSpatTopic.pipeInput(mapSpatKey, processedSpatUnaligned);


            
            


            List<KeyValue<RsuIntersectionKey, SignalGroupAlignmentNotification>> notificationResults = outputNotificationTopic.readKeyValuesToList();
            assertEquals(1, notificationResults.size());
            
            KeyValue<RsuIntersectionKey, SignalGroupAlignmentNotification> notificationKeyValue = notificationResults.get(0);

            assertEquals(mapSpatKey, notificationKeyValue.key);

            SignalGroupAlignmentNotification notification = notificationKeyValue.value;

            assertEquals("SignalGroupAlignmentNotification", notification.getNotificationType());

            assertEquals("Signal Group Alignment Notification, generated because corresponding signal group alignment event was generated.", notification.getNotificationText());

            assertEquals("Signal Group Alignment", notification.getNotificationHeading());

            SignalGroupAlignmentEvent event = notification.getEvent();

            assertEquals(mapSpatKey.getRsuId(), event.getSource());

            assertEquals(event.getEventType(), "SignalGroupAlignment");
            assertTrue(event.getSpatSignalGroupIds().contains(3));
            assertTrue(event.getSpatSignalGroupIds().contains(5));
            assertTrue(event.getSpatSignalGroupIds().contains(7));
            assertEquals(3, event.getSpatSignalGroupIds().size());

            assertEquals(3, event.getMapSignalGroupIds().size());
            assertTrue(event.getMapSignalGroupIds().contains(2));
            assertTrue(event.getMapSignalGroupIds().contains(4));
            assertTrue(event.getMapSignalGroupIds().contains(6));
            
            
        }
        assertEquals(0,0);
    }


    @Test
    public void signalStateConflictNotification() {

        MapSpatMessageAssessmentTopology mapSpat = new MapSpatMessageAssessmentTopology();
        MapSpatMessageAssessmentParameters parameters = new MapSpatMessageAssessmentParameters();

        parameters.setDebug(false);
        parameters.setIntersectionReferenceAlignmentEventTopicName(kafkaTopicIntersectionReferenceAlignmentEvents);
        parameters.setIntersectionReferenceAlignmentNotificationTopicName(kafkaTopicIntersectionReferenceAlignmentNotifications);
        parameters.setMapInputTopicName(kafkaTopicMapInputTopicName);
        parameters.setSpatInputTopicName(kafkaTopicSpatInputTopicName);
        parameters.setSignalGroupAlignmentEventTopicName(kafkaTopicSignalGroupAlignmentEventTopicName);
        parameters.setSignalStateConflictEventTopicName(kafkaTopicSignalStateConflictEventTopicName);
        parameters.setSignalGroupAlignmentNotificationTopicName(kafkaTopicSignalGroupAlignmentNotificationTopicName);
        parameters.setSignalStateConflictNotificationTopicName(kafkaTopicSignalStateConflictNotificationTopicName);

        mapSpat.setParameters(parameters);

        Topology topology = mapSpat.buildTopology();
        

        try (TopologyTestDriver driver = new TopologyTestDriver(topology)) {
            
            
            TestInputTopic<RsuIntersectionKey, String> inputMapTopic = driver.createInputTopic(
                kafkaTopicMapInputTopicName,
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey().serializer(),
                    Serdes.String().serializer());

            TestInputTopic<RsuIntersectionKey, String> inputSpatTopic = driver.createInputTopic(
                kafkaTopicSpatInputTopicName,
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey().serializer(),
                    Serdes.String().serializer());


            TestOutputTopic<RsuIntersectionKey, SignalStateConflictNotification> outputNotificationTopic = driver.createOutputTopic(
                kafkaTopicSignalStateConflictNotificationTopicName,
                    us.dot.its.jpo.geojsonconverter.serialization.JsonSerdes.RsuIntersectionKey().deserializer(),
                    JsonSerdes.SignalStateConflictNotification().deserializer());



            final String rsuIp = "10.11.81.12";
            final var mapSpatKey = new RsuIntersectionKey(rsuIp, 12109);
            inputMapTopic.pipeInput(mapSpatKey, processedMap);
            inputSpatTopic.pipeInput(mapSpatKey, processedSpatConflict);


            
            


            List<KeyValue<RsuIntersectionKey, SignalStateConflictNotification>> notificationResults = outputNotificationTopic.readKeyValuesToList();


            
            assertEquals(2, notificationResults.size());
            
            KeyValue<RsuIntersectionKey, SignalStateConflictNotification> notificationKeyValue = notificationResults.get(0);


            assertEquals(mapSpatKey, notificationKeyValue.key);

            SignalStateConflictNotification notification = notificationKeyValue.value;

            assertEquals("SignalStateConflictNotification", notification.getNotificationType());


            assertEquals("Signal State Conflict Notification, generated because corresponding signal state conflict event was generated.", notification.getNotificationText());

            assertEquals("Signal State Conflict", notification.getNotificationHeading());



            SignalStateConflictEvent event = notification.getEvent();
            

            assertEquals("PROTECTED_MOVEMENT_ALLOWED", event.getFirstConflictingSignalState().name());
            assertEquals("PROTECTED_MOVEMENT_ALLOWED", event.getSecondConflictingSignalState().name());
            assertEquals(6, event.getSecondConflictingSignalGroup());
            assertEquals(2, event.getFirstConflictingSignalGroup());
            assertEquals(12109, event.getIntersectionID());            
        }
    }
}