package com.walmart.common.domain.event.processing;

import com.walmart.common.domain.BaseEntity;
import com.walmart.util.JsonConverterUtil;
import java.io.StringWriter;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.JAXB;
import lombok.Getter;
import org.springframework.util.Assert;

/**
 * An Event going out from a domain. A new instance of EgressEvent can be build as shown below.
 *
 * <p>
 *
 * <pre>
 *  new {@link EgressEvent}.Builder<T,R>(eName, description)
 * .producedFrom({@link String}) //The domain code
 * .toEndPoint({@link String}) //The end point uri
 * .withModel(T t)  // The domain model.
 * .withMapper({@link Function<T,R>}) // A mapping function to map the domain model to the integration object.
 * .asXml() // Message format to be XML
 * .build()</pre>
 *
 * @param <T> source model object type.
 * @param <R> target model object type.
 */
@Entity
@Table(name = "OMSCORE.EGRESS_EVENTS")
public class EgressEvent<T extends BaseEntity, R> extends BaseEntity implements IEgressEvent {

  @Getter
  @Column(name = "DOMAIN_MODEL_ID")
  private String domainModelId;
  /** An alphabetical code representing the domain from which this event was created. */
  @Getter
  @Column(name = "SOURCE_DOMAIN")
  private String domain;
  /** A meaning full name of the event. */
  @Getter
  @Column(name = "EVENT_NAME")
  private String name;
  /** A brief description of the event. */
  @Getter
  @Column(name = "EVENT_DESCRIPTION")
  private String description;
  /** The destination endpoint example: http uri,JMS queue or topic end point. */
  @Getter
  @Column(name = "DESTINATION")
  private String destination;
  /** Marshalled string format of a message object. */
  @Column(name = "MESSAGE")
  private String message;
  /** A mapping function for converting model to the integration object. */
  @Getter @Transient private transient Function<T, R> mappingFunction;
  /** A copy of the domain model. */
  @Getter @Transient private T model;
  /** The type of the message:- XML, JSON */
  @Getter
  @Column(name = "MESSAGE_FORMAT")
  private MessageFormat format;
  /** A copy of the mapped object after the mapping has applied. */
  @Transient private transient R mappedObject;
  /**
   * The status of the EgressEvent interaction.
   *
   * @see EgressStatus
   */
  @Getter
  @Column(name = "EVENT_STATUS")
  @Enumerated(EnumType.STRING)
  private EgressStatus status;
  /** The number of retries made on the event. */
  @Getter
  @Column(name = "RETRIES")
  private int retries = 0;

  @Getter
  @Column(name = "JUST_AUDIT")
  private boolean justAudit;

  private EgressEvent() {}

  private EgressEvent(Builder<T, R> builder) {
    this.id = UUID.randomUUID().toString();
    this.description = builder.description;
    this.destination = builder.destination;
    this.domain = builder.domain;
    this.mappingFunction = builder.mappingFunction;
    this.name = builder.name;
    this.model = builder.model;
    this.format = builder.format;
    this.status = EgressStatus.INITIAL;
    this.domainModelId = builder.model != null ? builder.model.getId() : null;
    this.message = builder.message;
    this.retries = builder.retries;
    this.justAudit = builder.justAudit;
  }

  /**
   * Copy attributes from another event object.
   *
   * @param event
   */
  public void copy(EgressEvent<T, R> event) {
    this.domain = event.domain;
    this.status = event.status;
    this.message = event.message;
    this.format = event.format;
  }

  @Override
  public boolean isMappingApplied() {
    return mappedObject != null;
  }

  /**
   * Applies the mapping to convert T to R
   *
   * @return An object of type R if the mapping is successful.
   */
  @Override
  public void applyMapping() {
    this.mappedObject = this.mappingFunction.apply(this.model);
  }

  /**
   * Returns a copy of the message, if its present, otherwise throws {@link IllegalStateException}
   *
   * @return a copy of the message.
   * @throws IllegalStateException if th message is not constructed.
   */
  public String getMessage() {
    return this.message;
  }

  @Override
  public void markAsProduced() {
    this.status = EgressStatus.PRODUCED;
  }

  @Override
  public void markAsFailed() {
    this.status = EgressStatus.FAILED;
  }

  @Override
  public void markAsReadyToPublish() {
    Assert.notNull(message, "Event not ready to publish");
    this.status = EgressStatus.READY_TO_PUBLISH;
  }

  @Override
  public void markAsError() {
    this.status = EgressStatus.ERROR;
  }

  @Override
  public boolean markedAsError() {
    return this.status == EgressStatus.ERROR;
  }

  @Override
  public void markAsInitial() {
    this.status = EgressStatus.INITIAL;
  }

  @Override
  public int tryAgain() {
    return ++this.retries;
  }

  @Override
  public boolean isFailed() {
    return this.status == EgressStatus.FAILED;
  }

