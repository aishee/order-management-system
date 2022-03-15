package com.walmart.marketplace.repository

import com.walmart.marketplace.order.domain.entity.MarketPlaceEvent
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.repository.infrastructure.mssql.IMarketPlaceEventSqlServerRepository
import spock.lang.Specification

class MarketPlaceEventRepositoryTest extends Specification {

    IMarketPlaceEventSqlServerRepository marketPlaceEventSqlServerRepository = Mock()

    MarketPlaceEventRepository marketPlaceEventRepository

    def setup() {
        marketPlaceEventRepository = new MarketPlaceEventRepository(
                marketPlaceEventSqlServerRepository: marketPlaceEventSqlServerRepository)

    }

    def "Test Save entity"() {
        given:
        String eventId = UUID.randomUUID().toString();
        Vendor vendor = Vendor.UBEREATS;
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"

        MarketPlaceEvent eventToPersist = MarketPlaceEvent.builder()
                .externalOrderId(eventId)
                .vendor(vendor)
                .eventType(eventType)
                .sourceEventId(eventId)
                .resourceURL(resourceUrl).build();

        when:
        marketPlaceEventRepository.save(eventToPersist)

        then:
        1 * marketPlaceEventSqlServerRepository.save(_ as MarketPlaceEvent) >> eventToPersist

    }

    def "Test Get entity"() {
        given:
        String eventId = UUID.randomUUID().toString();
        Vendor vendor = Vendor.UBEREATS;
        String eventType = "orders.notify"
        String resourceUrl = "http://localhost:8080"

        MarketPlaceEvent eventFromDb = MarketPlaceEvent.builder()
                .externalOrderId(eventId)
                .vendor(vendor)
                .eventType(eventType)
                .sourceEventId(eventId)
                .resourceURL(resourceUrl).build();

        when:
        marketPlaceEventRepository.get(eventId)

        then:
        1 * marketPlaceEventSqlServerRepository.findBySourceEventId(_ as String) >> eventFromDb
    }

    def " Test GetNextIdentity"() {
        when:
        String id = marketPlaceEventRepository.getNextIdentity()

        then:
        UUID uuid = UUID.fromString(id)
        assert uuid != null


    }
}
