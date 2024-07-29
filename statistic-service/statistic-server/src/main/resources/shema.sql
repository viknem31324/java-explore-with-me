DROP TABLE IF EXISTS STATISTICS CASCADE;

CREATE TABLE IF NOT EXISTS STATISTICS (
    ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    APP VARCHAR(60) NOT NULL,
    URI VARCHAR(200) NOT NULL,
    IP VARCHAR(60) NOT NULL,
    TIME_STATISTIC TIMESTAMP WITHOUT TIME ZONE NOT NULL
);