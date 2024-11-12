-- create keyspace
CREATE KEYSPACE scheduler
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 2};

-- create table
CREATE TABLE scheduler.job (
    id UUID PRIMARY KEY,
    frequency TEXT,
    metadata TEXT,
    user_id INT,
    segment INT,
    created_at TIMESTAMP,
    next_exec TIMESTAMP,
    last_exec TIMESTAMP,
    retry INT
);


CREATE TABLE scheduler.job_exec (
    id UUID PRIMARY KEY,
    job_id UUID ,
    exec_at DATE,
    status Text
);


INSERT INTO scheduler.job (id, frequency, metadata, user_id, created_at, next_exec, last_exec, retry, segment)
VALUES (uuid(), '{"type": "every_minute", "frequency", 2}', '{}', 1, toTimestamp(now()), toTimestamp(now()), toTimestamp(now()), 1, 1);
