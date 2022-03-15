package com.walmart.oms.converter;

import com.walmart.oms.commands.SearchOmsOrderOnCreateDateCommand;
import com.walmart.oms.dto.OmsOrderReplayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** This is mapper class maps OmsOrderReplayRequest To SearchOmsOrderOnCreateDateCommand. */
@Slf4j
@Component
public class OmsOrderReplayRequestToSearchCommandMapper {

  /**
   * @param omsOrderReplayRequest {@link OmsOrderReplayRequest}
   * @return {@link SearchOmsOrderOnCreateDateCommand}
   */
  public SearchOmsOrderOnCreateDateCommand mapToSearchOmsOrderOnCreateDate(
      OmsOrderReplayRequest omsOrderReplayRequest) {
    return SearchOmsOrderOnCreateDateCommand.builder()
        .createStartDateTime(omsOrderReplayRequest.getCreateStartDateTime())
        .createEndDateTime(omsOrderReplayRequest.getCreateEndDateTime())
        .build();
  }
}
