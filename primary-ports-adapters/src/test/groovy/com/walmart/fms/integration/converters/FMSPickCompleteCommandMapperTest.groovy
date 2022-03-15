package com.walmart.fms.integration.converters

import com.walmart.common.domain.type.Tenant
import com.walmart.common.domain.type.Vertical
import com.walmart.fms.commands.FmsPickCompleteCommand
import com.walmart.fms.integration.xml.beans.orderpickcomplete.UpdateOrderPickedStatusRequest
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import java.util.stream.Collectors

class FMSPickCompleteCommandMapperTest extends Specification {
    FMSPickCompleteCommandMapper fmsPickCompleteCommandMapper
    JAXBContext orderPickCompleteJaxbContext = JAXBContext.newInstance(UpdateOrderPickedStatusRequest);

    def setup() {

        fmsPickCompleteCommandMapper = new FMSPickCompleteCommandMapperImpl()
    }

    def convertToPickcompleteCommand() {
        given:
        String xmlString = this.getClass().getResource('/fms/pickcomplete/PickCompleteValid.xml').text
        UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest = orderPickCompleteJaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString))
        when:
        FmsPickCompleteCommand fmsPickCompleteCommand = fmsPickCompleteCommandMapper.convertToPickcompleteCommand(updateOrderPickedStatusRequest)
        then:
        assert fmsPickCompleteCommand.getData().orderInfo.getStoreOrderId() == '22379099540'
        assert fmsPickCompleteCommand.getData().orderInfo.getStoreId() == '4218'
        assert fmsPickCompleteCommand.getData().orderInfo.getOrderStatus() == 'PICK_COMPLETE'
        assert fmsPickCompleteCommand.getData().orderInfo.getCancelledReasonCode() == '7'
        assert fmsPickCompleteCommand.getData().orderInfo.getTenant() == Tenant.ASDA
        assert fmsPickCompleteCommand.getData().orderInfo.getVertical() == Vertical.MARKETPLACE
        assert fmsPickCompleteCommand.getData().getPickedItems().size() == 1
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().size() == 1
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().size() == 1
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().get(0).getUpc() == '5054070874950'
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().get(0).getPickedQuantity() == 1;
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().get(0).getUom() == "EACH"
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().get(0).getWeight() == 0.0
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().get(0).getWin() == '5132342'
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemUpcs().get(0).getUnitPrice() == 4.0
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getCin() == '5373975';
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getDepartmentId() == '51'
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedBy() == 'store'
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getPickedItemDescription() == "ASDA Butcher's Selection 18 Smoked Back Bacon Rashers 600G"
    }

    def "pickCompleteValidMultipleOrderInfo"() {
        given:
        String xmlString = this.getClass().getResource('/fms/pickcomplete/PickCompleteMultipleOrderInfo.xml').text
        UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest = orderPickCompleteJaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString))
        when:
        FmsPickCompleteCommand fmsPickCompleteCommand = fmsPickCompleteCommandMapper.convertToPickcompleteCommand(updateOrderPickedStatusRequest)
        then:
        assert fmsPickCompleteCommand.getData().getPickedItems().size() == 2
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getCin() == '5373975'
        assert fmsPickCompleteCommand.getData().getPickedItems().get(1).getCin() == '6570920'
    }

    def "pickCompleteDepartmentIdNull"() {
        given:
        String xmlString = this.getClass().getResource('/fms/pickcomplete/PartialPickCompletDepartmentNull.xml').text
        UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest = orderPickCompleteJaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString))
        when:
        FmsPickCompleteCommand fmsPickCompleteCommand = fmsPickCompleteCommandMapper.convertToPickcompleteCommand(updateOrderPickedStatusRequest)
        then:
        assert fmsPickCompleteCommand.getData().getPickedItems().size() == 2
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getDepartmentId() == null
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getCin() == '544299'
    }

    def "pickCompleteAllItemsNilPickCancel"() {
        given:
        String xmlString = this.getClass().getResource('/fms/pickcomplete/All_NilPicks_CancelledStatus.xml').text
        UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest = orderPickCompleteJaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString))
        when:
        FmsPickCompleteCommand fmsPickCompleteCommand = fmsPickCompleteCommandMapper.convertToPickcompleteCommand(updateOrderPickedStatusRequest)
        then:
        assert fmsPickCompleteCommand.getData().getOrderInfo().getStoreOrderId() == '378104128013948928'
        assert fmsPickCompleteCommand.getData().getOrderInfo().getOrderStatus() == FMSPickCompleteCommandMapper.StoreOrderStatus.CANCELLED.getName()
        assert fmsPickCompleteCommand.getCancelReasonCode() == '9300'
        assert fmsPickCompleteCommand.getCancelledReasonDescription() == 'NIL PICKED'
        assert fmsPickCompleteCommand.getData().getPickedItems().size() == 1
        assert fmsPickCompleteCommand.getData().getPickedItems().get(0).getCin() == '2266209'
    }

    def "pickCompleteWithSubstitutions"() {
        given:
        String xmlString = this.getClass().getResource('/fms/pickcomplete/PickCompleteWithSubstitution.xml').text
        UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest = orderPickCompleteJaxbContext.createUnmarshaller().unmarshal(new StringReader(xmlString))
        when:
        FmsPickCompleteCommand fmsPickCompleteCommand = fmsPickCompleteCommandMapper.convertToPickcompleteCommand(updateOrderPickedStatusRequest)
        then:
        List<FmsPickCompleteCommand.PickedItemInfo> pickedItemWithSubstitutions = fmsPickCompleteCommand.getData().getPickedItems().stream().filter(
                { item -> !item.getSubstitutedItemInfoList().isEmpty() }).collect(Collectors.toList());
        List<FmsPickCompleteCommand.SubstitutedItemInfo> substitutedItemInfoList =
                pickedItemWithSubstitutions.stream().flatMap({ item -> item.getSubstitutedItemInfoList().stream() })
                        .collect(Collectors.toList())

        assert fmsPickCompleteCommand.getData().getOrderInfo().getStoreOrderId() == '9122171000200'
        assert fmsPickCompleteCommand.getData().getOrderInfo().getOrderStatus() == 'PICK_COMPLETE'
        assert fmsPickCompleteCommand.getData().getPickedItems().size() == 6
        assert pickedItemWithSubstitutions.size() == 2
        assert substitutedItemInfoList.size() == 2
        assert substitutedItemInfoList.get(0).getConsumerItemNumber() == "5996917"
        assert substitutedItemInfoList.get(0).getDescription() == "ASDA STIR FRY"
        assert substitutedItemInfoList.get(0).getWalmartItemNumber() == "50036738"
        assert substitutedItemInfoList.get(0).getUnitPrice() == 1.00
        assert substitutedItemInfoList.get(0).getDepartment() == "43"
        assert substitutedItemInfoList.get(0).getQuantity() == 1.000
        assert substitutedItemInfoList.get(0).getUpcs().get(0).getUpc() == "5057172289370"
        assert substitutedItemInfoList.get(0).getUpcs().get(0).getUom() == "EACH"
        assert substitutedItemInfoList.get(1).getConsumerItemNumber() == "5996919"
        assert substitutedItemInfoList.get(1).getDescription() == "ASDA CHICKEN WINGS"
        assert substitutedItemInfoList.get(1).getWalmartItemNumber() == "50036739"
        assert substitutedItemInfoList.get(1).getUnitPrice() == 1.00
        assert substitutedItemInfoList.get(1).getDepartment() == "49"
        assert substitutedItemInfoList.get(1).getQuantity() == 1.000
        assert substitutedItemInfoList.get(1).getUpcs().get(0).getUpc() == "5057172289390"
        assert substitutedItemInfoList.get(1).getUpcs().get(0).getUom() == "EACH"
    }


}