  /** converts the mapped object to an xml string and assigns it to message object. */
  @Override
  public void createXmlMessage() {
    Assert.state(
        this.mappedObject != null, "mappedObject is null, try calling applyMapping() first");
    StringWriter sw = new StringWriter();
    try {
      JAXB.marshal(this.mappedObject, sw);
      this.message = sw.toString();
      this.markAsReadyToPublish();
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** converts the mapped object to an json string and assigns it to message object. */
  @Override
  public void createJsonMessage() {
    Assert.state(
        this.mappedObject != null, "mappedObject is null, try calling applyMapping() first");
    this.message = JsonConverterUtil.convertToString(this.mappedObject);
    this.markAsReadyToPublish();
  }

  /**
   * Returns a lite copy of the egress event with only limited information.
   *
   * @return a slim copy of an EgressEvent
   */
  public EgressEvent<T, R> makeLiteCopy() {
    EgressEvent<T, R> event = new EgressEvent<>();
    event.description = this.description;
    event.name = this.name;
    event.domain = this.domain;
    event.domainModelId = this.domainModelId;
    event.format = this.format;
    event.destination = this.destination;
    event.retries = this.retries;
    event.status = this.status;
    return event;
  }

  /** An enum to capture the status of the integration where this EgressEvent was involved. */
  public enum EgressStatus {
    PRODUCED,
    FAILED,
    INITIAL,
    READY_TO_PUBLISH,
    ERROR
  }

  /** Messaging formats */
  public enum MessageFormat {
    XML,
    JSON
  }

  /** A builder for {@link EgressEvent} */
  public static class Builder<T extends BaseEntity, R> {

    private String domain;
    private String name;
    private String description;
    private String destination;
    private Function<T, R> mappingFunction;
    private T model;
    private MessageFormat format;
    private String message;
    private int retries;
    private boolean justAudit;

    public Builder(String name, String description) {
      this.name = name;
      this.description = description;
    }

    /**
     * Adds the message to the event.
     *
     * @param message
     * @return An instance of the Builder
     */
    public Builder<T, R> withMessage(String message) {
      this.message = message;
      return this;
    }

    /**
     * Mark the event tracing only for audit. If not explicitly marked the event will become
     * eligible for full tracing.
     *
     * @return An instance of the Builder
     */
    public Builder<T, R> justAudit() {
      this.justAudit = true;
      return this;
    }

    /**
     * Initialize the retry count.
     *
     * @param retries initialize the number of retires already made.
     * @return An instance of the Builder
     */
    public Builder<T, R> withRetries(int retries) {
      this.retries = retries;
      return this;
    }

    /**
     * Adds the origin domain code in to the event.
     *
     * @param source A code representing the origin domain example:<code>OMS,FMS</code>.
     * @return An instance of the Builder
     */
    public Builder<T, R> producedFrom(String source) {
      this.domain = source;
      return this;
    }

    /**
     * Adds the destination end point uri to the event.
     *
     * @param endPoint The destination endpoint uri.
     * @return An instance of the Builder.
     */
    public Builder<T, R> toDestination(String endPoint) {
      this.destination = endPoint;
      return this;
    }

    /**
     * Adds the domain model to the event.
     *
     * @param m A copy of the domain object.
     * @return An instance of the Builder.
     */
    public Builder<T, R> withModel(T m) {
      this.model = m;
      return this;
    }

    /**
     * Produce the message in Json format
     *
     * @return An instance of the Builder.
     */
    public Builder<T, R> asJson() {
      this.format = MessageFormat.JSON;
      return this;
    }

    /**
     * Produce the message as XML format.
     *
     * @return
     */
    public Builder<T, R> asXml() {
      this.format = MessageFormat.XML;
      return this;
    }

    /**
     * Adds the object mapper to the event.
     *
     * @param mappingFunction The mapping function to convert the domain object to the integration
     *     object.
     * @return An instance of the Builder.
     */
    public Builder<T, R> withMapper(Function<T, R> mappingFunction) {
      this.mappingFunction = mappingFunction;
      return this;
    }

    /**
     * Builds the {@link EgressEvent} object.
     *
     * @return Returns a new {@link EgressEvent} object.
     */
    public EgressEvent<T, R> build() {
      EgressEvent<T, R> event = new EgressEvent<>(this);
      validate(event);
      return event;
    }

    /**
     * Validate the integrity of the event object.
     *
     * @param event A copy of the {@link EgressEvent}
     * @throws IllegalArgumentException when a validation fails.
     */
    private void validate(EgressEvent<T, R> event) {
      if (event.getMessage() == null) {
        Assert.notNull(event.mappingFunction, "Mapping function cannot be null!!");
        Assert.notNull(event.format, "format cannot be null!!");
      }
      Assert.notNull(event.domain, "Origin domain name cannot be null!!");
      Assert.notNull(event.model, "Domain model cannot be null!!");
      Assert.notNull(event.model.getId(), "event id cannot be null!!");
      Assert.notNull(event.destination, "Destination cannot be null!!");
      Assert.notNull(event.name, "Name cannot be null!!");
    }
  }
}
