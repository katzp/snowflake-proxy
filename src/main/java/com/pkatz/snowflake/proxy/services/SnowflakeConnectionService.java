package com.pkatz.snowflake.proxy.services;

import com.pkatz.snowflake.proxy.entity.SnowflakeConnection;
import com.pkatz.snowflake.proxy.exceptions.InvalidConnectionException;
import com.pkatz.snowflake.proxy.repository.SnowflakeConnectionRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

@Service
public class SnowflakeConnectionService {
    SnowflakeConnectionRepository repository;
    HashMap<String, HikariDataSource> liveConnections = new HashMap<>();
    static final String SNOWFLAKE_JDBC_URL = "jdbc:snowflake://%s.snowflakecomputing.com";
    static final String SNOWFLAKE_JDBC_DRIVER_CLASS = "net.snowflake.client.jdbc.SnowflakeDriver";
    static final String SNOWFLAKE_QUERY_TAG = "snowflake-proxy";
    private final Logger logger = LoggerFactory.getLogger(SnowflakeConnectionService.class);

    public SnowflakeConnectionService(SnowflakeConnectionRepository repository) {
        this.repository = repository;
    }

    public static String buildJdbcUrl(String account) {
        return String.format(SNOWFLAKE_JDBC_URL, account);
    }

    public SnowflakeConnection createNewConnection(
            String account,
            String username,
            String password,
            String role,
            Optional<String> warehouse
    ) throws InvalidConnectionException {
        String url = buildJdbcUrl(account);
        HikariDataSource ds = createDataSource(url, username, password, role, warehouse);
        SnowflakeConnection entity = new SnowflakeConnection();
        entity.setUrl(url);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setRole(role);
        entity.setWarehouse(warehouse);
        SnowflakeConnection connection = repository.save(entity);
        liveConnections.put(connection.getId(), ds);
        return connection;
    }

    public SnowflakeConnection updateConnection(
            String connectionId,
            String account,
            String username,
            String password,
            String role,
            Optional<String> warehouse
    ) throws InvalidConnectionException {
        String url = buildJdbcUrl(account);
        HikariDataSource ds = createDataSource(url, username, password, role, warehouse);
        synchronized (liveConnections.get(connectionId)) {
            liveConnections.put(connectionId, ds);
        }
        SnowflakeConnection entity = new SnowflakeConnection();
        entity.setId(connectionId);
        entity.setUrl(url);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setRole(role);
        entity.setWarehouse(warehouse);
        SnowflakeConnection connection = repository.save(entity);
        return connection;
    }

    public Optional<SnowflakeConnection> getConnectionDetails(String connectionId) {
        return repository.findById(connectionId);
    }

    Optional<Connection> getConnectionFromPool(String connectionId) throws InvalidConnectionException {
        HikariDataSource datasource = liveConnections.get(connectionId);
        if (datasource != null) {
            try {
                return Optional.of(datasource.getConnection());
            } catch (SQLException error) {
                logger.warn("Cannot retrieve connection from id " + connectionId);
            }
        }
        Optional<SnowflakeConnection> connInfoOption = repository.findById(connectionId);
        if (connInfoOption.isPresent()) {
            // Rebuild datasource and return connection
            SnowflakeConnection connInfo = connInfoOption.get();
            HikariDataSource datasourceRebuilt;
            try {
                datasourceRebuilt = createDataSource(connInfo.getUrl(), connInfo.getUsername(), connInfo.getPassword(), connInfo.getRole(), connInfo.getWarehouse());
            } catch (InvalidConnectionException e) {
                logger.warn("Connection info is no longer valid for id " + connectionId);
                throw new InvalidConnectionException("Connection info is no longer valid for id " + connectionId);
            }
            liveConnections.put(connectionId, datasourceRebuilt);
            try {
                return Optional.of(datasourceRebuilt.getConnection());
            } catch (SQLException error) {
                logger.warn("Cannot retrieve connection from id " + connectionId);
            }
        }
        return Optional.empty();
    }

    private HikariDataSource createDataSource(
            String url,
            String username,
            String password,
            String role,
            Optional<String> warehouse
    ) throws InvalidConnectionException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(SNOWFLAKE_JDBC_DRIVER_CLASS);
        config.setJdbcUrl(url);

        Properties configProperties = new Properties();
        configProperties.put("user", username);
        configProperties.put("password", password);
        configProperties.put("role", role);
        configProperties.put("JDBC_QUERY_RESULT_FORMAT", "JSON"); // Required for JDK17
        configProperties.put("application", SNOWFLAKE_QUERY_TAG);
        warehouse.ifPresent(w -> configProperties.put("warehouse", w));
        config.setDataSourceProperties(configProperties);

        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new InvalidConnectionException("Invalid connection");
        }
    }
}
