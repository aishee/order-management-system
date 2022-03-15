package com.walmart.oms

import com.walmart.common.metrics.MetricConstants
import com.walmart.common.metrics.MetricService
import com.walmart.oms.commands.SearchOmsOrderOnCreateDateCommand
import com.walmart.oms.order.repository.OmsOrderReplayRepository
import spock.lang.Specification

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.LocalDateTime

class OmsOrderReplayApplicationServiceTest extends Specification {
    OmsOrderReplayRepository omsOrderReplayRepository = Mock()

    MetricService metricService = Mock()

    OmsOrderReplayApplicationService omsOrderReplayApplicationService

    def setup() {
        omsOrderReplayApplicationService = new OmsOrderReplayApplicationService(omsOrderReplayRepository, metricService)
    }

    def "Test Init"() {
        when:
        omsOrderReplayApplicationService.initExecutor()

        then:
        Field executorPoolField = omsOrderReplayApplicationService.getClass().getDeclaredField("executor")
        executorPoolField.setAccessible(true)
        executorPoolField.get(omsOrderReplayApplicationService) != null
    }

    def "test executeReplayOmsOrder success"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        LocalDateTime createEndDateTime = LocalDateTime.now()
        omsOrderReplayApplicationService.initExecutor()
        SearchOmsOrderOnCreateDateCommand searchOmsOrderOnCreateDateCommand = SearchOmsOrderOnCreateDateCommand.builder()
                .createEndDateTime(createEndDateTime).createStartDateTime(createStartDateTime).build()

        when:
        omsOrderReplayApplicationService.executeReplayOmsOrder(searchOmsOrderOnCreateDateCommand)

        then:
        0 * omsOrderReplayRepository.findAllOrderAndReplayToDwhKafkaTopic(_ as LocalDateTime, _ as LocalDateTime)
    }

    def "test findAllOrderAndReplayToDwhKafkaTopic success"() {
        given:
        Object[] args = new Object[2]
        args[0] = LocalDateTime.now()
        args[1] = LocalDateTime.now()
        Method findAllOrderAndReplayToDwhKafkaTopicMethod = omsOrderReplayApplicationService.
                getClass().
                getDeclaredMethod("findAllOrderAndReplayToDwhKafkaTopic", LocalDateTime.class, LocalDateTime.class)
        findAllOrderAndReplayToDwhKafkaTopicMethod.setAccessible(true)

        when:
        findAllOrderAndReplayToDwhKafkaTopicMethod.invoke(omsOrderReplayApplicationService, args)

        then:
        1 * omsOrderReplayRepository.findAllOrderAndReplayToDwhKafkaTopic(_ as LocalDateTime, _ as LocalDateTime)
    }

    def "test findAllOrderAndReplayToDwhKafkaTopic exceptional"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        LocalDateTime createEndDateTime = LocalDateTime.now()
        Object[] args = new Object[2]
        args[0] = createStartDateTime
        args[1] = createEndDateTime
        Method findAllOrderAndReplayToDwhKafkaTopicMethod = omsOrderReplayApplicationService.
                getClass().
                getDeclaredMethod("findAllOrderAndReplayToDwhKafkaTopic", LocalDateTime.class, LocalDateTime.class)
        findAllOrderAndReplayToDwhKafkaTopicMethod.setAccessible(true)
        omsOrderReplayRepository.findAllOrderAndReplayToDwhKafkaTopic(createStartDateTime, createEndDateTime) >> { throw new RuntimeException("Any Exception") }

        when:
        findAllOrderAndReplayToDwhKafkaTopicMethod.invoke(omsOrderReplayApplicationService, args)

        then:
        thrown(InvocationTargetException)
    }

    def "test executeReplayOmsOrder with error"() {
        given:
        LocalDateTime createStartDateTime = LocalDateTime.now()
        LocalDateTime createEndDateTime = LocalDateTime.now()
        SearchOmsOrderOnCreateDateCommand searchOmsOrderOnCreateDateCommand = SearchOmsOrderOnCreateDateCommand.builder()
                .createEndDateTime(createStartDateTime).createStartDateTime(createEndDateTime).build()
        omsOrderReplayApplicationService.initExecutor()
        omsOrderReplayRepository.findAllOrderAndReplayToDwhKafkaTopic(createStartDateTime, createEndDateTime) >> { throw new RuntimeException("Any Exception") }


        when:
        omsOrderReplayApplicationService.executeReplayOmsOrder(searchOmsOrderOnCreateDateCommand)

        then:
        0 * metricService.incrementExceptionCounterByType(_ as MetricConstants.MetricCounters, _ as MetricConstants.MetricCounters, _ as String)
    }
}
