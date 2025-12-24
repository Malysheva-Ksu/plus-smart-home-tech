package model.warehouse;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "warehouse.address")
@Validated
public class WarehouseAddressConfig {

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    @NotBlank
    private String street;

    @NotBlank
    private String house;

    private String flat;

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getHouse() { return house; }
    public void setHouse(String house) { this.house = house; }

    public String getFlat() { return flat; }
    public void setFlat(String flat) { this.flat = flat; }

    public WarehouseAddressDto toDto() {
        return WarehouseAddressDto.builder()
                .country(this.country)
                .city(this.city)
                .street(this.street)
                .house(this.house)
                .flat(this.flat)
                .build();
    }
}