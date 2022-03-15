package com.walmart.marketplace.infrastructure.gateway.uber

import com.walmart.common.domain.type.Currency
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberOrder
import com.walmart.marketplace.infrastructure.gateway.uber.dto.response.UberStore
import com.walmart.marketplace.infrastructure.gateway.uber.report.dto.UberReportReq
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem
import com.walmart.marketplace.order.domain.entity.type.Vendor
import com.walmart.marketplace.order.domain.uber.MarketPlaceReportRequest
import com.walmart.marketplace.order.domain.uber.PatchCartInfo
import com.walmart.marketplace.order.domain.uber.ReportType
import com.walmart.marketplace.order.domain.entity.UpdateItemInfo
import com.walmart.marketplace.order.domain.valueobject.Money
import com.walmart.marketplace.repository.MarketPlaceRepository
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.time.LocalDate

class UberOrderGatewaySpec extends Specification {

    UberServiceConfiguration uberServiceConfiguration = Mock()
    UberWebClient uberWebClient = Mock()
    UberOrderGateway uberGateway = Mock()
    MarketPlaceRepository marketPlaceRepository = Mock()
    String externalOrderId = UUID.randomUUID().toString()

    def setup() {
        uberGateway = new UberOrderGateway(
                uberWebClient: uberWebClient,
                marketPlaceRepository: marketPlaceRepository,
                uberServiceConfiguration: uberServiceConfiguration
        )
    }

    def "When Empty or Null ID List is passed to Get Uber"() {

        when:
        Object uberOrderForNull = uberGateway.getOrder(null, null)
        Object uberOrderForEmpty = uberGateway.getOrder("", "")

        then:
        uberOrderForNull == null
        uberOrderForEmpty == null
        uberWebClient
    }


    def "When valid Uber order is passed for Get Order"() {
        given:
        uberWebClient.getUberOrder("12345") >> { return createSampleUberOrder() }
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()


        when:
        Object validUberOrder = uberGateway.getOrder("12345", "")

        then:
        validUberOrder.vendorOrderId == "12345"
        validUberOrder.id != null
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.unitPrice == Double.valueOf(2.5)
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.totalPrice == Double.valueOf(5.0)
        validUberOrder.getOrderDueTime() == null
    }

    def "When valid Uber order is passed for Get Order without estimated ready for picked up date"() {
        given:
        uberWebClient.getUberOrder("12345") >> { return createSampleUberOrderWithoutPickedUpDateAndWithPlacedAtDate() }
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()
        uberServiceConfiguration.getDefaultDeliveryTimeInMinutes() >> 30

        when:
        Object validUberOrder = uberGateway.getOrder("12345", "")

        then:
        validUberOrder.vendorOrderId == "12345"
        validUberOrder.id != null
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.unitPrice == Double.valueOf(2.5)
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.totalPrice == Double.valueOf(5.0)
        validUberOrder.getOrderDueTime() == new Date(2020, 11, 1, 9, 50, 10)
    }

    UberOrder createSampleUberOrderWithoutPickedUpDateAndWithPlacedAtDate() {
        String externalOrderId = UUID.randomUUID().toString()

        UberOrder.Store store = new UberOrder.Store(
                externalReferenceId: "4401"

        )
        UberOrder.Eater eater = new UberOrder.Eater(
                firstName: "John",
                lastName: "Doe")

        UberOrder.Cart cart = new UberOrder.Cart(
                items: Arrays.asList(new UberOrder.Item(
                        externalData: "4646467",
                        quantity: 2.0,
                        id: "36363",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        ))))
        UberOrder.Payment payment = new UberOrder.Payment(
                charges: new UberOrder.Charges(total: new UberOrder.Money(amount: 350),
                        bagFee: new UberOrder.Money(amount: 0),
                        totalFee: new UberOrder.Money(amount: 0),
                        totalFeeTax: new UberOrder.Money(amount: 0),
                        tax: new UberOrder.Money(amount: 0),
                        subTotal: new UberOrder.Money(amount: 0)

                ))

