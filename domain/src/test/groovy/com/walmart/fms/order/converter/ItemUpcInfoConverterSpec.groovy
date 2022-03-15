package com.walmart.fms.order.converter


import com.walmart.fms.order.valueobject.ItemUpcInfo
import spock.lang.Specification

class ItemUpcInfoConverterSpec extends Specification {

    ItemUpcInfoConverter itemUpcInfoConverter = new ItemUpcInfoConverter()
    
    def "Valid ItemUpcInfo EntityAttribute to DBColumn then back to EntityAttribute"() {

        given:
        List<String> upcNumbers  = ["45444", "333444"]
        ItemUpcInfo itemUpcInfo = mockItemUpcInfo(upcNumbers)

        when:
        String catalogJson = itemUpcInfoConverter.convertToDatabaseColumn(itemUpcInfo)

        ItemUpcInfo itemUpcInfo1 = itemUpcInfoConverter.convertToEntityAttribute(catalogJson)
        then:
        assert catalogJson.contains(itemUpcInfo1.upcNumbers[0])
        assert itemUpcInfo.upcNumbers == itemUpcInfo1.upcNumbers

    }

    def "Null ItemUpcInfo EntityAttribute to DBColumn then back to EntityAttribute"() {

        given:
        ItemUpcInfo itemUpcInfo = null

        when:
        String catalogJson = itemUpcInfoConverter.convertToDatabaseColumn(itemUpcInfo)
        ItemUpcInfo itemUpcInfo1 = itemUpcInfoConverter.convertToEntityAttribute(catalogJson)

        then:
        assert catalogJson == null
        assert itemUpcInfo1 == null

    }


    private ItemUpcInfo mockItemUpcInfo(List<String> upcNumbers) {
        ItemUpcInfo.builder()
                .upcNumbers(upcNumbers)
                .build()
    }

    def "ConvertToEntityAttribute"() {
    }
}
