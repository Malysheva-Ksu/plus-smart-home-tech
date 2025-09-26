package dto.hub;

import dto.base.HubEventDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SensorDeregisteredEventDto extends HubEventDto {
    private String sensorId;
}