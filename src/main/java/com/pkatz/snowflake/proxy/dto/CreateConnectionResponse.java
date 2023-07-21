package com.pkatz.snowflake.proxy.dto;

import com.pkatz.snowflake.proxy.entity.SnowflakeConnection;

import java.util.Date;
import java.util.Optional;

public record CreateConnectionResponse(
        String id,
        String url,
        String username,
        String role,
        Optional<String> warehouse,
        Date createdAt,
        Date updatedAt
) {
    public static CreateConnectionResponse fromEntity(SnowflakeConnection entity) {
        return new CreateConnectionResponse(
                entity.getId(),
                entity.getUrl(),
                entity.getUsername(),
                entity.getRole(),
                entity.getWarehouse(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
