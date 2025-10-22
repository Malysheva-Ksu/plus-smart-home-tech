package model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.enums.ConditionOperation;
import model.enums.ConditionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private ConditionOperation operation;

    @Column(name = "value", nullable = false)
    private Integer value;

    public Condition(ConditionType type, ConditionOperation operation, Integer value) {
        this.type = type;
        this.operation = operation;
        this.value = value;
    }
}