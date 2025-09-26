package dto.hub;

import dto.base.HubEventDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SensorRegisteredEventDto extends HubEventDto {
    private String sensorId;
    private String sensorType;
}