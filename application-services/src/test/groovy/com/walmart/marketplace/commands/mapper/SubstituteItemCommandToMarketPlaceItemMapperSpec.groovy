package com.walmart.marketplace.commands.mapper

import com.walmart.marketplace.commands.MarketPlacePickCompleteCommand
import com.walmart.marketplace.mappers.SubstituteItemCommandToMarketPlaceItemMapperImpl
import com.walmart.marketplace.mappers.SubstituteItemCommandToMarketPlaceItemMapper
import com.walmart.marketplace.order.aggregateroot.MarketPlaceOrder
import com.walmart.marketplace.order.domain.entity.MarketPlaceItem
import com.walmart.marketplace.order.domain.valueobject.ItemIdentifier
import com.walmart.marketplace.order.repository.IMarketPlaceRepository
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class SubstituteItemCommandToMarketPlaceItemMapperSpec extends Specification {
    SubstituteItemCommandToMarketPlaceItemMapper marketPlaceCommandToEntityObjectMapper;
    IMarketPlaceRepository marketPlaceRepository = Mock(IMarketPlaceRepository.class)

    def setup() {
        marketPlaceCommandToEntityObjectMapper = new SubstituteItemCommandToMarketPlaceItemMapperImpl();
        ReflectionTestUtils.setField(marketPlaceCommandToEntityObjectMapper,
                "marketPlaceRepository",
                marketPlaceRepository)
    }

    def "Successful Mapping"() {
        given:
        MarketPlaceItem marketPlaceItem = MarketPlaceItem.builder()
                .externalItemId("1234")
                .itemDescription("A B C D")
                .quantity(2L)
                .marketPlaceOrder(Mock(MarketPlaceOrder.class))
                .itemIdentifier(Mock(ItemIdentifier.class))
                .build()

        MarketPlaceItem marketPlaceItem2 = MarketPlaceItem.builder()
                .externalItemId("1111")
                .itemDescription("A B C D")
                .quantity(2L)
                .marketPlaceOrder(Mock(MarketPlaceOrder.class))
                .itemIdentifier(Mock(ItemIdentifier.class))
                .build()

        List<MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand> pickedItems = new ArrayList<>();
        pickedItems.add(MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand.builder()
                .itemId("1111")
                .instanceId("1212uwy")
                .orderedQuantity(2L)
                .pickedItemCommand(MarketPlacePickCompleteCommand.PickedItemCommand.builder()
                        .orderedCin("1111")
                        .build())
                .build())

        pickedItems.add(MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand.builder()
                .itemId("1112")
                .instanceId("1212uwy")
                .orderedQuantity(2L)
                .pickedItemCommand(MarketPlacePickCompleteCommand.PickedItemCommand.builder()
                        .orderedCin("1234")
                        .substitutedItems(Arrays.asList(MarketPlacePickCompleteCommand.SubstitutedItem.builder()
                                .quantity(3L)
                                .externalItemId("1212")
                                .totalPrice(33.3)
                                .description("D E F G H")
                                .unitPrice(11.1)
                                .build()))
                        .build())
                .build())
        marketPlaceRepository.getNextIdentity() >> "pqrst"
        when:
        marketPlaceCommandToEntityObjectMapper.mapToSubstitutedItemEntityList(Arrays.asList(marketPlaceItem,marketPlaceItem2),
                pickedItems)
        then:
        assert marketPlaceItem.getSubstitutedItemList() != null
        assert marketPlaceItem.getSubstitutedItemList().size() == 1
        assert marketPlaceItem.getSubstitutedItemList().get(0).getId() != null
        assert marketPlaceItem.getSubstitutedItemList().get(0).getMarketPlaceItem() == marketPlaceItem
        assert marketPlaceItem.getSubstitutedItemList().get(0).getDescription() == "D E F G H"
        assert marketPlaceItem.getSubstitutedItemList().get(0).getExternalItemId() == "1212"
        assert marketPlaceItem.getSubstitutedItemList().get(0).getSubstitutedItemPriceInfo().getTotalPrice() == 33.3
        assert marketPlaceItem.getSubstitutedItemList().get(0).getSubstitutedItemPriceInfo().getUnitPrice() == 11.1
    }

    def "Successful Mapping for no substitutes"() {
        given:
        MarketPlaceItem marketPlaceItem = MarketPlaceItem.builder()
                .externalItemId("1234")
                .itemDescription("A B C D")
                .quantity(2L)
                .marketPlaceOrder(Mock(MarketPlaceOrder.class))
                .itemIdentifier(Mock(ItemIdentifier.class))
                .build()

        List<MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand> pickedItems = new ArrayList<>();
        pickedItems.add(MarketPlacePickCompleteCommand.MarketplacePickCompleteItemCommand.builder()
                .itemId("1111")
                .instanceId("1212uwy")
                .orderedQuantity(2L)
                .pickedItemCommand(MarketPlacePickCompleteCommand.PickedItemCommand.builder()
                        .orderedCin("1234")
                        .build())
                .build())
        when:
        marketPlaceCommandToEntityObjectMapper.mapToSubstitutedItemEntityList(Arrays.asList(marketPlaceItem), pickedItems)
        then:
        assert marketPlaceItem.getSubstitutedItemList() == null
    }
}
