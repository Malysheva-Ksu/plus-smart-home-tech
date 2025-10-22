package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scenario_actions")
@IdClass(ScenarioAction.ScenarioActionId.class)
public class ScenarioAction {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScenarioActionId implements Serializable {
        private Long scenario;
        private String sensor;
        private Long action;
    }
}