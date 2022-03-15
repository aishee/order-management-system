package com.walmart.marketplace.justeats.processors

import com.walmart.common.metrics.MetricService
import com.walmart.marketplace.MarketPlaceApplicationService
import com.walmart.marketplace.commands.MarketPlaceCreateOrderCommand
import com.walmart.marketplace.commands.WebHookEventCommand
import com.walmart.marketplace.converter.JustEatsRequestToCommandMapper
import com.walmart.marketplace.justeats.request.JustEatsWebHookRequest
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import spock.lang.Specification

class JustEatsOrderNotifyEventProcessorTest extends Specification {

    MetricService metricService = Mock()
    JustEatsRequestToCommandMapper mapper = Mock()
    MarketPlaceApplicationService marketPlaceApplicationService = Mock()
    JustEatsOrderNotifyEventProcessor justEatsOrderNotifyEventProcessor

    def setup() {
        justEatsOrderNotifyEventProcessor = new JustEatsOrderNotifyEventProcessor(
                metricService: metricService,
                mapper: mapper,
                marketPlaceApplicationService: marketPlaceApplicationService)
    }

    def "Successful order create processing"() {
        given:
        JustEatsWebHookRequest justEatsWebHookRequest = new JustEatsWebHookRequest()
        WebHookEventCommand webHookEventCommand = Mock()
        MarketPlaceEvent marketPlaceEvent = Mock()
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = Mock()
        MarketPlaceOrder marketPlaceOrder = Mock()

        when:
        boolean response = justEatsOrderNotifyEventProcessor.processWebhookRequest(justEatsWebHookRequest)


        then:
        1 * mapper.createWebHookCommand(justEatsWebHookRequest) >> {
            return webHookEventCommand
        }
        1 * mapper.createMarketPlaceOrderCmd(justEatsWebHookRequest) >> {
            return marketPlaceCreateOrderCommand
        }
        1 * marketPlaceApplicationService.captureWebHookEvent(webHookEventCommand) >> {
            return marketPlaceEvent
        }
        1 * marketPlaceApplicationService.createAndProcessMarketPlaceOrder(marketPlaceCreateOrderCommand) >> {
            return marketPlaceOrder
        }
        response

    }

    def "Runtime Exception in JustEats Notify Processing"() {
        given:
        JustEatsWebHookRequest justEatsWebHookRequest = new JustEatsWebHookRequest()
        WebHookEventCommand webHookEventCommand = Mock()
        MarketPlaceEvent marketPlaceEvent = Mock()
        MarketPlaceCreateOrderCommand marketPlaceCreateOrderCommand = Mock()

        when:
        justEatsOrderNotifyEventProcessor.processWebhookRequest(justEatsWebHookRequest)

        then:
        thrown(OMSThirdPartyException)
        1 * mapper.createWebHookCommand(justEatsWebHookRequest) >> {
            return webHookEventCommand
        }
        1 * mapper.createMarketPlaceOrderCmd(justEatsWebHookRequest) >> {
            throw new OMSThirdPartyException("Command object mapping exception")
        }
        1 * marketPlaceApplicationService.captureWebHookEvent(webHookEventCommand) >> {
            return marketPlaceEvent
        }
        0 * marketPlaceApplicationService.createAndProcessMarketPlaceOrder(marketPlaceCreateOrderCommand)

    }

    def "Failure in order create processing for JustEats"() {
        given:
        JustEatsWebHookRequest justEatsWebHookRequest = new JustEatsWebHookRequest()

        when:
        justEatsOrderNotifyEventProcessor.processWebhookRequest(justEatsWebHookRequest)

        then:
        1 * mapper.createWebHookCommand(justEatsWebHookRequest) >> { throw new OMSBadRequestException("Invalid Requests") }
        thrown(OMSBadRequestException.class)

    }

}
