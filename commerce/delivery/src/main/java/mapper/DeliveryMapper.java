package mapper;

import model.delivery.Address;
import model.delivery.CreateNewDeliveryRequest;
import model.delivery.Delivery;
import model.delivery.DeliveryDto;
import model.warehouse.AddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class DeliveryMapper {

    private static final Logger log = LoggerFactory.getLogger(DeliveryMapper.class);

    private DeliveryMapper() {
    }

    public static Delivery toDelivery(CreateNewDeliveryRequest request) {
        log.debug("Mapping CreateNewDeliveryRequest to Delivery entity");

        Delivery delivery = new Delivery();

        delivery.setFromAddress(toAddress(request.fromAddress()));
        delivery.setToAddress(toAddress(request.toAddress()));

        Optional.ofNullable(request.orderDto()).ifPresent(order -> {
            delivery.setDeliveryWeight(order.getDeliveryWeight());
            delivery.setDeliveryVolume(order.getDeliveryVolume());
            delivery.setFragile(order.isFragile());
        });

        return delivery;
    }

    public static DeliveryDto toDeliveryDto(Delivery delivery) {
        log.debug("Mapping Delivery entity to DeliveryDto");

        DeliveryDto dto = new DeliveryDto();

        dto.setFromAddress(toAddressDto(delivery.getFromAddress()));
        dto.setToAddress(toAddressDto(delivery.getToAddress()));
        dto.setOrderId(delivery.getOrderId());
        dto.setDeliveryState(delivery.getDeliveryState());

        return dto;
    }

    public static AddressDto toAddressDto(Address address) {
        if (address == null) return null;

        AddressDto dto = new AddressDto();

        dto.setCountry(address.getCountry());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setHouse(address.getHouse());

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