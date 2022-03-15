package com.walmart.marketplace.order.domain.uber

import com.walmart.marketplace.order.domain.entity.type.Vendor
import spock.lang.Specification

class PatchCartSpec extends Specification{
    PatchCartInfo patchCartInfo
    String vendorOrderId = UUID.randomUUID().toString()

    def setup() {
        patchCartInfo = new PatchCartInfo()
    }

    def "Verify containsNilOrPartialPicks" () {
        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>();
        partialPickInstanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        List<String> nilPickInstanceIds = new ArrayList<>()
        nilPickInstanceIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        nilPickInstanceIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        patchCartInfo = getPatchCartInfo(nilPickInstanceIds, partialPickInstanceIds)

        when:
        boolean result = patchCartInfo.containsNilOrPartialPicks()

        then:
        result == true
    }

    def "Verify containsNilOrPartialPicks when no nil Picks" () {
        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>();
        partialPickInstanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        List<String> nilPickInstanceIds = new ArrayList<>()
        patchCartInfo = getPatchCartInfo(nilPickInstanceIds, partialPickInstanceIds)

        when:
        boolean result = patchCartInfo.containsNilOrPartialPicks()

        then:
        result == true
    }

    def "Verify containsNilOrPartialPicks when no partial Picks" () {
        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>();
        List<String> nilPickInstanceIds = new ArrayList<>()
        nilPickInstanceIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        nilPickInstanceIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        patchCartInfo = getPatchCartInfo(nilPickInstanceIds, partialPickInstanceIds)

        when:
        boolean result = patchCartInfo.containsNilOrPartialPicks()

        then:
        result == true
    }

    def "Verify isValidVendor UBEREATS" () {
        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>();
        partialPickInstanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        List<String> nilPickInstanceIds = new ArrayList<>()
        nilPickInstanceIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        nilPickInstanceIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        patchCartInfo = getPatchCartInfo(nilPickInstanceIds, partialPickInstanceIds)

        when:
        boolean result = patchCartInfo.isValidVendor()

        then:
        result == true
    }

    def "Verify isValidVendor TESTVENDOR" () {
        given:
        Map<String, Integer> partialPickInstanceIds = new HashMap<>();
        partialPickInstanceIds.put("e2066983-7793-4017-ac06-74785bfeff15", 1)
        List<String> nilPickInstanceIds = new ArrayList<>()
        nilPickInstanceIds.add("e2066983-7793-4017-ac06-74785bfeff15")
        nilPickInstanceIds.add("273e9df8-838e-476b-b899-b23795b55b6e")
        patchCartInfo = getPatchCartInfo(nilPickInstanceIds, partialPickInstanceIds)
        patchCartInfo.vendorId = Vendor.TESTVENDOR

        when:
        boolean result = patchCartInfo.isValidVendor()

        then:
        result == false
    }

    PatchCartInfo getPatchCartInfo(List<String> nilPicks, Map<String, Integer> partialPicks) {
        return PatchCartInfo.builder()
                .vendorOrderId(vendorOrderId)
                .vendorId(Vendor.UBEREATS)
                .nilPickInstanceIds(nilPicks)
                .partialPickInstanceIds(partialPicks)
                .build()
    }
}
