package com.walmart.common.domain.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.walmart.common.domain.type.DomainEventType
import com.walmart.common.domain.type.Domain
import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceItemPriceInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderContactInfo
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.marketplace.order.domain.valueobject.mappers.MarketPlaceOrderToValueObjectMapper
import spock.lang.Specification

class DomainEventSpec extends Specification {

    MarketPlaceOrder marketPlaceOrder
    String externalOrderId = UUID.randomUUID().toString()
    Vendor vendor = Vendor.UBEREATS;
    String createdOrderState = "CREATED"
    String storeId = "4401"
    String externalItemId = UUID.randomUUID().toString()
    String customerFirstName = "John"
    String customerLastName = "Doe"
    Domain source = Domain.MARKETPLACE
    Domain destination = Domain.OMS
    String instanceId = UUID.randomUUID().toString()

    def setup() {
        ItemIdentifier itemIdentifier = ItemIdentifier.builder().itemId(externalItemId).itemType("CIN").build()
        MarketPlaceItemPriceInfo marketPlaceItemPriceInfo = MarketPlaceItemPriceInfo.builder().totalPrice(1.0).baseTotalPrice(1.0).unitPrice(1.0).baseUnitPrice(1.0).build()
        MarketPlaceOrderContactInfo marketPlaceOrderContactInfo = MarketPlaceOrderContactInfo.builder().firstName(customerFirstName).lastName(customerLastName).build()
        marketPlaceOrder = MarketPlaceOrder.builder()
                .vendorOrderId(externalOrderId)
                .orderDueTime(new Date())
                .orderState(createdOrderState)
                .marketPlaceItems([])
                .sourceModifiedDate(new Date())
                .storeId(storeId).vendorId(vendor)
                .marketPlaceOrderContactInfo(marketPlaceOrderContactInfo).build()
        marketPlaceOrder.addMarketPlaceItem(UUID.randomUUID().toString(), externalItemId, "test item", 1, instanceId, itemIdentifier, marketPlaceItemPriceInfo, SubstitutionOption.DO_NOT_SUBSTITUTE)
    }

