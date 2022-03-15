package com.walmart.oms


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.junit.jupiter.api.Assertions.assertTrue

@SpringBootTest(classes = OrderManagementSystemApplication)
@ActiveProfiles("test")
class OrderManagementSystemApplicationTest extends Specification {

    @Autowired
    ApplicationContext context;

    def "test context and components loads"() {
        expect:
        context != null
        context.containsBean("orderController")
        context.containsBean("defaultConnectionFactory")
        context.containsBean("configureRedeliveryPolicy")
        context.containsBean("jacksonJmsMessageConverter")
        context.containsBean("MaasMqProducerComponent")
    }

    def "Main test method call"() {
        String[] args = []
        OrderManagementSystemApplication.main(args);
        expect:
        assertTrue(true);
    }

}
