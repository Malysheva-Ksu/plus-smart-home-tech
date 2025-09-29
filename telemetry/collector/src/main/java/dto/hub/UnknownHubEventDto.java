package dto.hub;

import dto.abstractDto.HubEventDto;
import dto.enums.HubEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UnknownHubEventDto extends HubEventDto {
    @Override
    public HubEventType getEventType() {
        return HubEventType.UNKNOWN_HUB_EVENT;
    }
}