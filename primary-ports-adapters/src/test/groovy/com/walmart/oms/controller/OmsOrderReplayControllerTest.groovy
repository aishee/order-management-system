package com.walmart.oms.controller

import com.walmart.common.metrics.MetricService
import com.walmart.oms.OmsOrderReplayApplicationService
import com.walmart.oms.commands.SearchOmsOrderOnCreateDateCommand
import com.walmart.oms.converter.OmsOrderReplayRequestToSearchCommandMapper
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.oms.dto.OmsOrderReplayRequest
import spock.lang.Specification

import java.time.LocalDateTime

class OmsOrderReplayControllerTest extends Specification {
    MetricService metricService = Mock()
    OmsOrderReplayRequestToSearchCommandMapper orderReplayRequestToSearchCommandMapper = Mock()
    OmsOrderReplayApplicationService omsOrderReplayApplicationService = Mock()
    OmsOrderReplayController omsOrderReplayController

    def setup() {
        omsOrderReplayController = new OmsOrderReplayController(metricService, orderReplayRequestToSearchCommandMapper, omsOrderReplayApplicationService)

    }

    def "test republish to kafka dwh topic"() {
        given:
        OmsOrderReplayRequest omsOrderRepublishRequest = new OmsOrderReplayRequest()
        omsOrderRepublishRequest.setCreateStartDateTime(LocalDateTime.now())
        omsOrderRepublishRequest.setCreateEndDateTime(LocalDateTime.now())
        SearchOmsOrderOnCreateDateCommand omsOrderOnCreateDateCommand = SearchOmsOrderOnCreateDateCommand.builder()
                .createStartDateTime(omsOrderRepublishRequest.getCreateStartDateTime()).createEndDateTime(omsOrderRepublishRequest.getCreateEndDateTime()).build()

        orderReplayRequestToSearchCommandMapper.mapToSearchOmsOrderOnCreateDate(omsOrderRepublishRequest) >> omsOrderOnCreateDateCommand

        when:
        omsOrderReplayController.replayOmsOrderToKafkaDwhTopic(omsOrderRepublishRequest)

        then:
        1 * omsOrderReplayApplicationService.executeReplayOmsOrder(_ as SearchOmsOrderOnCreateDateCommand)

    }

    def "test republish to kafka dwh topic exceptionally"() {
        given:
        OmsOrderReplayRequest omsOrderRepublishRequest = new OmsOrderReplayRequest()
        omsOrderRepublishRequest.setCreateStartDateTime(LocalDateTime.now())
        omsOrderRepublishRequest.setCreateEndDateTime(LocalDateTime.now())
        SearchOmsOrderOnCreateDateCommand omsOrderOnCreateDateCommand = SearchOmsOrderOnCreateDateCommand.builder()
                .createStartDateTime(omsOrderRepublishRequest.getCreateStartDateTime()).createEndDateTime(omsOrderRepublishRequest.getCreateEndDateTime()).build()
        orderReplayRequestToSearchCommandMapper.mapToSearchOmsOrderOnCreateDate(omsOrderRepublishRequest) >> omsOrderOnCreateDateCommand
        omsOrderReplayApplicationService.executeReplayOmsOrder(_ as SearchOmsOrderOnCreateDateCommand) >> { throw new OMSThirdPartyException("Any Exception") }

        when:
        omsOrderReplayController.replayOmsOrderToKafkaDwhTopic(omsOrderRepublishRequest)

        then:
        thrown(OMSThirdPartyException)

    }

    def "test republish to kafka dwh topic validation exception"() {
        given:
        OmsOrderReplayRequest omsOrderRepublishRequest = new OmsOrderReplayRequest()
        omsOrderRepublishRequest.setCreateStartDateTime(LocalDateTime.now().plusMinutes(1))
        omsOrderRepublishRequest.setCreateEndDateTime(LocalDateTime.now())

        when:
        omsOrderReplayController.replayOmsOrderToKafkaDwhTopic(omsOrderRepublishRequest)

        then:
        thrown(OMSBadRequestException)

    }
}
