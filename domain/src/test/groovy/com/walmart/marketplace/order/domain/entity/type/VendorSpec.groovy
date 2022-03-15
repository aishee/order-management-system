package com.walmart.marketplace.order.domain.entity.type

import spock.lang.Specification

class VendorSpec extends Specification {

    def "Test Number of Vendors in Enum"() {
        expect:
        Vendor.values().size() == 3
        Vendor.UBEREATS.getVendorType() == Vendor.VendorType.UBEREATS
        Vendor.TESTVENDOR.getVendorType() == Vendor.VendorType.TESTVENDOR
        Vendor.JUSTEAT.getVendorType() == Vendor.VendorType.JUSTEAT
        Vendor.UBEREATS.getVendorName() == Vendor.VendorType.UBEREATS.toString()
        Vendor.UBEREATS.getVendorId() == Vendor.UBEREATS.toString()
        Vendor.JUSTEAT.getVendorId() == Vendor.JUSTEAT.toString()
        Vendor.TESTVENDOR.nextOSN() == null
        Vendor.UBEREATS.nextOSN() != null
        Vendor.JUSTEAT.nextOSN() != null
        Vendor.UBEREATS.getCode() == "U"
        Vendor.JUSTEAT.getCode() == "J"
        Vendor.TESTVENDOR.getCode() == "T"
    }

}
