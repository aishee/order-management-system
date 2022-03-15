package com.walmart.oms.integrationtest

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.walmart.oms.OrderManagementSystemApplication
import com.walmart.oms.infrastructure.gateway.iro.dto.request.IRORequest
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IROResponse
import com.walmart.oms.infrastructure.gateway.iro.dto.response.IRORootItems
import groovy.text.GStringTemplateEngine
import lombok.extern.slf4j.Slf4j
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import static com.github.tomakehurst.wiremock.client.WireMock.ok
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.serverError
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

@Slf4j
@SpringBootTest(classes = OrderManagementSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-tests")
class BaseITest extends Specification {

    @Autowired
    ObjectMapper jsonObjectMapper

    GStringTemplateEngine stringTemplateEngine = new GStringTemplateEngine()

    String WM_VERTICAL_ID = "WM_VERTICAL_ID"
    String WM_TENANT_ID = "WM_TENANT_ID"

    static {

        System.setProperty("loggerLocation", "./log")
        System.setProperty("loggerName", "console-async")

    }

    HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.add(WM_VERTICAL_ID, "ASDAGR")
        httpHeaders.add(WM_TENANT_ID, "MARKETPLACE")
        return httpHeaders
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())


    void setupCatalogWireMockStubsForSku(List<String> cins, Instant shipOnDate = null, String storeId = "store123") {

        //stubbing IRO request & response
        String expectedRequestBody = formCatalogRequest(cins, storeId, shipOnDate)
        String expectedResponseBody = formCatalogResponse(cins)
        wireMockRule.stubFor(
                post(urlPathMatching("/catalogitem"))
                        .withRequestBody(equalToJson(expectedRequestBody))
                        .willReturn(
                                ok()
                                        .withHeader("content-type", "application/json")
                                        .withBody(expectedResponseBody)
                        )
        )
    }

    void setupCatalogWireMockStubsFor400() {

        //stubbing IRO request & response
        wireMockRule.stubFor(
                post(urlPathMatching("/catalogitem"))
                        .willReturn(
                                badRequest()
                                        .withHeader("content-type", "application/json")
                                        .withBody("""{"error_code":"INTERNAL_ERROR"}""")
                        )
        )
    }

    void setupCatalogWireMockStubsFor500() {

        //stubbing IRO request & response
        wireMockRule.stubFor(
                post(urlPathMatching("/catalogitem"))
                        .willReturn(
                                serverError()
                                        .withHeader("content-type", "application/json")
                                        .withBody("""{"error_code":"INTERNAL_ERROR"}""")
                        )
        )
    }

    String formCatalogRequest(List<String> cins, String storeId = "store123", Instant shipOnDate) {
        IRORequest iroRequest = IRORequest.builder()
                .itemIds(cins)
                .storeId(storeId)
                .requestOrigin("ods")
                .consumerContract("ods_item")
                .shipOnDate(shipOnDate != null ? shipOnDate.atZone(ZoneId.of("Europe/London")).toLocalDate().toString() : null)
                .build()
        return jsonObjectMapper.writeValueAsString(iroRequest)
    }

    String formCatalogResponse(List<String> cins) {

        List<IRORootItems> iroRootItems = []
        IROResponse mockedIroResponse = new IROResponse(
                data: new IROResponse.Data(
                        uberItem: new IROResponse.UberItem(
                                items: iroRootItems
                        )
                )
        )
        cins.eachWithIndex { String entry, int i ->
            String cin = cins.get(i)
            String salesUnit = "EACH"
            String productStatus = "AVAILABLE"
            iroRootItems << formIroRootItem(cin, salesUnit, productStatus)
        }
        return jsonObjectMapper.writeValueAsString(mockedIroResponse)
    }

    IRORootItems formIroRootItem(String cin, String salesUnit, String productStatus) {
        String iroItemTemplate = getClass().getResource('/IRO/IROResponseItemTemplate.json').text
        Map bindings = [cin: cin, salesUnit: salesUnit, productStatus: productStatus]
        String iroItem = stringTemplateEngine.createTemplate(iroItemTemplate).make(bindings).toString()
        return jsonObjectMapper.readValue(iroItem, IRORootItems)
    }
}
