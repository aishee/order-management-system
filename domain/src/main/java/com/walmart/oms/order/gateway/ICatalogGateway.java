package com.walmart.oms.order.gateway;

import com.walmart.oms.order.valueobject.CatalogItem;
import com.walmart.oms.order.valueobject.CatalogItemInfoQuery;
import java.util.Map;

public interface ICatalogGateway {
  Map<String, CatalogItem> fetchCatalogData(CatalogItemInfoQuery catalogItemInfoQuery);
}
