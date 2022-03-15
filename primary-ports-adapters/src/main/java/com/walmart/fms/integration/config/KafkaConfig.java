package com.walmart.fms.integration.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaConfig {
  @JsonProperty("key.deserializer")
  private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

  @JsonProperty("value.deserializer")
  private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";

  @JsonProperty("key.serializer")
  private String keySerializer = "org.apache.kafka.common.serialization.StringSerializer";

  @JsonProperty("value.serializer")
  private String valueSerializer = "org.apache.kafka.common.serialization.StringSerializer";

  @JsonProperty("enable.auto.commit")
  private boolean autoCommit = false;

  @JsonProperty("bootstrap.servers")
  private String bootStrapServers;

  @JsonProperty("topic")
  private String topic;

  @JsonProperty("session.timeout.ms")
  private int sessionTimeout = 30000;

  @JsonProperty("request.timeout.ms")
  private int requestTimeout = 60000;

  @JsonProperty("auto.offset.reset")
  private String autoOffset = "latest";

  @JsonProperty("max.poll.records")
  private int maxPollRecords = 5;

  @JsonProperty("heartbeat.interval.ms")
  private int heartBeatIntervalMs = 10000;

  @JsonProperty("group.id")
  private String groupId = "UKFMS-CONSUMER";

  @JsonProperty("secured.cluster")
  private boolean securedCluster;
}
