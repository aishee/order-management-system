package com.walmart.common.domain.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.common.domain.type.Domain;
import com.walmart.common.domain.type.DomainEventType;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.MapUtils;
import org.springframework.util.Assert;

@NoArgsConstructor
public class DomainEvent {

  @Getter
  private UUID id;
  @Getter
  private DomainEventType name;
  @Getter
  private String description;
  @Getter
  private Date createdTime;
  @Getter
  private Domain source;
  @Getter
  private Domain destination;

  private Map<String, Object> headers;

  @Getter
  private String key;

  @Getter
  private String message;

  private static ObjectMapper objectMapper = getInstance();

  private static ObjectMapper getInstance() {
    return new ObjectMapper();
  }

  private DomainEvent(EventBuilder builder) {
    this.id = UUID.randomUUID();
    this.name = builder.name;
    this.description = builder.description;
    this.createdTime = new Date();
    this.source = builder.source;
    this.destination = builder.destination;
    this.key = builder.key;
    this.message = builder.message;
    this.headers = builder.headers;
  }

  public static class EventBuilder {

    private final DomainEventType name;
    private final String description;

    private Domain source;
    private Domain destination;
    private String key;
    private String message;
    private Map<String, Object> headers;

    public EventBuilder(DomainEventType name, String description) {
      this.name = name;
      this.description = description;
      this.headers = new HashMap<>();
    }

    public EventBuilder from(Domain source) {
      this.source = source;
      return this;
    }

    public EventBuilder to(Domain destination) {
      this.destination = destination;
      return this;
    }

    public EventBuilder withKey(String key) {
      this.key = key;
      return this;
    }

    public <T> EventBuilder addMessage(T t) {
      Assert.notNull(t, "Message object must not be null !!");
      this.message = convertObjectToString(t);
      return this;
    }

    public EventBuilder addHeader(String key, Object value) {
      if (MapUtils.isEmpty(this.headers)) {
        this.headers = new HashMap<>();
      }
      this.headers.put(key, value);
      return this;
    }

    public DomainEvent build() {
      DomainEvent event = new DomainEvent(this);
      validateEvent(event);
      return event;
    }

    private <T> String convertObjectToString(T t) {
      try {
        return objectMapper.writeValueAsString(t);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException(e);
      }
    }

    private void validateEvent(DomainEvent event) {
      Assert.notNull(event.getMessage(), "Message must not be null");
      Assert.notNull(event.getName(), "Name must not be null");
      Assert.notNull(event.getSource(), "Source must not be null");
      Assert.notNull(event.getDestination(), "Destination must not be null");
      Assert.notNull(event.getHeaders(), "Headers are not initialized");
    }
  }

  public <V> Optional<V> getHeaderValueForKey(String key, Class<V> c) {
    Object value = this.headers.get(key);
    if (value != null && c.isInstance(value)) {
      return Optional.of(c.cast(value));
    } else {
      return Optional.empty();
    }
  }

  public Map<String, Object> getHeaders() {
    return Collections.unmodifiableMap(this.headers);
  }

  public <R> Optional<R> createObjectFromJson(Class<R> className) {
    R r = null;
    try {
      r = objectMapper.readValue(this.message, className);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
    return Optional.ofNullable(r);
  }

  public boolean isInitiatedByOMS() {
    return Domain.OMS == this.source;
  }

  public boolean isInitiatedByMarketPlace() {
    return Domain.MARKETPLACE == this.source;
  }
}
