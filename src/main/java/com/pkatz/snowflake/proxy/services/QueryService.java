package com.pkatz.snowflake.proxy.services;

import com.pkatz.snowflake.proxy.dto.QueryResponse;
import com.pkatz.snowflake.proxy.exceptions.InvalidConnectionException;
import com.pkatz.snowflake.proxy.exceptions.NoSuchConnectionException;
import net.snowflake.client.jdbc.SnowflakePreparedStatement;
import net.snowflake.client.jdbc.SnowflakeResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class QueryService {
    private SnowflakeConnectionService connectionService;
    private final Logger logger = LoggerFactory.getLogger(QueryService.class);

    public QueryService(SnowflakeConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public QueryResponse query(String connectionId, String sql, Optional<Object[]> bindParameters)
            throws NoSuchConnectionException, InvalidConnectionException {
        Optional<Connection> conn = connectionService.getConnectionFromPool(connectionId);
        if (conn.isEmpty()) {
            throw new NoSuchConnectionException("Connection does not exist");
        }
        try (
                Connection connection = conn.get();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            if (bindParameters.isPresent()) {
                Object[] params = bindParameters.get();
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
            }
            statement.execute();
            ResultSet result = statement.getResultSet();
            if (result == null) {
                SnowflakePreparedStatement snowStatement = statement.unwrap(SnowflakePreparedStatement.class);
                return new QueryResponse(new ArrayList<>(), new ArrayList<>(), 0, snowStatement.getQueryID(), connectionId);
            }
            ResultSetMetaData metaData = result.getMetaData();
            int columnCount = metaData.getColumnCount();
            ArrayList<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnName(i));
            }

            ArrayList<ArrayList<Object>> records = new ArrayList<>();
            while (result.next()) {
                ArrayList<Object> record = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    record.add(result.getObject(i));
                }
                records.add(record);
            }
            SnowflakeResultSet snowResult = result.unwrap(SnowflakeResultSet.class);
            return new QueryResponse(records, columns, records.size(), snowResult.getQueryID(), connectionId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
