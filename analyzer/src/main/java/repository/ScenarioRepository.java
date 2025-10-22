package repository;

import model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {

    @Query("SELECT DISTINCT s FROM Scenario s " +
            "LEFT JOIN FETCH s.conditions sc " +
            "LEFT JOIN FETCH sc.condition " +
            "LEFT JOIN FETCH sc.sensor " +
            "LEFT JOIN FETCH s.actions sa " +
            "LEFT JOIN FETCH sa.action " +
            "LEFT JOIN FETCH sa.sensor " +
            "WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithDetails(@Param("hubId") String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    void deleteByHubId(String hubId);
}