    def "Create a domain event and test json string creation"() {

        given:
        DomainEvent marketPlaceOrderCreatedEvent
        MarketPlaceOrderValueObject mvo = MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder)
        String mkValueObject = new ObjectMapper().writeValueAsString(mvo)
        Optional<MarketPlaceOrderValueObject> unmarshalledMarketPlaceOrder
        when:
        marketPlaceOrderCreatedEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "Description")
                .from(source)
                .to(destination)
                .addMessage(mvo)
                .build()
        unmarshalledMarketPlaceOrder = marketPlaceOrderCreatedEvent.createObjectFromJson(MarketPlaceOrderValueObject.class)
        then:
        assert marketPlaceOrderCreatedEvent.getMessage() != null
        assert marketPlaceOrderCreatedEvent.getMessage().equals(mkValueObject)
        assert marketPlaceOrderCreatedEvent.getSource().equals(source)
        assert marketPlaceOrderCreatedEvent.getDestination().equals(destination)
        assert unmarshalledMarketPlaceOrder.isPresent()
        assert unmarshalledMarketPlaceOrder.get().contactInfo.firstName.equals(customerFirstName)
        assert unmarshalledMarketPlaceOrder.get().contactInfo.lastName.equals(customerLastName)
        assert unmarshalledMarketPlaceOrder.get().storeId.equals(storeId)
        assert unmarshalledMarketPlaceOrder.get().vendorOrderId.equals(externalOrderId)
        assert unmarshalledMarketPlaceOrder.get().vendorId.equals(vendor)
    }

    def "Test domain event creation with empty name"() {
        when:
        new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(null, null)
                .from(source)
                .addMessage(MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder))
                .to(destination).build()
        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Name must not be null"
    }

    def "Test domain event creation with empty message"() {
        when:
        new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "sample event description")
                .from(source)
                .to(destination).build()
        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Message must not be null"
    }

    def "Test domain event creation with empty source"() {
        when:
        new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "sample event description")
                .addMessage(MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder))
                .to(destination).build()
        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Source must not be null"
    }

    def "Test domain event creation with empty destination"() {
        when:
        new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "sample event description")
                .addMessage(MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder))
                .from(source)
                .build()
        then:
        def e = thrown(IllegalArgumentException)
        assert e.message == "Destination must not be null"
    }

    def "Test domain event adding headers to the event"() {
        given:
        DomainEvent marketPlaceOrderCreatedEvent = null

        when:
        marketPlaceOrderCreatedEvent = new DomainEvent.EventBuilder<MarketPlaceOrderValueObject>(DomainEventType.MARKET_PLACE_ORDER_CREATED, "Description")
                .from(source)
                .to(destination)
                .addMessage(MarketPlaceOrderToValueObjectMapper.INSTANCE.modelToValueObject(marketPlaceOrder))
                .addHeader("key1", "value1")
                .addHeader("key2", new Integer(100))
                .build()

        then:
        assert marketPlaceOrderCreatedEvent.getHeaders() != null
        assert marketPlaceOrderCreatedEvent.getHeaders().size() == 2
        assert marketPlaceOrderCreatedEvent.getHeaderValueForKey("key1", String.class) != null
        assert marketPlaceOrderCreatedEvent.getHeaderValueForKey("key1", String.class).get() == "value1"
        assert marketPlaceOrderCreatedEvent.getHeaderValueForKey("key2", Integer.class) != null
        assert marketPlaceOrderCreatedEvent.getHeaderValueForKey("key2", Integer.class).get() == 100
    }

    def "Test getters for DomainEvent"() {
        given:
        String description = "description"
        String key = "key"
        String msg = "msg"
        DomainEvent domainEvent = new DomainEvent()
        DomainEvent.EventBuilder builder = new DomainEvent.EventBuilder(DomainEventType.MARKET_PLACE_ORDER_CREATED, description)
        builder.withKey(key).addMessage(msg).from(source).to(destination).addHeader("key1", "value1")
        when:
        domainEvent = builder.build()

        then:
        domainEvent.getId() != null
        domainEvent.getKey() == key
        domainEvent.getDescription() == description
        domainEvent.getCreatedTime() != null
    }

    def "Test ConvertObjectToString failure"() {
        given:
        String description = "description"
        String key = "key"
        String msg = "msg"
        DomainEvent domainEvent = new DomainEvent()
        ObjectMapper objectMapper = Spy(ObjectMapper) {
            writeValueAsString(_) >> { throw new JsonProcessingException("Exception") }
        }
        domainEvent.@objectMapper = objectMapper

        DomainEvent.EventBuilder builder = new DomainEvent.EventBuilder(DomainEventType.MARKET_PLACE_ORDER_CREATED, description)
        builder.withKey(key).from(source).to(destination).addHeader("key1", "value1")
        when:
        builder.addMessage(msg)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test createObjectFromJson Failure"() {
        given:
        DomainEvent domainEvent = new DomainEvent()
        ObjectMapper objectMapper = Spy(ObjectMapper) {
            readValue(_, _) >> { throw new JsonProcessingException("Exception") }
        }
        domainEvent.@objectMapper = objectMapper
        when:
        domainEvent.createObjectFromJson(DomainEvent.class)
        then:
        thrown(IllegalArgumentException)
    }

    def "Test getHeaderValueForKey with empty Object"() {
        given:
        String key = "key"
        DomainEvent domainEvent = new DomainEvent()
        Map<String, Object> map = new HashMap<>()
        map.put(key, null)
        domainEvent.@headers = map
        when:
        Optional<Object> response = domainEvent.getHeaderValueForKey(key, null)
        then:
        !response.isPresent()
    }
}
