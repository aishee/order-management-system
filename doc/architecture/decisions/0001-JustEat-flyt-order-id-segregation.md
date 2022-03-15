# 1. JustEat and Flyt order id segregation and flow changes
Date: 2021-12-01

## Status
Accepted

## Context
The ADR is intended the capture the changes required to maintain 2 order ids for JustEat integration

### Problem Statement
- For UberEats, we have 1 vendor order id that is used to uniquely identify the order on UberEats end, we store this information in our database, use this for Order cancellation/Accept/Deny/Patch cart APIs and also send it to GIF. 
- For JustEat, Technical capabilities are managed by Flyt application. In this case, we will be getting 2 order ids for each order. 1 to uniquely identify the order in JustEat and another to identify it in Flyt system.  
  Flyt Order ID has to be used for Accepting or Rejecting the order whereas JustEat order id needs to be passed to OMS and FMS domains (and to GIF eventually).   

## Decision
- Since Flyt Order Id is used only for API integrations, we can keep this id in a new database column in Marketplace domain only and this can be used only while interacting with JustEat. 
- JustEat Order Id will be treated as the existing VendorOrderId column. This will be sent to GIF eventually in the Order Download request. Last 5 digits of Vendor order id will be used by Picker to identify an order while handing it over to JustEat driver.
- JustEat Order Id will also be sent to DWH for any reporting related use cases in the future.
<details>
  <summary>Sample JustEat Order Payload</summary>

```json
{
  "type": "delivery-by-delivery-partner",
  "posLocationId": "AKZ12",
  "id": "38bbeb45-f520-4438-a44f-0fcdbb29e166",
  "location": {
    "id": 1296,
    "timezone": "Europe/London"
  },
  "driver": {
    "first_name": "John",
    "last_name": "Smith",
    "phone_number": "555-111-3344"
  },
  "items": [
    {
      "name": "Cheeseburger",
      "description": "",
      "plu": "M2",
      "children": [],
      "price": 1700,
      "notes": ""
    }
  ],
  "created_at": "1606780145",
  "channel": {
    "id": 32,
    "name": "Just Eat"
  },
  "collect_at": "1606780980",
  "kitchen_notes": "",
  "third_party_order_reference": "22721763",
  "total": 1800,
  "payment_method": "CARD",
  "menu_reference": "",
  "payment": {
    "items_in_cart": {
      "inc_tax": 2160,
      "tax": 360
    },
    "adjustments": [
      {
        "name": "bagFee",
        "price": {
          "inc_tax": 199,
          "tax": 0
        }
      }
    ],
    "final": {
      "inc_tax": 2359,
      "tax": 360
    }
  },
  "delivery": {
    "first_name": "****************",
    "last_name": "****************",
    "line_one": "**********************",
    "line_two": "",
    "city": "*****",
    "postcode": "*****",
    "email": "customer@email.hidden",
    "coordinates": {
      "longitude": -97.13560152293131,
      "latitude": 49.898498728223224,
      "longitude_as_string": "-122.2966",
      "latitude_as_string": "49.8984"
    },
    "phone_masking_code": ""
  },
  "extras": {}
}
```
</details>

#### Implementation
Added a new column in MARKET_PLACE_ORDER as VENDOR_NATIVE_ORDER_ID to keep FlytId and Storing JustEat Order id in existing VENDOR_ORDER_ID column. 
In the JustEatsOrderGateway, passing VendorNativeOrderId field for API integrations. 
```
justEatsOrderStatusUpdateClient.acceptOrder(marketPlaceOrder.getVendorNativeOrderId())
```

#### Alternative
Instead of adding another column in database, we could have stored both Flyt and JustEat order ids in the existing Vendor Order ID column with some delimiter. 
Although this could have worked, it required changes in multiple places to separate them out while sending to OMS/FMS/GIF and in Automation. 

## Consequences
- We are able to send JustEat id to other domains and use Flyt id for API integrations.