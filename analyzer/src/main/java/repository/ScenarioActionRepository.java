package repository;

import model.ScenarioAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioAction.ScenarioActionId> {
}