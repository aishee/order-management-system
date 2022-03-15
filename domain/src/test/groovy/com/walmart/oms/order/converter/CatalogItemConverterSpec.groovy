package com.walmart.oms.order.converter

import com.walmart.oms.order.valueobject.CatalogItem
import spock.lang.Specification

class CatalogItemConverterSpec extends Specification {
    CatalogItemConverter catalogItemConverter = new CatalogItemConverter()

    def "ConvertToDatabaseColumn"() {

        given:

        CatalogItem catalogItem = CatalogItem.builder()
                .cin("464646")
                .skuId("2727272")
                .onSale(false)
                .price("£2.50")
                .isSellByDateRequired(false)
                .untraitedStores(Arrays.asList("5731"))
                .salesUnit("EACH")
                .upcNumbers(Arrays.asList("8388338", "626262"))
                .pickerDesc("test desc")
                .largeImageURL("http://localhost/largeImage")
                .smallImageURL("http://localhost/smallImage")
                .pricePerUom("£2.50")
                .build()

        when:
        String catalogJson = catalogItemConverter.convertToDatabaseColumn(catalogItem)

        CatalogItem catalogItem1 = catalogItemConverter.convertToEntityAttribute(catalogJson)
        then:
        assert catalogItem.cin == catalogItem1.cin
        assert catalogItem.skuId == catalogItem1.skuId
        assert catalogItem.onSale == catalogItem1.onSale
        assert catalogItem.price == catalogItem1.price
        assert catalogItem.isSellByDateRequired == catalogItem1.isSellByDateRequired
        assert catalogItem.untraitedStores == catalogItem1.untraitedStores
        assert catalogItem.salesUnit == catalogItem1.salesUnit
        assert catalogItem.upcNumbers == catalogItem1.upcNumbers
        assert catalogItem.pickerDesc == catalogItem1.pickerDesc
        assert catalogItem.largeImageURL == catalogItem1.largeImageURL
        assert catalogItem.smallImageURL == catalogItem1.smallImageURL
        assert catalogItem.pricePerUom == catalogItem1.pricePerUom


    }

    def "convertToEntityAttribute Exceptionally"() {
        when:
        CatalogItem catalogItem = catalogItemConverter.convertToEntityAttribute("")

        then:
        assert catalogItem == null
    }
}
