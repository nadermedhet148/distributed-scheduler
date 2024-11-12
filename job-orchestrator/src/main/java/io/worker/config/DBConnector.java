package io.worker.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.InetSocketAddress;

@ApplicationScoped
public class DBConnector {
    @ConfigProperty(name = "db.url") String url;
    @ConfigProperty(name = "db.port") int port;
    @ConfigProperty(name = "db.keyspace") String keyspace;

    public CqlSessionBuilder getConnection() {
        return CqlSession.builder()
                .addContactPoint(new InetSocketAddress(url, port))
                .withKeyspace(keyspace)
                .withLocalDatacenter("datacenter1");
    }


}