        Date placedAt = new Date(2020, 11, 1, 9, 20, 10)
        return new UberOrder(
                id: "12345",
                displayId: "ABCD",
                externalReferenceId: externalOrderId,
                store: store,
                eater: eater,
                cart: cart,
                payment: payment,
                placedAt: placedAt)

    }

    def "When valid Uber order is passed for Get Order with estimated ready for picked up date"() {
        given:
        uberWebClient.getUberOrder("12345") >> { return createSampleUberOrderWithEstimatedPickedUpDate() }
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()
        uberServiceConfiguration.getDefaultDeliveryTimeInMinutes() >> 30

        when:
        Object validUberOrder = uberGateway.getOrder("12345", "")

        then:
        validUberOrder.vendorOrderId == "12345"
        validUberOrder.id != null
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.unitPrice == Double.valueOf(2.5)
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.totalPrice == Double.valueOf(5.0)
        validUberOrder.getOrderDueTime() == new Date(2020, 11, 1, 9, 20, 10)
    }

    UberOrder createSampleUberOrderWithEstimatedPickedUpDate() {
        String externalOrderId = UUID.randomUUID().toString()

        UberOrder.Store store = new UberOrder.Store(
                externalReferenceId: "4401"

        )
        UberOrder.Eater eater = new UberOrder.Eater(
                firstName: "John",
                lastName: "Doe")

        UberOrder.Cart cart = new UberOrder.Cart(
                items: Arrays.asList(new UberOrder.Item(
                        externalData: "4646467",
                        quantity: 2.0,
                        id: "36363",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        ))))
        UberOrder.Payment payment = new UberOrder.Payment(
                charges: new UberOrder.Charges(total: new UberOrder.Money(amount: 350),
                        bagFee: new UberOrder.Money(amount: 0),
                        totalFee: new UberOrder.Money(amount: 0),
                        totalFeeTax: new UberOrder.Money(amount: 0),
                        tax: new UberOrder.Money(amount: 0),
                        subTotal: new UberOrder.Money(amount: 0)

                ))

        Date estimatedReadyForPickupAt = new Date(2020, 11, 1, 9, 20, 10);
        return new UberOrder(
                id: "12345",
                displayId: "ABCD",
                externalReferenceId: externalOrderId,
                store: store,
                eater: eater,
                cart: cart,
                payment: payment,
                estimatedReadyForPickupAt: estimatedReadyForPickupAt)

    }

    UberOrder createSampleUberOrder() {
        String externalOrderId = UUID.randomUUID().toString()

        UberOrder.Store store = new UberOrder.Store(
                externalReferenceId: "4401"

        )
        UberOrder.Eater eater = new UberOrder.Eater(
                firstName: "John",
                lastName: "Doe")

        UberOrder.Cart cart = new UberOrder.Cart(
                items: Arrays.asList(new UberOrder.Item(
                        externalData: "4646467",
                        quantity: 2.0,
                        id: "36363",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        ))))
        UberOrder.Payment payment = new UberOrder.Payment(
                charges: new UberOrder.Charges(total: new UberOrder.Money(amount: 350),
                        bagFee: new UberOrder.Money(amount: 0),
                        totalFee: new UberOrder.Money(amount: 0),
                        totalFeeTax: new UberOrder.Money(amount: 0),
                        tax: new UberOrder.Money(amount: 0),
                        subTotal: new UberOrder.Money(amount: 0)

                ))


        return new UberOrder(
                id: "12345",
                displayId: "ABCD",
                externalReferenceId: externalOrderId,
                store: store,
                eater: eater,
                cart: cart,
                payment: payment)

    }

    def "When Empty or Null ID List is passed to Accept Uber Order"() {

        when:
        boolean orderAcceptedForNull = uberGateway.acceptOrder(null, "accepted")
        boolean orderAcceptedForEmpty = uberGateway.acceptOrder("", "accepted")

        then:
        !orderAcceptedForNull
        !orderAcceptedForEmpty
    }

    def "When Valid Order ID is passed to Accept Uber Order"() {

        given:
        uberWebClient.acceptUberOrder(_ as String, _ as String) >> { return true }

        when:
        boolean validOrderAccepted = uberGateway.acceptOrder("63747b7d-b58d-4c3d-9340-d15d5afcd013", "accepted")

        then:
        validOrderAccepted

    }


    def "When Empty or Null ID List is passed to Deny Uber Order"() {

        when:
        boolean orderDeniedForNull = uberGateway.denyOrder(null, "denied", null, null)
        boolean orderDeniedForEmpty = uberGateway.denyOrder("", "accepted", _ as List, _ as List)

        then:
        !orderDeniedForNull
        !orderDeniedForEmpty
    }

    def "When Valid Order ID is passed to Deny Uber Order"() {

        given:
        uberWebClient.denyUberOrder(_ as String, _ as String, _, _) >> { return true }

        when:
        boolean validOrderDenied = uberGateway.denyOrder("63747b7d", "NOTES_NOT_SUPPORTED", null, null)

        then:
        validOrderDenied

    }

    def "When Empty or Null ID List is passed to Cancel Uber Order"() {

        when:
        boolean orderCancelledForNull = uberGateway.cancelUberOrder(null, "OUT_OF_ITEMS", null)
        boolean orderCancelledForEmpty = uberGateway.cancelUberOrder("", "OUT_OF_ITEMS", null)

        then:
        !orderCancelledForNull
        !orderCancelledForEmpty
    }

    def "When Valid Order ID is passed to Cancel Uber Order"() {

        given:
        uberWebClient.cancelUberOrder(_ as String, _ as String, _) >> { return true }

        when:
        boolean validOrderCancelled = uberGateway.cancelUberOrder("63747b7d", "OUT_OF_ITEMS", null)

        then:
        validOrderCancelled

    }

    def "When Empty or Null ID is passed to Patch Cart Nil Picks"() {

        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>()
        List<String> instanceId = new ArrayList<>()
        instanceId.add("e2066983-7793-4017-ac06-74785bfeff15")
        instanceId.add("273e9df8-838e-476b-b899-b23795b55b6e")
        PatchCartInfo patchCartInfoForNull = getPatchCartInfo(null, instanceId, partialPickInstanceIds)
        PatchCartInfo patchCartInfoForEmpty = getPatchCartInfo("", instanceId, partialPickInstanceIds)

        when:
        CompletableFuture<Boolean> patchCartForNull = uberGateway.patchCart(patchCartInfoForNull)
        CompletableFuture<Boolean> patchCartForEmpty = uberGateway.patchCart(patchCartInfoForEmpty)

        then:
        assert patchCartForNull.get() == false
        assert patchCartForEmpty.get() == false
    }

    def "When Valid Order ID is passed to Patch Cart Nil Picks"() {

        given:
        List<String> instanceId = new ArrayList<>()
        instanceId.add("e2066983-7793-4017-ac06-74785bfeff15")
        instanceId.add("273e9df8-838e-476b-b899-b23795b55b6e")
        Map<String, Integer> partialPickInstanceIds = new HashMap<>()
        PatchCartInfo patchCartInfo = getPatchCartInfo(externalOrderId, instanceId, partialPickInstanceIds)
        uberWebClient.patchCart(_ as PatchCartInfo) >> { return CompletableFuture.completedFuture(true) }

        when:
        CompletableFuture<Boolean> validPatchCart = uberGateway.patchCart(patchCartInfo)

        then:
        assert validPatchCart.get() == true
    }

    def "When Empty or Null ID is passed to Patch Cart Partial Picks"() {
        given:
        List<String> nilPickInstanceIds = new ArrayList<>();
        Map<String, Integer> instanceIds = new HashMap<>();
        instanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        PatchCartInfo patchCartInfoForNull = getPatchCartInfo(null, nilPickInstanceIds, instanceIds)
        PatchCartInfo patchCartInfoForEmpty = getPatchCartInfo("", nilPickInstanceIds, instanceIds)

        when:
        CompletableFuture<Boolean> validPatchCartForNull = uberGateway.patchCart(patchCartInfoForNull)
        CompletableFuture<Boolean> validPatchCartForEmpty = uberGateway.patchCart(patchCartInfoForEmpty)

        then:
        assert validPatchCartForEmpty.get() == false
        assert validPatchCartForNull.get() == false
    }

    def "When Valid Order ID is passed to Patch Cart Partial Picks"() {

        given:
        List<String> nilPickInstanceIds = new ArrayList<>();
        Map<String, Integer> instanceIds = new HashMap<>();
        instanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        PatchCartInfo patchCartInfo = getPatchCartInfo(externalOrderId, nilPickInstanceIds, instanceIds)
        uberWebClient.patchCart(_ as PatchCartInfo) >> { return CompletableFuture.completedFuture(true) }

        when:
        CompletableFuture<Boolean> validPatchCart = uberGateway.patchCart(patchCartInfo)

        then:
        assert validPatchCart.get() == true

    }

    def "When Valid Order ID is passed to Patch Cart Nil Pick and Partial Picks"() {

        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>();
        partialPickInstanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        List<String> nilPickInstanceIds = new ArrayList<>()
        nilPickInstanceIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        nilPickInstanceIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        PatchCartInfo patchCartInfo = getPatchCartInfo(externalOrderId, nilPickInstanceIds, partialPickInstanceIds)
        uberWebClient.patchCart(_ as PatchCartInfo) >> { return CompletableFuture.completedFuture(true) }

        when:
        CompletableFuture<Boolean> validPatchCart = uberGateway.patchCart(patchCartInfo)

        then:
        assert validPatchCart.get() == true
    }

    def "When Empty or Null vendorStoreID is passed to Update Item endpoint"() {

        given:
        List<String> outOfStockItemIds = new ArrayList<>();
        outOfStockItemIds.add("12123")
        outOfStockItemIds.add("13123")
        UpdateItemInfo updateItemInfoForNull = getUpdateItemInfo(null, outOfStockItemIds)
        UpdateItemInfo updateItemInfoForEmpty = getUpdateItemInfo("", outOfStockItemIds)

        when:
        CompletableFuture<List<Boolean>> updateItemForNull = uberGateway.updateItem(updateItemInfoForNull)
        CompletableFuture<List<Boolean>> updateItemForEmpty = uberGateway.updateItem(updateItemInfoForEmpty)

        then:
        0 * uberWebClient.updateItem(_ as UpdateItemInfo, _ as String)
        updateItemForEmpty.get().size() == 0
        updateItemForNull.get().size() == 0
    }

    def "When Valid Order vendorStoreID is passed to Update Item endpoint"() {

        given:
        String vendorStoreId = "a48c2238-5f8d-48ad-917b-b91e2d577cdc"
        List<String> outOfStockItemIds = new ArrayList<>()
        outOfStockItemIds.add("12123")
        outOfStockItemIds.add("13132")
        UpdateItemInfo updateItemInfo = getUpdateItemInfo(vendorStoreId, outOfStockItemIds)
        uberWebClient.updateItem(_ as UpdateItemInfo, _ as String) >> { return CompletableFuture.completedFuture(true) }

        when:
        CompletableFuture<List<Boolean>> updateItem = uberGateway.updateItem(updateItemInfo)

        then:
        assert updateItem.get().size() == 2
    }

    def "Test getMarketplaceMoneyFromUberMoney with null and 0 UberMoney"() {

        given:
        UberOrder.Money nullUberMoney = null
        UberOrder.Money zeroUberMoney = new UberOrder.Money(
                amount: 0,
                currencyCode: "GBP"
        )

        when:
        Money nullMarketPlaceMoney = uberGateway.getMarketplaceMoneyFromUberMoney(nullUberMoney)
        Money zeroMarketPlaceMoney = uberGateway.getMarketplaceMoneyFromUberMoney(zeroUberMoney)

        then:
        nullMarketPlaceMoney == null
        zeroMarketPlaceMoney.amount == 0

    }

    def "Test getMarketplaceMoneyFromUberMoney with Valid UberMoney"() {

        given:
        int uberAmountLessThanOnePound = 98
        int uberAmountMoreThanOnePound = 998

        UberOrder.Money uberMoneyLessThanOnePound = new UberOrder.Money(
                amount: uberAmountLessThanOnePound,
                currencyCode: "GBP"
        )
        UberOrder.Money uberMoneyMoreThanOnePound = new UberOrder.Money(
                amount: uberAmountMoreThanOnePound,
                currencyCode: "GBP"
        )

        when:
        Money marketPlaceMoneyLessThanOnePound = uberGateway.getMarketplaceMoneyFromUberMoney(uberMoneyLessThanOnePound)
        Money marketPlaceMoneyMoreThanOnePound = uberGateway.getMarketplaceMoneyFromUberMoney(uberMoneyMoreThanOnePound)

        then:
        marketPlaceMoneyLessThanOnePound.amount == uberAmountLessThanOnePound / 100
        marketPlaceMoneyLessThanOnePound.currency == Currency.GBP

        marketPlaceMoneyMoreThanOnePound.amount == uberAmountMoreThanOnePound / 100
        marketPlaceMoneyMoreThanOnePound.currency == Currency.GBP

    }

    def "When a valid uber order containing duplicate items is passed"() {
        given:
        uberWebClient.getUberOrder("12345") >> { return createSampleUberOrderWithDuplicateItems() }
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()


        when:
        Object validUberOrder = uberGateway.getOrder("12345", "")

        then:
        validUberOrder.vendorOrderId == "12345"
        validUberOrder.id != null
        validUberOrder.marketPlaceItems.size() == 2

        validUberOrder.marketPlaceItems.sort(new SortbyId())
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.unitPrice == Double.valueOf(2.5)
        validUberOrder.marketPlaceItems[0].marketPlacePriceInfo.totalPrice == Double.valueOf(5.0)

        validUberOrder.marketPlaceItems[1].marketPlacePriceInfo.unitPrice == Double.valueOf(2.5)
        validUberOrder.marketPlaceItems[1].quantity == 3
        validUberOrder.marketPlaceItems[1].marketPlacePriceInfo.totalPrice == Double.valueOf(7.5)

    }

    def "When a valid uber order containing bundled items is passed"() {
        given:
        uberWebClient.getUberOrder("12345") >> { return createSampleUberOrderWithBundledItems() }
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()

        when:
        Object validUberOrder = uberGateway.getOrder("12345", "")

        then:
        validUberOrder.vendorOrderId == "12345"
        validUberOrder.id != null
        validUberOrder.marketPlaceItems.size() == 5
    }

    def "When a valid uber order containing items as part of bundle and nonbundle items is passed"() {
        given:
        uberWebClient.getUberOrder("12345") >> { return createSampleUberOrderWithItemAsBundledAndNonbundledItems() }
        marketPlaceRepository.getNextIdentity() >> UUID.randomUUID().toString()

        when:
        Object validUberOrder = uberGateway.getOrder("12345", "")

        then:
        validUberOrder.vendorOrderId == "12345"
        validUberOrder.id != null
        validUberOrder.marketPlaceItems.size() == 3

        validUberOrder.getMarketPlaceItems().forEach({ item ->
            if (item.getItemId() == "4646468" || item.getItemId() == "4646469") {
                assert item.getQuantity() == 5
                assert item.getBundledItemList() != null && item.getBundledItemList().size() == 1 && item.getBundledItemList()[0].getBundleQuantity() == 2
            } else if(item.getItemId() == "5012871") {
                assert item.getBundledItemList() != null && item.getBundledItemList().size() == 1
                assert item.getQuantity() == 2  && item.getBundledItemList()[0].getBundleQuantity() == 2
                assert item.getBundledItemList()[0].getItemQuantity() == 1
                assert item.getVendorInstanceId() == ""
                assert item.getBundledItemList()[0].getBundleInstanceId() == "1c0cb514-3b5d-4211-9bce-111111111"

            }
        })
    }

    def "When Uber Downtime Report is Success"() {
        given:
        uberWebClient.getUberStore() >> { return createSampleUberStore() }
        uberWebClient.invokeUberReport(createUberReportReq(createDownTimeUberReportRequest())) >> { return "Test" }


        when:
        String workFlowId = uberGateway.invokeMarketPlaceReport(createDownTimeUberReportRequest())

        then:
        workFlowId == null

    }

    private PatchCartInfo getPatchCartInfo(String vendorOrderId, List<String> nilPicks, Map<String, Integer> partialPicks) {
        return PatchCartInfo.builder()
                .vendorOrderId(vendorOrderId)
                .vendorId(Vendor.UBEREATS)
                .nilPickInstanceIds(nilPicks)
                .partialPickInstanceIds(partialPicks)
                .storeId("4376")
                .build()
    }

    private UpdateItemInfo getUpdateItemInfo(String vendorStoreId, List<String> outOfStockItemIds) {
        return UpdateItemInfo.builder()
                .vendorOrderId(externalOrderId)
                .vendorStoreId(vendorStoreId)
                .vendorId(Vendor.UBEREATS)
                .outOfStockItemIds(outOfStockItemIds)
                .suspendUntil(189)
                .reason("OUT_OF_STOCK")
                .storeId("4376")
                .build()
    }

    class SortbyId implements Comparator<MarketPlaceItem> {

        int compare(MarketPlaceItem a, MarketPlaceItem b) {
            return Long.valueOf(a.getExternalItemId()) - Long.valueOf(b.getExternalItemId())
        }
    }

    UberOrder createSampleUberOrderWithDuplicateItems() {
        String externalOrderId = UUID.randomUUID().toString()

        UberOrder.Store store = new UberOrder.Store(
                externalReferenceId: "4401"

        )
        UberOrder.Eater eater = new UberOrder.Eater(
                firstName: "John",
                lastName: "Doe")

        UberOrder.Cart cart = new UberOrder.Cart(
                items: Arrays.asList(new UberOrder.Item(
                        externalData: "4646467",
                        quantity: 2.0,
                        id: "36363",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        )), new UberOrder.Item(
                        externalData: "4646468",
                        quantity: 2.0,
                        id: "36363",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        )), new UberOrder.Item(
                        externalData: "4646468",
                        quantity: 1.0,
                        id: "36363",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 250)

                        )))
        )
        UberOrder.Payment payment = new UberOrder.Payment(
                charges: new UberOrder.Charges(total: new UberOrder.Money(amount: 350),
                        bagFee: new UberOrder.Money(amount: 0),
                        totalFee: new UberOrder.Money(amount: 0),
                        totalFeeTax: new UberOrder.Money(amount: 0),
                        tax: new UberOrder.Money(amount: 0),
                        subTotal: new UberOrder.Money(amount: 0)

                ))

        return new UberOrder(
                id: "12345",
                displayId: "ABCD",
                externalReferenceId: externalOrderId,
                store: store,
                eater: eater,
                cart: cart,
                payment: payment)

    }

    UberOrder createSampleUberOrderWithBundledItems() {
        String externalOrderId = UUID.randomUUID().toString()

        UberOrder.Store store = new UberOrder.Store(
                externalReferenceId: "4401"

        )
        UberOrder.Eater eater = new UberOrder.Eater(
                firstName: "John",
                lastName: "Doe")

        UberOrder.Cart cart = new UberOrder.Cart(
                items: Arrays.asList(new UberOrder.Item(
                        externalData: "123456",
                        quantity: 1.0,
                        id: "123456",
                        instanceId: "1c0cb514-3b5d-4211-9bce-111111111",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        ),
                        selectedModifierGroups : Arrays.asList(
                                new UberOrder.ModifierGroup(
                                        id: "One-Sandwich",
                                        title: "Choose the Sandwich or Salad for Lunch",
                                        selectedItems: Arrays.asList(
                                                new UberOrder.Item(
                                                        externalData: "4646468",
                                                        quantity: 2.0,
                                                        id: "4646468",
                                                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)),
                                                        title: "ASDA BLT Sandwich Each"),
                                                new UberOrder.Item(
                                                        externalData: "4646469",
                                                        quantity: 1.0,
                                                        id: "4646469",
                                                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 250)),
                                                        title: "Chicken sandwich"

                                                )
                                        )
                                ),
                                new UberOrder.ModifierGroup(
                                        id: "One-Juice",
                                        title: "Choose the Drink for Lunch",
                                        selectedItems: Arrays.asList(
                                                new UberOrder.Item(
                                                        externalData: "5012871",
                                                        quantity: 1.0,
                                                        id: "5012871",
                                                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 168), totalPrice: new UberOrder.Money(amount: 168)
                                                        ), title: "Tropicana Smooth Orange Juice 300ML")
                                        )
                                )
                        )
                )

                        , new UberOrder.Item(
                        externalData: "707070",
                        quantity: 2.0,
                        id: "707070",
                        instanceId: "44444444-4444-4444-4444-44444444444",
                        title: "nachos",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        )), new UberOrder.Item(
                        externalData: "717171",
                        quantity: 1.0,
                        id: "717171",
                        instanceId: "55555555-5555-5555-5555-555555555555",
                        title: "lays",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 250)

                        ))

                )
        )
        UberOrder.Payment payment = new UberOrder.Payment(
                charges: new UberOrder.Charges(total: new UberOrder.Money(amount: 350),
                        bagFee: new UberOrder.Money(amount: 0),
                        totalFee: new UberOrder.Money(amount: 0),
                        totalFeeTax: new UberOrder.Money(amount: 0),
                        tax: new UberOrder.Money(amount: 0),
                        subTotal: new UberOrder.Money(amount: 0)

                ))


        return new UberOrder(
                id: "12345",
                displayId: "ABCD",
                externalReferenceId: externalOrderId,
                store: store,
                eater: eater,
                cart: cart,
                payment: payment)

    }

    UberOrder createSampleUberOrderWithItemAsBundledAndNonbundledItems() {
        String externalOrderId = UUID.randomUUID().toString()

        UberOrder.Store store = new UberOrder.Store(
                externalReferenceId: "4401"

        )
        UberOrder.Eater eater = new UberOrder.Eater(
                firstName: "John",
                lastName: "Doe")

        UberOrder.Cart cart = new UberOrder.Cart(
                items: Arrays.asList(new UberOrder.Item(
                        externalData: "123456",
                        quantity: 2.0,
                        id: "123456",
                        instanceId: "1c0cb514-3b5d-4211-9bce-111111111",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        ),
                        selectedModifierGroups : Arrays.asList(
                                new UberOrder.ModifierGroup(
                                        id: "One-Sandwich",
                                        title: "Choose the Sandwich or Salad for Lunch",
                                        selectedItems: Arrays.asList(
                                                new UberOrder.Item(
                                                        externalData: "4646468",
                                                        quantity: 1.0,
                                                        id: "4646468",
                                                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)),
                                                        title: "ASDA BLT Sandwich Each"),
                                                new UberOrder.Item(
                                                        externalData: "4646469",
                                                        quantity: 1.0,
                                                        id: "4646469",
                                                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 250)),
                                                        title: "Chicken sandwich"

                                                )
                                        )
                                ),
                                new UberOrder.ModifierGroup(
                                        id: "One-Juice",
                                        title: "Choose the Drink for Lunch",
                                        selectedItems: Arrays.asList(
                                                new UberOrder.Item(
                                                        externalData: "5012871",
                                                        quantity: 1.0,
                                                        id: "5012871",
                                                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 168), totalPrice: new UberOrder.Money(amount: 168)
                                                        ), title: "Tropicana Smooth Orange Juice 300ML")
                                        )
                                )
                        )
                )

                        , new UberOrder.Item(
                        externalData: "4646468",
                        quantity: 3.0,
                        id: "4646468",
                        instanceId: "44444444-4444-4444-4444-44444444444",
                        title: "ASDA BLT Sandwich Each",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 500)

                        )), new UberOrder.Item(
                        externalData: "4646469",
                        quantity: 3.0,
                        id: "4646469",
                        instanceId: "55555555-5555-5555-5555-555555555555",
                        title: "Chicken sandwich",
                        price: new UberOrder.ItemPrice(unitPrice: new UberOrder.Money(amount: 250), totalPrice: new UberOrder.Money(amount: 250)

                        ))

                )
        )
        UberOrder.Payment payment = new UberOrder.Payment(
                charges: new UberOrder.Charges(total: new UberOrder.Money(amount: 350),
                        bagFee: new UberOrder.Money(amount: 0),
                        totalFee: new UberOrder.Money(amount: 0),
                        totalFeeTax: new UberOrder.Money(amount: 0),
                        tax: new UberOrder.Money(amount: 0),
                        subTotal: new UberOrder.Money(amount: 0)

                ))


        return new UberOrder(
                id: "12345",
                displayId: "ABCD",
                externalReferenceId: externalOrderId,
                store: store,
                eater: eater,
                cart: cart,
                payment: payment)

    }

    MarketPlaceReportRequest createDownTimeUberReportRequest() {
        return MarketPlaceReportRequest.builder()
                .reportType(ReportType.DOWNTIME_REPORT)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(2))
                .build()
    }

    MarketPlaceReportRequest createOrderHistoryUberReportRequest() {
        return MarketPlaceReportRequest.builder()
                .reportType(ReportType.ORDER_HISTORY_REPORT)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().minusDays(2))
                .build()
    }

    UberStore createSampleUberStore() {
        return new UberStore("test",
                Arrays.asList(new UberStore.Store("test",
                        "a48c2238-5f8d-48ad-917b-b91e2d577cdc",
                        "1234"),
                        new UberStore.Store("test",
                                "73fcf766-07b3-4640-93aa-1d8de5faac08",
                                "1234"))
        )
    }

    UberStore createSampleEmptyUberStore() {
        return new UberStore("test",
                Arrays.asList()
        )
    }

    UberReportReq createUberReportReq(MarketPlaceReportRequest uberReportRequest) {
        List<String> storeUUIDs = Arrays.asList("a48c2238-5f8d-48ad-917b-b91e2d577cdc",
                "73fcf766-07b3-4640-93aa-1d8de5faac08")
        return UberReportReq.builder()
                .endDate(uberReportRequest.getEndDate())
                .startDate(uberReportRequest.getStartDate())
                .storeUUIDs(storeUUIDs)
                .reportType(uberReportRequest.getReportType())
                .build()
    }


}
