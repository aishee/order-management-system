package com.walmart.oms.commands;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/** This command used to search OmsOrder based on created date range. */
@Getter
@Builder
public class SearchOmsOrderOnCreateDateCommand {
  private final LocalDateTime createStartDateTime;
  private final LocalDateTime createEndDateTime;
}
