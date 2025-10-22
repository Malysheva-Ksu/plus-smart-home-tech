DROP TRIGGER IF EXISTS tr_bi_scenario_conditions_hub_id_check ON scenario_conditions CASCADE;
DROP TRIGGER IF EXISTS tr_bi_scenario_actions_hub_id_check ON scenario_actions CASCADE;

DROP FUNCTION IF EXISTS check_hub_id() CASCADE;

DROP TABLE IF EXISTS scenario_actions CASCADE;
DROP TABLE IF EXISTS scenario_conditions CASCADE;
DROP TABLE IF EXISTS actions CASCADE;
DROP TABLE IF EXISTS conditions CASCADE;
DROP TABLE IF EXISTS scenarios CASCADE;
DROP TABLE IF EXISTS sensors CASCADE;

CREATE TABLE scenarios (
    id BIGSERIAL PRIMARY KEY,
    hub_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_scenario_hub_name UNIQUE(hub_id, name)
);

CREATE TABLE sensors (
    id VARCHAR(255) PRIMARY KEY,
    hub_id VARCHAR(255) NOT NULL
);

CREATE TABLE conditions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    value INTEGER NOT NULL
);

CREATE TABLE actions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    value INTEGER NOT NULL
);

CREATE TABLE scenario_conditions (
    scenario_id BIGINT NOT NULL,
    sensor_id VARCHAR(255) NOT NULL,
    condition_id BIGINT NOT NULL,
    PRIMARY KEY (scenario_id, sensor_id, condition_id),
    CONSTRAINT fk_sc_scenario FOREIGN KEY (scenario_id) 
        REFERENCES scenarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_sc_sensor FOREIGN KEY (sensor_id) 
        REFERENCES sensors(id) ON DELETE CASCADE,
    CONSTRAINT fk_sc_condition FOREIGN KEY (condition_id) 
        REFERENCES conditions(id) ON DELETE CASCADE
);

CREATE TABLE scenario_actions (
    scenario_id BIGINT NOT NULL,
    sensor_id VARCHAR(255) NOT NULL,
    action_id BIGINT NOT NULL,
    PRIMARY KEY (scenario_id, sensor_id, action_id),
    CONSTRAINT fk_sa_scenario FOREIGN KEY (scenario_id) 
        REFERENCES scenarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_sa_sensor FOREIGN KEY (sensor_id) 
        REFERENCES sensors(id) ON DELETE CASCADE,
    CONSTRAINT fk_sa_action FOREIGN KEY (action_id) 
        REFERENCES actions(id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION check_hub_id()
RETURNS TRIGGER AS $$
DECLARE
    v_scenario_hub_id VARCHAR(255);
    v_sensor_hub_id VARCHAR(255);
BEGIN
    SELECT hub_id INTO v_scenario_hub_id 
    FROM scenarios 
    WHERE id = NEW.scenario_id;

    SELECT hub_id INTO v_sensor_hub_id 
    FROM sensors 
    WHERE id = NEW.sensor_id;

    IF v_scenario_hub_id IS NULL THEN
        RAISE EXCEPTION 'Scenario with id % does not exist', NEW.scenario_id;
    END IF;
    
    IF v_sensor_hub_id IS NULL THEN
        RAISE EXCEPTION 'Sensor with id % does not exist', NEW.sensor_id;
    END IF;

    IF v_scenario_hub_id != v_sensor_hub_id THEN
        RAISE EXCEPTION 'Hub IDs do not match: scenario hub_id=%, sensor hub_id=%', 
                        v_scenario_hub_id, v_sensor_hub_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_bi_scenario_conditions_hub_id_check
BEFORE INSERT ON scenario_conditions
FOR EACH ROW
EXECUTE FUNCTION check_hub_id();

CREATE TRIGGER tr_bi_scenario_actions_hub_id_check
BEFORE INSERT ON scenario_actions
FOR EACH ROW
EXECUTE FUNCTION check_hub_id();