package com.walmart.fms.commands.converters


import com.walmart.common.domain.messaging.DomainEvent
import com.walmart.fms.commands.CreateFmsOrderCommand
import com.walmart.oms.order.valueobject.events.FmsOrderValueObject
import spock.lang.Specification

class FMSValueObjectToCreateCmdConverterSpec extends Specification {

    def "Mapping from Value Object to Command Object"() {
        given:
        FmsOrderValueObject fmsOrderValueObject = DomainEvent.getInstance()
                .readValue(new File("src/test/resources/input/FmsOrderValueObject.json"), FmsOrderValueObject.class);

        when:
        CreateFmsOrderCommand createFmsOrderCommand = FMSValueObjectToCreateCmdConverter.INSTANCE.convertVoToCommand(fmsOrderValueObject)

        then:
        CreateFmsOrderCommand expectedCreateFmsOrderCommand = DomainEvent.getInstance().readValue(new File("src/test/resources/output/CreateFmsOrderCommand.json"), CreateFmsOrderCommand.class)
        expectedCreateFmsOrderCommand == createFmsOrderCommand

    }

}
