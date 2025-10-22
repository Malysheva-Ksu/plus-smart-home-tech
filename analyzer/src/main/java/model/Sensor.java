package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;
}