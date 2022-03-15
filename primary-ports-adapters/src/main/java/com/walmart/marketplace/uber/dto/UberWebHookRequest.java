package com.walmart.marketplace.uber.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@JsonPropertyOrder({"event_id", "event_time", "event_type", "meta", "resource_href"})
public class UberWebHookRequest {

  @JsonProperty("event_id")
  private String eventId;

  @JsonProperty("event_time")
  private long eventTime;

  @JsonProperty("event_type")
  private String eventType;

  @JsonProperty("meta")
  private Meta meta;

  @JsonProperty("resource_href")
  private String resourceHref;

  @JsonProperty("report_type")
  private String reportType;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("report_metadata")
  private ReportMetaData reportMetaData;

  public void setReportMetaData(ReportMetaData reportMetaData) {
    this.reportMetaData = reportMetaData;
  }

  public List<String> getDownloadUrlList() {
    if (reportMetaData != null) {
      return reportMetaData.getDownloadUrlList();
    }
    return Collections.emptyList();
  }

  public boolean hasDownloadUrl() {
    return !this.getDownloadUrlList().isEmpty();
  }

  @JsonProperty("event_id")
  public String getEventId() {
    return eventId;
  }

  @JsonProperty("event_id")
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  @JsonProperty("event_time")
  public long getEventTime() {
    return eventTime;
  }

  @JsonProperty("event_time")
  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
  }

  @JsonProperty("event_type")
  public String getEventType() {
    return eventType;
  }

  @JsonProperty("event_type")
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  @JsonProperty("meta")
  public Meta getMeta() {
    return meta;
  }

  @JsonProperty("meta")
  public void setMeta(Meta meta) {
    this.meta = meta;
  }

  @JsonProperty("resource_href")
  public String getResourceHref() {
    return resourceHref;
  }

  @JsonProperty("resource_href")
  public void setResourceHref(String resourceHref) {
    this.resourceHref = resourceHref;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  @JsonProperty("report_type")
  public String getReportType() {
    return reportType;
  }

  @JsonProperty("report_type")
  public void setReportType(String reportType) {
    this.reportType = reportType;
  }
}
