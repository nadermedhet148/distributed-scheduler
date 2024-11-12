package io.worker.database.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


import org.jboss.logging.Logger;

@ApplicationScoped
public class JobsData {
    private static final Logger log = Logger.getLogger(JobsData.class);
//
//    @Inject
//    CqlSession dbConnector;
//
//    public void getJobs() {
//        log.info("getJobs");
//        try {
//            var rs = dbConnector.execute(SELECT_JSOB);
//            rs.forEach(rr -> {
//                System.out.println("id : " + rr.getString("frequency"));
//            });
//        } catch (Exception e) {
//            log.error("e", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static final String SELECT_JSOB = """
//            select * from scheduler.job
//            """;

}
