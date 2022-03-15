package com.walmart.common.infrastructure.config;

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaProducerConfig {

  private String bootStrapServers;
  private String keySerializer;
  private String valueSerializer;
  private String acks = "1";
  private String topic;
  private String compressionType;
  private int lingerMs;
  private int retries;
  private int bufferMemory = 1024 * 1024;
  private int batchSize = 1;
  private int requestTimeout = 60000;
  private String securityProtocol;
  private String sslTruststoreLocation;
  private String sslKeystoreLocation;
  private String sslTruststorePassword;
  private String sslKeystorePassword;
  private String sslKeyPassword;
  private boolean securedCluster;

  public Properties createProperties() {
    Properties props = new Properties();
    props.put(KafkaProducerProperties.BOOT_STRAP_SERVERS.getName(), bootStrapServers);
    props.put(KafkaProducerProperties.KEY_SERIALIZER.getName(), keySerializer);
    props.put(KafkaProducerProperties.VALUE_SERIALIZER.getName(), valueSerializer);
    props.put(KafkaProducerProperties.ACKS.getName(), acks);
    props.put(KafkaProducerProperties.RETRIES.getName(), retries);
    props.put(KafkaProducerProperties.BATCH_SIZE.getName(), batchSize);
    props.put(KafkaProducerProperties.LINGER_MS.getName(), lingerMs);
    props.put(KafkaProducerProperties.BUFFER_MEMORY.getName(), bufferMemory);
    props.put(KafkaProducerProperties.REQUEST_TIMEOUT.getName(), requestTimeout);
    if (securedCluster) {
      props.put(KafkaProducerProperties.SECURITY_PROTOCOL.getName(), securityProtocol);
      props.put(KafkaProducerProperties.SSL_TRUSTSTORE_LOCATION.getName(), sslTruststoreLocation);
      props.put(KafkaProducerProperties.SSL_KEYSTORE_LOCATION.getName(), sslKeystoreLocation);
      props.put(KafkaProducerProperties.SSL_TRUSTSTORE_PASSWORD.getName(), sslTruststorePassword);
      props.put(KafkaProducerProperties.SSL_KEYSTORE_PASSWORD.getName(), sslKeystorePassword);
      props.put(KafkaProducerProperties.SSL_KEY_PASSWORD.getName(), sslKeyPassword);
    }
    return props;
  }

  private enum KafkaProducerProperties {
    BOOT_STRAP_SERVERS("bootstrap.servers"),
    KEY_SERIALIZER("key.serializer"),
    VALUE_SERIALIZER("value.serializer"),
    COMPRESSION_TYPE("compression.type"),
    ACKS("acks"),
    RETRIES("retries"),
    BATCH_SIZE("batch.size"),
    LINGER_MS("linger.ms"),
    BUFFER_MEMORY("buffer.memory"),
    TOPIC("topic"),
    REQUEST_TIMEOUT("request.timeout.ms"),
    SECURITY_PROTOCOL("security.protocol"),
    SSL_TRUSTSTORE_LOCATION("ssl.truststore.location"),
    SSL_KEYSTORE_LOCATION("ssl.keystore.location"),
    SSL_TRUSTSTORE_PASSWORD("ssl.truststore.password"),
    SSL_KEYSTORE_PASSWORD("ssl.keystore.password"),
    SSL_KEY_PASSWORD("ssl.key.password");

    @Getter private final String name;

    KafkaProducerProperties(String name) {
      this.name = name;
    }
  }
}
