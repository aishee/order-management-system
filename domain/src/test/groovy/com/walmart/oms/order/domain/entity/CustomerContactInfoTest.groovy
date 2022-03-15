package com.walmart.oms.order.domain.entity

import com.walmart.oms.order.aggregateroot.OmsOrder
import com.walmart.oms.order.valueobject.EmailAddress
import com.walmart.oms.order.valueobject.FullName
import com.walmart.oms.order.valueobject.MobilePhone
import com.walmart.oms.order.valueobject.TelePhone
import spock.lang.Specification

class CustomerContactInfoTest extends Specification {

    def "test default constructor"() {
        when:
        CustomerContactInfo customerContactInfo = new CustomerContactInfo()

        then:
        assert customerContactInfo != null

    }

    def "test oms order"() {
        given:
        OmsOrder omsOrder = Mock()
        FullName fullName = Mock()
        TelePhone phoneNumberOne = Mock()
        TelePhone phoneNumberTwo = Mock()
        EmailAddress email = Mock()
        MobilePhone mobileNumber = Mock()
        CustomerContactInfo customerContactInfo1 = CustomerContactInfo.builder().order(omsOrder)
                .fullName(fullName).phoneNumberOne(phoneNumberOne).phoneNumberTwo(phoneNumberTwo)
                .email(email).mobileNumber(mobileNumber)
                .build()

        CustomerContactInfo customerContactInfo2 = CustomerContactInfo.builder().order(omsOrder)
                .fullName(fullName).phoneNumberOne(phoneNumberOne).phoneNumberTwo(phoneNumberTwo)
                .email(email).mobileNumber(mobileNumber)
                .build()

        customerContactInfo1.getFullName().getMiddleName() >> "Middle Name"

        when:
        boolean status = customerContactInfo1.equals(customerContactInfo2)

        then:
        assert status
        assert customerContactInfo1.hashCode() != 0
        assert customerContactInfo1.toString() != null
        assert customerContactInfo1.getMiddleName() != null
        assert customerContactInfo1.getOrder() != null


    }
}
