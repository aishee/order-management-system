package com.walmart.oms.commands.mappers

import com.walmart.common.domain.type.CancellationSource
import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.oms.commands.PickCompleteCommand
import com.walmart.oms.order.valueobject.events.CancellationDetailsValueObject
import com.walmart.oms.order.valueobject.events.FmsOrderItemvalueObject
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject
import com.walmart.oms.order.valueobject.events.FmsPickedItemUpcVo
import com.walmart.oms.order.valueobject.events.FmsPickedItemValueObject
import spock.lang.Specification

class FmsOrderValueObjectToPickCommandMapperSpec extends Specification {

    FmsOrderValueObjectToPickCommandMapper fmsOrderValueObjectToPickCommandMapper
    String sourceOrderId = UUID.randomUUID().toString()

    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        fmsOrderValueObjectToPickCommandMapper = new FmsOrderValueObjectToPickCommandMapperImpl()
    }

    def "convert FmsOrderValueObject to PickCompleteCommand"() {
        FmsOrderValueObject fmsOrderValueObject = new FmsOrderValueObject(
                sourceOrderId: sourceOrderId,
                storeId: "4401",
                deliveryDate: new Date(),
                tenant: Tenant.ASDA,
                vertical: Vertical.MARKETPLACE,
                spokeStoreId: "12345",
                pickupLocationId: "45678",
                cancellationDetails: new CancellationDetailsValueObject(
                        cancelledBy: CancellationSource.STORE,
                        cancelledReasonCode: "CANCELLED_AT_STORE")
        )


        when:
        PickCompleteCommand convertedPickCompleteObj = fmsOrderValueObjectToPickCommandMapper.convertToCommand(fmsOrderValueObject)

        then:
        convertedPickCompleteObj.sourceOrderId == fmsOrderValueObject.sourceOrderId
        convertedPickCompleteObj.tenant == fmsOrderValueObject.tenant
        convertedPickCompleteObj.vertical == fmsOrderValueObject.vertical
        PickCompleteCommand.OmsOrderData convertedOmsOrderData = convertedPickCompleteObj.data
        convertedOmsOrderData.orderInfo.storeId == fmsOrderValueObject.storeId
        convertedOmsOrderData.orderInfo.pickupLocationId == fmsOrderValueObject.pickupLocationId
        convertedOmsOrderData.orderInfo.authStatus == fmsOrderValueObject.authStatus
    }

    def "convert FmsOrderValueObject to PickCompleteCommand for substitution Items"() {
        FmsOrderValueObject fmsOrderValueObject = new FmsOrderValueObject(
                sourceOrderId: sourceOrderId,
                storeId: "4401",
                deliveryDate: new Date(),
                tenant: Tenant.ASDA,
                vertical: Vertical.MARKETPLACE,
                spokeStoreId: "12345",
                pickupLocationId: "45678",
                fmsOrderItemvalueObjectList: Arrays.asList(FmsOrderItemvalueObject.builder()
                        .itemDescription("Fanta")
                        .quantity(3L)
                        .uom("EACH")
                        .pickedItem(mockPickedItemWithSubstitution())
                        .build())
        )

        when:
        PickCompleteCommand convertedPickCompleteObj =
                fmsOrderValueObjectToPickCommandMapper.convertToCommand(fmsOrderValueObject)

        then:
        convertedPickCompleteObj.sourceOrderId == fmsOrderValueObject.sourceOrderId
        convertedPickCompleteObj.tenant == fmsOrderValueObject.tenant
        convertedPickCompleteObj.vertical == fmsOrderValueObject.vertical
        PickCompleteCommand.OmsOrderData convertedOmsOrderData = convertedPickCompleteObj.data
        convertedOmsOrderData.orderInfo.storeId == fmsOrderValueObject.storeId
        convertedOmsOrderData.orderInfo.pickupLocationId == fmsOrderValueObject.pickupLocationId
        convertedOmsOrderData.orderInfo.authStatus == fmsOrderValueObject.authStatus
        convertedOmsOrderData.getPickedItems().size() == 1
        convertedOmsOrderData.getPickedItems().get(0) != null
        convertedOmsOrderData.getPickedItems().get(0).substitutedItems.size() == 1
        PickCompleteCommand.SubstitutedItemInfo substitutedItemInfo =
                convertedOmsOrderData.getPickedItems().get(0).substitutedItems.get(0);
        substitutedItemInfo.getUpcs().get(0).getUpc() == "222"
        substitutedItemInfo.getUpcs().get(0).getUom() == "KG"
        substitutedItemInfo.getDepartment() == "31"
        substitutedItemInfo.getDescription() == "COCA-COLA"
        substitutedItemInfo.getQuantity() == 3L
        substitutedItemInfo.getWeight() == 3.5
        substitutedItemInfo.getConsumerItemNumber() == "123"
        substitutedItemInfo.getWalmartItemNumber() == "4321"
        substitutedItemInfo.getTotalPrice() == BigDecimal.valueOf(30L)
        substitutedItemInfo.getUnitPrice() == BigDecimal.TEN
    }

    def "convert FmsOrderValueObject to PickCompleteCommand when null is passed"() {
        when:
        PickCompleteCommand convertedPickCompleteObj = fmsOrderValueObjectToPickCommandMapper.convertToCommand(null)

        then:
        convertedPickCompleteObj == null
    }

    def "convert FmsOrderItemvalueObject to PickCompleteCommand.PickedItemInfo "() {
        FmsOrderItemvalueObject fmsOrderItemvalueObject = new FmsOrderItemvalueObject(

                pickedItem: new FmsPickedItemValueObject(
                        orderedCin: "2468",
                        departmentID: "369",
                        pickerUserName: "John Wick",
                        pickedItemDescription: "Dog",
                        pickedItemUpcList: [new FmsPickedItemUpcVo(), new FmsPickedItemUpcVo()]
                )
        )

        when:
        PickCompleteCommand.PickedItemInfo convertedPickItemInfo =
                fmsOrderValueObjectToPickCommandMapper.convertToPickedItemInfo(fmsOrderItemvalueObject)

        then:
        convertedPickItemInfo.cin == "2468"
        convertedPickItemInfo.departmentId == "369"
        convertedPickItemInfo.pickedItemUpcs.size() == 2
        convertedPickItemInfo.pickedBy == "John Wick"
        convertedPickItemInfo.pickedItemDescription == "Dog"
    }

    def "convert FmsOrderItemvalueObject to PickCompleteCommand.PickedItemInfo when null is passed "() {
        when:
        PickCompleteCommand.PickedItemInfo convertedPickItemInfo =
                fmsOrderValueObjectToPickCommandMapper.convertToPickedItemInfo(null)

        then:
        convertedPickItemInfo == null
    }

    def "convert FmsPickedItemUpcVo to PickCompleteCommand.PickedItemUpc"() {
        FmsPickedItemUpcVo pickedItemUpcVo = new FmsPickedItemUpcVo(
                quantity: 2,
                storeUnitPrice: 200
        )

        when:
        PickCompleteCommand.PickedItemUpc convertedPickedItemUpc =
                fmsOrderValueObjectToPickCommandMapper.convertToPickedItemUpc(pickedItemUpcVo)

        then:
        convertedPickedItemUpc.pickedQuantity == 2
        convertedPickedItemUpc.unitPrice == 200
    }

    def "convert FmsPickedItemUpcVo to PickCompleteCommand.PickedItemUpc when null is passed"() {
        when:
        PickCompleteCommand.PickedItemUpc convertedPickedItemUpc =
                fmsOrderValueObjectToPickCommandMapper.convertToPickedItemUpc(null)

        then:
        convertedPickedItemUpc == null
    }

    private FmsPickedItemValueObject mockPickedItemWithSubstitution() {
        return FmsPickedItemValueObject.builder()
                .quantity(3L)
                .substitutedItems(Arrays.asList(FmsPickedItemValueObject.FmsSubstitutedItemValueObject.builder()
                        .quantity(3L)
                        .department("31")
                        .description("COCA-COLA")
                        .consumerItemNumber("123")
                        .walmartItemNumber("4321")
                        .weight(3.5)
                        .upcs(Arrays.asList(FmsPickedItemValueObject.SubstitutedItemUpcValueObject.builder()
                                .upc("222")
                                .uom("KG")
                                .build()
                        )).substitutedItemPriceInfo(FmsPickedItemValueObject.SubstitutedItemPriceInfoValueObject.builder()
                        .totalPrice(BigDecimal.valueOf(30L))
                        .unitPrice(BigDecimal.TEN)
                        .build()).build()
                )).build()
    }
}