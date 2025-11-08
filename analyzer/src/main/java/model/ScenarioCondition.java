package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "scenario_conditions")
@IdClass(ScenarioCondition.ScenarioConditionId.class)
@EqualsAndHashCode(of = {"scenarioId", "sensorId", "conditionId"})
public class ScenarioCondition {

    @Id
    @Column(name = "scenario_id")
    private Long scenarioId;

    @Id
    @Column(name = "sensor_id")
    private String sensorId;

    @Id
    @Column(name = "condition_id")
    private Long conditionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", insertable = false, updatable = false)
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", insertable = false, updatable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "condition_id", insertable = false, updatable = false)
    private Condition condition;

    @Data
    public static class ScenarioConditionId implements Serializable {
        private Long scenarioId;
        private String sensorId;
        private Long conditionId;
    }
}