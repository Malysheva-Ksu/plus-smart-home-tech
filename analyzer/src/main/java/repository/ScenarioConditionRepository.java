package repository;

import model.ScenarioCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioCondition.ScenarioConditionId> {
}