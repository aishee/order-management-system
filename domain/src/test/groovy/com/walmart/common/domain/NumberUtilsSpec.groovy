package com.walmart.common.domain

import com.walmart.common.utils.NumberUtils
import spock.lang.Specification

import java.lang.reflect.Constructor

class NumberUtilsSpec extends Specification {


    def "Round off BigDecimal for long"() {
        given:
        long value = 126172l
        when:
        BigDecimal res = NumberUtils.getRoundedBigDecimal(BigDecimal.valueOf(value));
        then:
        res.scale() == 2
    }

    def "Round off BigDecimal to double "() {
        given:
        double value = 126172.222299918
        when:
        double res = NumberUtils.getRoundedDouble(BigDecimal.valueOf(value));
        then:
        res.toString().equals("126172.22") == true
    }

    def "Round off BigDecimal to double to rounded off to next greater decimal value"() {
        given:
        double value = 126172.229299918
        when:
        double res = NumberUtils.getRoundedDouble(BigDecimal.valueOf(value));
        then:
        res.toString().equals("126172.23") == true
    }

    def "Round off BigDecimal for null"() {
        given:
        long value = 126172l
        when:
        BigDecimal res = NumberUtils.getRoundedBigDecimal(null);
        then:
        res == BigDecimal.ZERO
    }

    def "Round off BigDecimal for product with long"() {
        given:
        long value = 12l
        BigDecimal multiplierValue = BigDecimal.valueOf(value)
        when:
        BigDecimal otherVal = NumberUtils.getRoundedBigDecimal(new BigDecimal("123.998112"));
        BigDecimal res = otherVal.multiply(multiplierValue)
        then:
        res.scale() == 2
    }

    def "test instance of util"() {
        given:
        Constructor<?>[] constructor = NumberUtils.class.getDeclaredConstructors();
        Constructor<?> cons = constructor[0];
        cons.setAccessible(true);

        when:
        NumberUtils numberUtils = cons.newInstance() as NumberUtils;

        then:
        numberUtils instanceof NumberUtils
    }
}
