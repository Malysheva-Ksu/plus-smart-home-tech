package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scenario_actions")
@IdClass(ScenarioAction.ScenarioActionId.class)
public class ScenarioAction {

    @Id
    @Column(name = "scenario_id")
    private Long scenarioId;

    @Id
    @Column(name = "sensor_id")
    private String sensorId;

    @Id
    @Column(name = "action_id")
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", insertable = false, updatable = false)
    private Action action;

    @Data
    public static class ScenarioActionId implements Serializable {
        private Long scenarioId;
        private String sensorId;
        private Long actionId;
    }
}