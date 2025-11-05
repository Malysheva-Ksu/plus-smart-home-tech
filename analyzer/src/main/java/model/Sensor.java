package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    private String id;

    @Column(nullable = true)
    private String deviceType;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    public Sensor(String id, String hubId) {
        this.id = id;
        this.hubId = hubId;
    }
}