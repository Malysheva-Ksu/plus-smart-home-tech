package dto.base;

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