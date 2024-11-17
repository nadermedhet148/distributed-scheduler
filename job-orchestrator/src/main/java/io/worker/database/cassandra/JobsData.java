package io.worker.database.cassandra;


import io.worker.config.DBConnector;
import io.worker.model.Job;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class JobsData {
    private static final Logger log = Logger.getLogger(JobsData.class);

    @Inject
    DBConnector connector;

    public List<Job> getJobs() {
        try (var dbConnector = connector.getConnection().build()) {
            var rs = dbConnector.execute(SELECT_JSOB);
            return rs.map(rr -> new Job(
                    rr.getUuid("id"),
                    rr.getString("frequency"),
                    rr.getString("metadata"),
                    rr.getInt("user_id"),
                    rr.getInt("segment"),
                    rr.getInstant("created_at"),
                    rr.getInstant("next_exec"),
                    rr.getInstant("last_exec"),
                    rr.getInt("retry")
            )).all();
        } catch (Exception e) {
            log.error("e", e);
            throw new RuntimeException(e);
        }
    }

    public void dummyData() {
        try (var dbConnector = connector.getConnection().build()) {
            for (int J = 0; J < 1000; J++) {
                for (int i = 0; i < 12; i++) {
                    dbConnector.execute("INSERT INTO scheduler.job (id, frequency, metadata, user_id, created_at, next_exec, last_exec, retry, segment)\n" +
                            "VALUES (uuid(), '{\"type\": \"every_minute\", \"frequency\", 2}', '{}', 1, toTimestamp(now()), toTimestamp(now()), toTimestamp(now()), 1, " + i + ");");
                }
            }
        } catch (Exception e) {
            log.error("e", e);
            throw new RuntimeException(e);
        }
    }


    private static final String SELECT_JSOB = """
            select * from scheduler.job where next_exec < toTimestamp(now()) ALLOW FILTERING;
            """;

}
