package com.pkatz.snowflake.proxy.controllers;

import com.pkatz.snowflake.proxy.dto.CreateConnectionRequest;
import com.pkatz.snowflake.proxy.dto.CreateConnectionResponse;
import com.pkatz.snowflake.proxy.dto.QueryRequest;
import com.pkatz.snowflake.proxy.dto.QueryResponse;
import com.pkatz.snowflake.proxy.entity.SnowflakeConnection;
import com.pkatz.snowflake.proxy.exceptions.InvalidConnectionException;
import com.pkatz.snowflake.proxy.exceptions.NoSuchConnectionException;
import com.pkatz.snowflake.proxy.services.QueryService;
import com.pkatz.snowflake.proxy.services.SnowflakeConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v0/connections", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConnectionController {
    SnowflakeConnectionService connectionService;
    QueryService queryService;

    public ConnectionController(SnowflakeConnectionService connectionService, QueryService queryService) {
        this.connectionService = connectionService;
        this.queryService = queryService;
    }

    @PostMapping
    ResponseEntity<CreateConnectionResponse> createConnection(@Valid @RequestBody CreateConnectionRequest connectionProps)
            throws InvalidConnectionException {
        SnowflakeConnection conn = connectionService.createNewConnection(
                connectionProps.account(),
                connectionProps.username(),
                connectionProps.password(),
                connectionProps.role(),
                connectionProps.warehouse()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/" + conn.getId()).build().toUri();
        return ResponseEntity.created(location).body(CreateConnectionResponse.fromEntity(conn));
    }

    @GetMapping("/{connectionId}")
    ResponseEntity<CreateConnectionResponse> getConnection(@PathVariable String connectionId)
            throws NoSuchConnectionException {
        Optional<SnowflakeConnection> conn = connectionService.getConnectionDetails(connectionId);
        if (conn.isEmpty()) {
            throw new NoSuchConnectionException("Connection does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK).body(CreateConnectionResponse.fromEntity(conn.get()));
    }

    @PutMapping("/{connectionId}")
    ResponseEntity<CreateConnectionResponse> getConnection(
            @PathVariable String connectionId,
            @Valid @RequestBody CreateConnectionRequest connectionProps
    ) throws InvalidConnectionException {
        SnowflakeConnection connection = connectionService.updateConnection(
                connectionId,
                connectionProps.account(),
                connectionProps.username(),
                connectionProps.password(),
                connectionProps.role(),
                connectionProps.warehouse()
        );
        return ResponseEntity.status(HttpStatus.OK).body(CreateConnectionResponse.fromEntity(connection));
    }

    @PostMapping("/{connectionId}/query")
    ResponseEntity<QueryResponse> query(@PathVariable String connectionId, @RequestBody QueryRequest query)
            throws NoSuchConnectionException, InvalidConnectionException {
        QueryResponse response = queryService.query(connectionId, query.sql(), query.bindParameters());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
