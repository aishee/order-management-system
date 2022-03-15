package com.walmart.marketplace.filters


import com.walmart.marketplace.infrastructure.gateway.uber.UberServiceConfiguration
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UberEatsHmacAccessFilterTest extends Specification {

    UberHmacAccessFilter uberHmacAccessFilter
    UberServiceConfiguration uberServiceConfiguration = Mock()

    def setup() {
        uberHmacAccessFilter = new UberHmacAccessFilter(uberServiceConfiguration: uberServiceConfiguration)
    }

    def "Header missing in Request"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()

        when:
        uberHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED)

    }

    def "Request Body missing in Request"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        HttpServletResponse httpServletResponse = Mock()
        httpServletRequest.getHeader("X-Uber-Signature") >> "ABC"
        FilterChain filterChain = Mock()

        when:
        uberHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED)

    }


    def "Secret Key mismatch with Body"() {
        given:
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()
        httpServletRequest.addHeader("X-Uber-Signature", "ABC")
        httpServletRequest.setContent("Request Body".getBytes())
        uberServiceConfiguration.getClientSecret() >> "1234"

        when:
        uberHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED)

    }

    def "Secret Key mismatch with Query String"() {
        given:
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()
        httpServletRequest.addHeader("X-Uber-Signature", "ABC")
        httpServletRequest.setQueryString("http://abc.com")
        uberServiceConfiguration.getClientSecret() >> "1234"

        when:
        uberHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED)

    }

    def "Secret Key mismatch with Request URI"() {
        given:
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()
        httpServletRequest.addHeader("X-Uber-Signature", "ABC")
        httpServletRequest.setRequestURI("http://abc.com")
        uberServiceConfiguration.getClientSecret() >> "1234"

        when:
        uberHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED)

    }

    def "Secret Key Successfully Matching"() {
        given:
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()
        httpServletRequest.addHeader("X-Uber-Signature", "434d7b7f644f3fc79855906b873bba6a5cd4f15cb0e72fd7705b32431d1c89bf")
        httpServletRequest.setContent("Request Body".getBytes())
        uberServiceConfiguration.getClientSecret() >> "1234"

        when:
        uberHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * filterChain.doFilter(_ as HttpServletRequest, _ as HttpServletResponse)

    }

    def "Filter Trigger for Invalid Path"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        httpServletRequest.getServletPath() >> "invalid_path_to_filter"

        when:
        boolean shouldNotFilter = uberHmacAccessFilter.shouldNotFilter(httpServletRequest)

        then:
        shouldNotFilter
    }

    def "Filter Trigger for Valid Path"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        httpServletRequest.getServletPath() >> "/marketplace/uber/"

        when:
        boolean shouldNotFilter = uberHmacAccessFilter.shouldNotFilter(httpServletRequest)

        then:
        !shouldNotFilter
    }
}
