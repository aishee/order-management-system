package com.walmart.marketplace.uber.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportMetaData {

  private List<Section> sections;

  @Getter
  @Setter
  @ToString
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Section {

    @JsonProperty("section_id")
    private String sectionId;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("download_url")
    private String downloadUrl;
  }

  public List<String> getDownloadUrlList() {
    return Optional.ofNullable(sections)
        .map(list -> list.stream().map(Section::getDownloadUrl).collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }
}
