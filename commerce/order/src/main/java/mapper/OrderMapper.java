package mapper;

import model.order.Address;
import model.order.CreateNewOrderRequest;
import model.order.Order;
import model.order.OrderDto;
import model.order.OrderProduct;
import model.shoppingCart.CartItem;
import model.shoppingCart.ShoppingCart;
import model.warehouse.AddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class OrderMapper {

    private static final Logger log = LoggerFactory.getLogger(OrderMapper.class);

    private OrderMapper() {
    }

    public static Order toOrder(CreateNewOrderRequest request, String username) {
        log.debug("Mapping CreateNewOrderRequest to Order entity for user: {}", username);

        Order order = new Order();
        ShoppingCart cart = request.shoppingCart();

        order.setProducts(toOrderProductList(cart.getItems()));

        order.setShoppingCartId(cart.getId());
        order.setUsername(username);
        order.setDeliveryAddress(toAddress(request.deliveryAddress()));

        return order;
    }

    public static List<OrderProduct> toOrderProductList(List<CartItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(item -> {
                    OrderProduct orderProduct = new OrderProduct();
                    orderProduct.setProductId(item.getProductId());
                    orderProduct.setQuantity(item.getQuantity());
                    return orderProduct;
                })
                .collect(Collectors.toList());
    }

    public static OrderDto toOrderDto(Order order) {
        log.debug("Mapping Order entity to OrderDto");

        OrderDto orderDto = new OrderDto();
        orderDto.setProducts(toOrderProductDtoMap(order.getProducts()));

        Optional.ofNullable(order.getDeliveryDetails()).ifPresent(details -> {
            orderDto.setDeliveryWeight(details.getDeliveryWeight());
            orderDto.setDeliveryVolume(details.getDeliveryVolume());
            orderDto.setDeliveryPrice(details.getDeliveryPrice());
            orderDto.setDeliveryId(details.getDeliveryId());
            orderDto.setFragile(details.isFragile());
        });

        return orderDto;
    }

    public static List<OrderDto> toOrderDtoList(List<Order> orders) {
        return Optional.ofNullable(orders)
                .orElse(Collections.emptyList())
                .stream()
                .map(OrderMapper::toOrderDto)
                .collect(Collectors.toList());
    }

    public static Map<UUID, Integer> toOrderProductDtoMap(List<OrderProduct> products) {
        if (products == null) {
            return Collections.emptyMap();
        }

        return products.stream()
                .collect(Collectors.toMap(
                        OrderProduct::getProductId,
                        OrderProduct::getQuantity,
                        (existing, replacement) -> existing
                ));
    }

    public static AddressDto toAddressDto(Address address) {
        if (address == null) return null;

        AddressDto dto = new AddressDto();

        dto.setCountry(address.getCountry());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setHouse(address.getHouse());
        dto.setFlat(address.getFlat());

        return dto;
    }

    public static Address toAddress(AddressDto addressDto) {
        if (addressDto == null) return null;

        Address address = new Address();

        address.setCountry(addressDto.getCountry());
        address.setCity(addressDto.getCity());
        address.setStreet(addressDto.getStreet());
        address.setHouse(addressDto.getHouse());
        address.setFlat(addressDto.getFlat());

        return address;
    }
}