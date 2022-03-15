package com.walmart.oms.commands.mappers

import com.walmart.common.domain.type.SubstitutionOption
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.valueobject.MarketPlaceOrderValueObject
import com.walmart.oms.commands.CreateOmsOrderCommand
import spock.lang.Specification

class MarketPlaceVoToOMSCommandMapperSpec extends Specification {

    def "Marketplace VO to Command Mapper for order without bundles"() {
        given:
        MarketPlaceOrderValueObject valueObject = new MarketPlaceOrderValueObject()
        MarketPlaceOrderValueObject.ContactInfo contactInfo = new MarketPlaceOrderValueObject.ContactInfo()
        contactInfo.setFirstName("A")
        contactInfo.setLastName("B")
        valueObject.setContactInfo(contactInfo)
        valueObject.setStoreId("5755")
        Date orderDueTime = new Date()
        valueObject.setOrderDueTime(orderDueTime)
        valueObject.setVendorId(Vendor.TESTVENDOR)
        valueObject.setSourceOrderId(UUID.randomUUID().toString())
        valueObject.setVendorOrderId(UUID.randomUUID().toString())
        MarketPlaceOrderValueObject.Item item = new MarketPlaceOrderValueObject.Item()
        item.setExternalItemId("1234")
        item.setItemDescription("burger")
        item.setItemIdentifier(new MarketPlaceOrderValueObject.ItemIdentifier())
        item.setQuantity(1)

        valueObject.setItems(Collections.singletonList(item))

        when:
        CreateOmsOrderCommand command = MarketPlaceVoToOMSCommandMapper.INSTANCE.convertToCommand(valueObject, Tenant.ASDA, Vertical.ASDAGR, "CIN")

        then:
        command.getTenant() == Tenant.ASDA
        command.getVertical() == Vertical.ASDAGR
        command.getContactInfo().getFirstName().equalsIgnoreCase("A")
        command.getContactInfo().getLastName().equalsIgnoreCase("B")
        command.getOrderInfo().getStoreId().equalsIgnoreCase("5755")
        command.getOrderInfo().getPickupLocationId().equalsIgnoreCase("5755")
        command.getOrderInfo().getDeliveryDate() == orderDueTime
        command.getSchedulingInfo().getPlannedDueTime() == orderDueTime
        command.getSchedulingInfo().getScheduleNumber().equalsIgnoreCase("CIN")
        command.getMarketPlaceInfo().getVendor() == Vendor.TESTVENDOR
        command.getOrderInfo().getSourceOrderId().equalsIgnoreCase(valueObject.getSourceOrderId())
        command.getMarketPlaceInfo().getVendorOrderId().equalsIgnoreCase(valueObject.getVendorOrderId())
        command.getOrderItemInfoList().size() == 1
        command.getOrderItemInfoList().get(0).getBundledItemList() == null
        command.getOrderItemInfoList().get(0).getSubstitutionOption() !=null
        command.getOrderItemInfoList().get(0).getSubstitutionOption() == SubstitutionOption.DO_NOT_SUBSTITUTE
    }

    def "Marketplace VO to Command Mapper for order with bundles"() {

        given:
        MarketPlaceOrderValueObject valueObject = new MarketPlaceOrderValueObject()
        MarketPlaceOrderValueObject.ContactInfo contactInfo = new MarketPlaceOrderValueObject.ContactInfo()
        contactInfo.setFirstName("A")
        contactInfo.setLastName("B")
        valueObject.setContactInfo(contactInfo)
        valueObject.setStoreId("5755")
        Date orderDueTime = new Date()
        valueObject.setOrderDueTime(orderDueTime)
        valueObject.setVendorId(Vendor.TESTVENDOR)
        valueObject.setSourceOrderId(UUID.randomUUID().toString())
        valueObject.setVendorOrderId(UUID.randomUUID().toString())
        MarketPlaceOrderValueObject.BundleItem bundleItem = new MarketPlaceOrderValueObject.BundleItem()
        bundleItem.setBundleDescription("burger and pepsi")
        bundleItem.setBundleInstanceId("bundle1")
        bundleItem.setBundleQuantity(1)
        bundleItem.setBundleSkuId("1218929317391")

        MarketPlaceOrderValueObject.Item item = new MarketPlaceOrderValueObject.Item()
        item.setExternalItemId("1234")
        item.setItemDescription("burger")
        item.setItemIdentifier(new MarketPlaceOrderValueObject.ItemIdentifier())
        item.setQuantity(1)
        item.setBundledItemList(Arrays.asList(bundleItem))

        MarketPlaceOrderValueObject.Item item2 = new MarketPlaceOrderValueObject.Item()
        item2.setExternalItemId("1235")
        item2.setItemDescription("pepsi")
        item2.setItemIdentifier(new MarketPlaceOrderValueObject.ItemIdentifier())
        item2.setQuantity(1)
        item2.setBundledItemList(Arrays.asList(bundleItem))

        valueObject.setItems(Arrays.asList(item, item2))

        when:
        CreateOmsOrderCommand command = MarketPlaceVoToOMSCommandMapper.INSTANCE.convertToCommand(valueObject, Tenant.ASDA, Vertical.ASDAGR, "CIN")

        then:
        command.getTenant() == Tenant.ASDA
        command.getVertical() == Vertical.ASDAGR
        command.getContactInfo().getFirstName().equalsIgnoreCase("A")
        command.getContactInfo().getLastName().equalsIgnoreCase("B")
        command.getOrderInfo().getStoreId().equalsIgnoreCase("5755")
        command.getOrderInfo().getPickupLocationId().equalsIgnoreCase("5755")
        command.getOrderInfo().getDeliveryDate() == orderDueTime
        command.getSchedulingInfo().getPlannedDueTime() == orderDueTime
        command.getSchedulingInfo().getScheduleNumber().equalsIgnoreCase("CIN")
        command.getMarketPlaceInfo().getVendor() == Vendor.TESTVENDOR
        command.getOrderInfo().getSourceOrderId().equalsIgnoreCase(valueObject.getSourceOrderId())
        command.getMarketPlaceInfo().getVendorOrderId().equalsIgnoreCase(valueObject.getVendorOrderId())
        command.getOrderItemInfoList().size() == 2
        command.getOrderItemInfoList().get(0).getBundledItemList() != null
        command.getOrderItemInfoList().get(1).getBundledItemList() != null
        command.getOrderItemInfoList().get(0).getBundledItemList().get(0).getBundleInstanceId() == "bundle1"
        command.getOrderItemInfoList().get(1).getBundledItemList().get(0).getBundleInstanceId() == "bundle1"
        command.getOrderItemInfoList().get(0).getSubstitutionOption() !=null
        command.getOrderItemInfoList().get(0).getSubstitutionOption() == SubstitutionOption.DO_NOT_SUBSTITUTE
    }
}
