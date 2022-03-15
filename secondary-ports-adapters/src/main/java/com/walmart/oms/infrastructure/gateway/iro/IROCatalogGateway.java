package com.walmart.oms.infrastructure.gateway.iro;

import static java.util.Objects.isNull;

import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROItem;
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROPrice;
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROPromotionInfo;
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROResponse;
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IRORootItems;
import com.walmart.oms.order.gateway.ICatalogGateway;
import com.walmart.oms.order.valueobject.CatalogItem;
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IROCatalogGateway implements ICatalogGateway {

  @Autowired private IROHttpWebClient iroHttpClient;

  @ManagedConfiguration private IROServiceConfiguration iroServiceConfiguration;

  private static final String LARGE_IMAGE = "?$280_IDShot_3$";

  private static final String SMALL_IMAGE = "?$130_IDShot_4$";

  @Override
  public Map<String, CatalogItem> fetchCatalogData(CatalogItemInfoQuery catalogItemInfoQuery) {

    // Check whether query object contains all required fields.
    if (catalogItemInfoQuery.isValidRequest()) {

      List<IROResponse> iroResponseList = iroHttpClient.retrieveCatalogData(catalogItemInfoQuery);
      return iroResponseList.stream()
          .flatMap(iroResponse -> iroResponse.getItems().stream())
          .collect(Collectors.toMap(IRORootItems::getItemId, this::mapToCatalogData));

    } else {
      String errorMessage =
          String.format("Invalid Catalog item fetch request :%s", catalogItemInfoQuery);
      log.error(errorMessage);
      throw new OMSBadRequestException(errorMessage);
    }
  }

  private CatalogItem mapToCatalogData(IRORootItems iroRootItems) {

    IROItem iroItem = iroRootItems.getIroItem();
    IROPrice iroPrice = iroRootItems.getIroPrice();

    return CatalogItem.builder()
        .skuId(iroItem.getSkuId())
        .cin(iroItem.getCin())
        .upcNumbers(iroItem.getUpcNumbers())
        .isBundle(iroRootItems.isBundle())
        .brand(iroItem.getBrand())
        .itemName(iroItem.getItemName())
        .name(iroItem.getName())
        .pickerDesc(iroItem.getPickerDesc())
        .salesUnit(iroItem.getSalesUnit())
        .largeImageURL(
            iroItem.getImages().getScene7Host() + iroItem.getImages().getScene7Id() + LARGE_IMAGE)
        .smallImageURL(
            iroItem.getImages().getScene7Host() + iroItem.getImages().getScene7Id() + SMALL_IMAGE)
        .untraitedStores(iroItem.getUntraitedStores())
        .minIdealDayValue(iroItem.getFreshnessInfo().getMinIdealDayValue())
        .maxIdealDayValue(iroItem.getFreshnessInfo().getMaxIdealDayValue())
        .isSellByDateRequired(iroItem.getFreshnessInfo().getIsSellByDateRequired())
        .weight(iroItem.getExtendedItemInfo().getWeight())
        .replenishUnitIndicator(iroItem.getExtendedItemInfo().getReplenishUnitIndicator())
        .pricePerUom(
            !isNull(iroPrice) && !isNull(iroPrice.getPriceInfo())
                ? iroPrice.getPriceInfo().getPricePerUom()
                : "£0.0")
        .price(
            !isNull(iroPrice) && !isNull(iroPrice.getPriceInfo())
                ? iroPrice.getPriceInfo().getPrice()
                : "£0.0")
        .onSale(checkIfItemIsOnSale(iroRootItems, iroPrice))
        .salePrice(
            !isNull(iroPrice) && !isNull(iroPrice.getPriceInfo())
                ? iroPrice.getPriceInfo().getSalePrice()
                : "£0.0")
        .build();
  }

  private boolean checkIfItemIsOnSale(IRORootItems iroRootItems, IROPrice iroPrice) {

    if (iroServiceConfiguration.isNewPriceDropTagEnabled()) {
      return iroRootItems.getIroPromotionInfo().stream()
          .findAny()
          .map(IROPromotionInfo::getIroBasePromotion)
          .filter(
              iroBasePromotion ->
                  iroServiceConfiguration
                      .getPriceDrop()
                      .equalsIgnoreCase(iroBasePromotion.getItemPromoType()))
          .isPresent();
    } else {
      return !isNull(iroPrice) && iroPrice.isOnSale();
    }
  }
}
