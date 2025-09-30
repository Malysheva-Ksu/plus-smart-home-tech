package dto.hub;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import dto.abstractDto.HubEventDto;
import dto.enums.HubEventType;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class UnknownHubEventDto extends HubEventDto {

    private final Map<String, Object> unknownFields = new HashMap<>();

    @Override
    public HubEventType getEventType() {
        return null;
    }

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }
}