package com.walmart.util

import spock.lang.Specification

import javax.xml.datatype.XMLGregorianCalendar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeUtilSpec extends Specification {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    def "Invalid Local Date formatter"() {
        when:
        Optional<String> formatterLocalDate = DateTimeUtil.formatLocalDate(null, formatter)

        then:
        !formatterLocalDate.isPresent()
    }

    def "Valid Local Date formatter"() {
        when:
        Optional<String> formatterLocalDate = DateTimeUtil.formatLocalDate(LocalDate.of(2021, 11, 1), formatter)

        then:
        formatterLocalDate.isPresent()
        formatterLocalDate.get().equalsIgnoreCase("2021-11-01")
    }

    def "XmlGregorianCalendar test"() {
        when:
        XMLGregorianCalendar calendar = DateTimeUtil.getTime(new Date(2021, 11, 5))

        then:
        calendar.getYear() == 3921
    }

    def "XmlGregorianCalendar null check"() {
        when:
        XMLGregorianCalendar calendar = DateTimeUtil.getTime(null)

        then:
        calendar == null
    }

    def "test fromLocalDateToDate"() {
        given:
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        LocalDateTime localDateTime = LocalDateTime.parse("16-09-2021 11:13:00", formatter)

        when:
        Date dateResult = DateTimeUtil.fromLocalDateTime(localDateTime)

        then:
        Calendar cal = Calendar.getInstance()
        cal.setTime(dateResult)
        cal.get(Calendar.YEAR) == 2021
        cal.get(Calendar.MONTH) == 8
        cal.get(Calendar.DAY_OF_MONTH) == 16
    }

}
