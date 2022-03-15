package com.walmart.oms.infrastructure.gateway.iro

import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import com.walmart.oms.infrastructure.gateway.iro.dto.response.*
import com.walmart.oms.order.valueobject.CatalogItem
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery
import spock.lang.Specification

class IROCatalogGatewayTest extends Specification {

    IROCatalogGateway iroCatalogGateway = Mock()
    IROHttpWebClient iroHttpClient = Mock()
    IROServiceConfiguration iroServiceConfiguration = Spy()

    def setup() {
        iroCatalogGateway = new IROCatalogGateway(
                iroHttpClient: iroHttpClient,
                iroServiceConfiguration: iroServiceConfiguration
        )
    }

    def "When Empty ItemId List is passed to IROCatalogGateway"() {

        when:
        iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().shipOnDate(new Date()).itemType("CIN").storeId("1234").build())

        then:
        thrown(OMSBadRequestException)
    }

    def "When Null ItemId List is passed to IROCatalogGateway"() {

        when:
        iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(null).shipOnDate(new Date()).itemType("CIN").storeId("1234").build())

        then:
        thrown(OMSBadRequestException)
    }

    def "When Invalid ItemType is passed to IROCatalogGateway"() {

        when:
        iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["987654321"]).shipOnDate(new Date()).itemType("ABC").storeId("1234").build())

        then:
        thrown(OMSBadRequestException)
    }

    def "When Empty ItemType is passed to IROCatalogGateway"() {

        when:
        iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["987654321"]).shipOnDate(new Date()).itemType("").storeId("1234").build())

        then:
        thrown(OMSBadRequestException)
    }

    def "When Null ItemType is passed to IROCatalogGateway"() {

        when:
        iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["987654321"]).itemType(null).shipOnDate(new Date()).storeId("1234").build())

        then:
        thrown(OMSBadRequestException)
    }

    def "Exception while calling IRO Web Client"() {

        given:
        iroHttpClient.retrieveCatalogData(_ as CatalogItemInfoQuery) >> { throw new OMSThirdPartyException("") }

        when:
        iroHttpClient.retrieveCatalogData(CatalogItemInfoQuery.builder().itemIds(["419072"]).itemType("CIN").storeId("1234").build())

        then:
        thrown(OMSThirdPartyException)
    }

    def "Testing IROResponse to Domain field Mapping"() {
        given:
        List<IROResponse> iroResponseList = new ArrayList<>()
        IROResponse iroResponse = createSampleIroResponse()
        iroResponseList.add(iroResponse)
        iroHttpClient.retrieveCatalogData(_ as CatalogItemInfoQuery) >> { return iroResponseList }


        when:
        Map<String, CatalogItem> catalogDataMap = iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["419072"]).itemType("CIN").storeId("1234").build())

        then:
        catalogDataMap.keySet().size() == 1

        catalogDataMap.get("419072") != null
        CatalogItem catalogItem = catalogDataMap.get("419072")
        IRORootItems iroRootItems = iroResponse.data.uberItem.items.get(0)
        IROItem iroItem = iroResponse.data.uberItem.items.get(0).iroItem
        IROPrice iroPrice = iroResponse.data.uberItem.items.get(0).iroPrice

        catalogItem.skuId == iroItem.skuId
        catalogItem.cin == iroItem.cin
        catalogItem.upcNumbers == iroItem.upcNumbers
        catalogItem.isBundle == iroRootItems.bundle
        catalogItem.brand == iroItem.brand
        catalogItem.itemName == iroItem.itemName
        catalogItem.name == iroItem.name
        catalogItem.pickerDesc == iroItem.pickerDesc
        catalogItem.salesUnit == iroItem.salesUnit
        catalogItem.largeImageURL == iroItem.images.scene7Host + iroItem.images.scene7Id + "?\$280_IDShot_3\$"
        catalogItem.smallImageURL == iroItem.images.scene7Host + iroItem.images.scene7Id + "?\$130_IDShot_4\$"
        catalogItem.untraitedStores == iroItem.untraitedStores
        catalogItem.pricePerUom == iroPrice.priceInfo.pricePerUom
        catalogItem.minIdealDayValue == iroItem.freshnessInfo.minIdealDayValue
        catalogItem.maxIdealDayValue == iroItem.freshnessInfo.maxIdealDayValue
        catalogItem.isSellByDateRequired == iroItem.freshnessInfo.isSellByDateRequired
        catalogItem.weight == iroItem.extendedItemInfo.weight
        catalogItem.replenishUnitIndicator == iroItem.extendedItemInfo.replenishUnitIndicator
    }

    def "Testing IROResponse to Domain field Mapping for an onsale item"() {
        given:
        List<IROResponse> iroResponseList = new ArrayList<>()
        IROResponse iroResponse = createSampleIroResponseWithSale()
        iroResponseList.add(iroResponse)
        iroHttpClient.retrieveCatalogData(_ as CatalogItemInfoQuery) >> { return iroResponseList }
        iroServiceConfiguration.isNewPriceDropTagEnabled() >> true
        iroServiceConfiguration.getPriceDrop() >> "Price Drop"


        when:
        Map<String, CatalogItem> catalogDataMap = iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["419072"]).shipOnDate(new Date()).itemType("CIN").storeId("1234").build())

        then:
        catalogDataMap.keySet().size() == 1

        catalogDataMap.get("419072") != null
        CatalogItem catalogItem = catalogDataMap.get("419072")
        IRORootItems iroRootItems = iroResponse.data.uberItem.items.get(0)
        IROItem iroItem = iroResponse.data.uberItem.items.get(0).iroItem
        IROPrice iroPrice = iroResponse.data.uberItem.items.get(0).iroPrice

        catalogItem.skuId == iroItem.skuId
        catalogItem.cin == iroItem.cin
        catalogItem.upcNumbers == iroItem.upcNumbers
        catalogItem.isBundle == iroRootItems.bundle
        catalogItem.brand == iroItem.brand
        catalogItem.itemName == iroItem.itemName
        catalogItem.name == iroItem.name
        catalogItem.pickerDesc == iroItem.pickerDesc
        catalogItem.salesUnit == iroItem.salesUnit
        catalogItem.largeImageURL == iroItem.images.scene7Host + iroItem.images.scene7Id + "?\$280_IDShot_3\$"
        catalogItem.smallImageURL == iroItem.images.scene7Host + iroItem.images.scene7Id + "?\$130_IDShot_4\$"
        catalogItem.untraitedStores == iroItem.untraitedStores
        catalogItem.pricePerUom == iroPrice.priceInfo.pricePerUom
        catalogItem.minIdealDayValue == iroItem.freshnessInfo.minIdealDayValue
        catalogItem.maxIdealDayValue == iroItem.freshnessInfo.maxIdealDayValue
        catalogItem.isSellByDateRequired == iroItem.freshnessInfo.isSellByDateRequired
        catalogItem.weight == iroItem.extendedItemInfo.weight
        catalogItem.replenishUnitIndicator == iroItem.extendedItemInfo.replenishUnitIndicator
        catalogItem.onSale
        catalogItem.salePrice == iroPrice.getPriceInfo().getSalePrice()
    }

    def "Testing IROResponse to Domain field Mapping for an new onsale item"() {
        given:
        List<IROResponse> iroResponseList = new ArrayList<>()
        IROResponse iroResponse = createSampleIroResponseWithSale()
        iroResponseList.add(iroResponse)
        iroHttpClient.retrieveCatalogData(_ as CatalogItemInfoQuery) >> { return iroResponseList }
        iroServiceConfiguration.isNewPriceDropTagEnabled() >> true
        iroServiceConfiguration.getPriceDrop() >> "Price Drop"


        when:
        Map<String, CatalogItem> catalogDataMap = iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["419072"]).shipOnDate(new Date()).itemType("CIN").storeId("1234").build())

        then:
        catalogDataMap.keySet().size() == 1

        catalogDataMap.get("419072") != null
        CatalogItem catalogItem = catalogDataMap.get("419072")
        IRORootItems iroRootItems = iroResponse.data.uberItem.items.get(0)
        IROItem iroItem = iroResponse.data.uberItem.items.get(0).iroItem
        IROPrice iroPrice = iroResponse.data.uberItem.items.get(0).iroPrice
        iroPrice.setOnSale(false);
        catalogItem.skuId == iroItem.skuId
        catalogItem.cin == iroItem.cin
        catalogItem.upcNumbers == iroItem.upcNumbers
        catalogItem.isBundle == iroRootItems.bundle
        catalogItem.brand == iroItem.brand
        catalogItem.itemName == iroItem.itemName
        catalogItem.name == iroItem.name
        catalogItem.pickerDesc == iroItem.pickerDesc
        catalogItem.salesUnit == iroItem.salesUnit
        catalogItem.largeImageURL == iroItem.images.scene7Host + iroItem.images.scene7Id + "?\$280_IDShot_3\$"
        catalogItem.smallImageURL == iroItem.images.scene7Host + iroItem.images.scene7Id + "?\$130_IDShot_4\$"
        catalogItem.untraitedStores == iroItem.untraitedStores
        catalogItem.pricePerUom == iroPrice.priceInfo.pricePerUom
        catalogItem.minIdealDayValue == iroItem.freshnessInfo.minIdealDayValue
        catalogItem.maxIdealDayValue == iroItem.freshnessInfo.maxIdealDayValue
        catalogItem.isSellByDateRequired == iroItem.freshnessInfo.isSellByDateRequired
        catalogItem.weight == iroItem.extendedItemInfo.weight
        catalogItem.replenishUnitIndicator == iroItem.extendedItemInfo.replenishUnitIndicator
        catalogItem.salePrice == iroPrice.getPriceInfo().getSalePrice()
    }

    def "Testing IROResponse to Domain field Mapping for an new onsale item with flag false"() {
        given:
        List<IROResponse> iroResponseList = new ArrayList<>()
        IROResponse iroResponse = createSampleIroResponseWithSale()
        iroResponseList.add(iroResponse)
        iroHttpClient.retrieveCatalogData(_ as CatalogItemInfoQuery) >> { return iroResponseList }
        iroServiceConfiguration.isNewPriceDropTagEnabled() >> false
        iroServiceConfiguration.getPriceDrop() >> "Price Drop"

        when:
        Map<String, CatalogItem> catalogDataMap = iroCatalogGateway.fetchCatalogData(CatalogItemInfoQuery.builder().itemIds(["419072"]).shipOnDate(new Date()).itemType("CIN").storeId("1234").build())

        then:
        catalogDataMap.keySet().size() == 1

        catalogDataMap.get("419072") != null
        CatalogItem catalogItem = catalogDataMap.get("419072")
        IRORootItems iroRootItems = iroResponse.data.uberItem.items.get(0)
        IROItem iroItem = iroResponse.data.uberItem.items.get(0).iroItem
        IROPrice iroPrice = iroResponse.data.uberItem.items.get(0).iroPrice
        catalogItem.onSale == iroPrice.isOnSale()
    }

    IROResponse createSampleIroResponse() {
        return new IROResponse(
                data: new IROResponse.Data(
                        uberItem: new IROResponse.UberItem(
                                items: [
                                        new IRORootItems(
                                                itemId: "419072",
                                                isBundle: false,
                                                iroItem: new IROItem(
                                                        skuId: "399016",
                                                        cin: "419072",
                                                        upcNumbers: ["5740900805408"],
                                                        brand: "Lurpak",
                                                        itemName: "Slightly Salted Spreadable",
                                                        name: "Lurpak Slightly Salted Spreadable",
                                                        pickerDesc: "Lurpak Slightly Salted Spreadable Butter 500G",
                                                        salesUnit: "Each",
                                                        images: new IROItem.Images(
                                                                scene7Id: "5740900805408",
                                                                scene7Host: "https://ui.assets-asda.com:443/dm/"
                                                        ),
                                                        untraitedStores: ["4293",
                                                                          "4173"],
                                                        freshnessInfo: new IROItem.FreshnessInfo(
                                                                minIdealDayValue: 0,
                                                                maxIdealDayValue: 0,
                                                                isSellByDateRequired: false

                                                        ),
                                                        extendedItemInfo: new IROItem.ExtendedItemInfo(
                                                                weight: "500g",
                                                                replenishUnitIndicator: "N"
                                                        )
                                                ),
                                                iroPrice: new IROPrice(
                                                        isOnSale: false,
                                                        priceInfo: new IROPrice.IROPriceInfoDTO(
                                                                pricePerUom: "£5.50/kg",
                                                                "price": "£2.50"
                                                        )
                                                ),
                                                iroPromotionInfo: [
                                                        new IROPromotionInfo(
                                                                rollback: new IROPromotionInfo.IROPromotionRollback(
                                                                        wasPrice: "£3.75"
                                                                )
                                                        )
                                                ]
                                        )
                                ]
                        )
                )
        )
    }

    IROResponse createSampleIroResponseWithSale() {
        return new IROResponse(
                data: new IROResponse.Data(
                        uberItem: new IROResponse.UberItem(
                                items: [
                                        new IRORootItems(
                                                itemId: "419072",
                                                isBundle: false,

                                                iroItem: new IROItem(
                                                        skuId: "399016",
                                                        cin: "419072",
                                                        upcNumbers: ["5740900805408"],
                                                        brand: "Lurpak",
                                                        itemName: "Slightly Salted Spreadable",
                                                        name: "Lurpak Slightly Salted Spreadable",
                                                        pickerDesc: "Lurpak Slightly Salted Spreadable Butter 500G",
                                                        salesUnit: "Each",
                                                        images: new IROItem.Images(
                                                                scene7Id: "5740900805408",
                                                                scene7Host: "https://ui.assets-asda.com:443/dm/"
                                                        ),
                                                        untraitedStores: ["4293",
                                                                          "4173"],
                                                        freshnessInfo: new IROItem.FreshnessInfo(
                                                                minIdealDayValue: 0,
                                                                maxIdealDayValue: 0,
                                                                isSellByDateRequired: false

                                                        ),
                                                        extendedItemInfo: new IROItem.ExtendedItemInfo(
                                                                weight: "500g",
                                                                replenishUnitIndicator: "N"
                                                        )
                                                ),
                                                iroPrice: new IROPrice(
                                                        isOnSale: true,
                                                        priceInfo: new IROPrice.IROPriceInfoDTO(
                                                                pricePerUom: "£5.50/kg",
                                                                "price": "£2.50",
                                                                "salePrice": "£1.0"
                                                        )
                                                ),
                                                iroPromotionInfo: [
                                                        new IROPromotionInfo(
                                                                rollback: new IROPromotionInfo.IROPromotionRollback(
                                                                        wasPrice: "£3.75"
                                                                ),
                                                                iroBasePromotion: new IROBasePromotion(
                                                                        itemPromoType: "Price Drop"
                                                                )
                                                        )
                                                ]
                                        )
                                ]
                        )
                )
        )
    }

}
