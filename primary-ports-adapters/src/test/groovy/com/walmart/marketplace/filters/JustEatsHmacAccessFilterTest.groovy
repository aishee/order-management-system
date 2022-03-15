package com.walmart.marketplace.filters

import com.walmart.marketplace.infrastructure.gateway.justeats.config.JustEatsServiceConfiguration
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JustEatsHmacAccessFilterTest extends Specification {

    JustEatsHmacAccessFilter justEatsHmacAccessFilter
    JustEatsServiceConfiguration justEatsServiceConfiguration = Mock()

    def setup() {
        justEatsHmacAccessFilter = new JustEatsHmacAccessFilter(justEatsServiceConfiguration: justEatsServiceConfiguration)
    }

    def "Header missing in Request"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()

        when:
        justEatsHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header Authorization is missing")

    }

    def "Secret Key Missing"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        HttpServletResponse httpServletResponse = Mock()
        httpServletRequest.getHeader("Authorization") >> "ABC"
        FilterChain filterChain = Mock()

        when:
        justEatsHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Secret Key is missing for JustEats Web hook authorization processing.")

    }

    def "Secret Key mismatch with Header"() {
        given:
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()
        httpServletRequest.addHeader("Authorization", "ABC")
        justEatsServiceConfiguration.getClientSecret() >> "1234"

        when:
        justEatsHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization encode is not matching with JustEats request header key=Authorization header value=ABC")

    }

    def "Secret Key Successfully Matching"() {
        given:
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = Mock()
        FilterChain filterChain = Mock()
        httpServletRequest.addHeader("Authorization", "434d7b7f644f3fc79855906b873bba6a5cd4f15cb0e72fd7705b32431d1c89bf")
        justEatsServiceConfiguration.getClientSecret() >> "434d7b7f644f3fc79855906b873bba6a5cd4f15cb0e72fd7705b32431d1c89bf"

        when:
        justEatsHmacAccessFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain)

        then:
        1 * filterChain.doFilter(_ as HttpServletRequest, _ as HttpServletResponse)

    }

    def "Filter Trigger for Invalid Path"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        httpServletRequest.getServletPath() >> "invalid_path_to_filter"

        when:
        boolean shouldNotFilter = justEatsHmacAccessFilter.shouldNotFilter(httpServletRequest)

        then:
        shouldNotFilter
    }

    def "Filter Trigger for Valid Path"() {
        given:
        HttpServletRequest httpServletRequest = Mock()
        httpServletRequest.getServletPath() >> "/marketplace/justeats/"

        when:
        boolean shouldNotFilter = justEatsHmacAccessFilter.shouldNotFilter(httpServletRequest)

        then:
        !shouldNotFilter
    }
}
