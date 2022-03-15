package com.walmart.oms.converter

import com.walmart.oms.commands.SearchOmsOrderOnCreateDateCommand
import com.walmart.oms.dto.OmsOrderReplayRequest
import spock.lang.Specification

import java.time.LocalDateTime

class OmsOrderReplayRequestToSearchCommandMapperTest extends Specification {
    OmsOrderReplayRequestToSearchCommandMapper commandMapper

    def setup() {
        commandMapper = new OmsOrderReplayRequestToSearchCommandMapper()
    }

    def "test mapToSearchOmsOrderOnCreateDate"() {
        given:
        OmsOrderReplayRequest omsOrderRepublishRequest = new OmsOrderReplayRequest()
        omsOrderRepublishRequest.setCreateStartDateTime(LocalDateTime.now())
        omsOrderRepublishRequest.setCreateEndDateTime(LocalDateTime.now())

        when:
        SearchOmsOrderOnCreateDateCommand omsOrderOnCreateDateCommand = commandMapper.mapToSearchOmsOrderOnCreateDate(omsOrderRepublishRequest)

        then:
        assert omsOrderOnCreateDateCommand.getCreateStartDateTime() != null
        assert omsOrderOnCreateDateCommand.getCreateEndDateTime() != null
    }
}
