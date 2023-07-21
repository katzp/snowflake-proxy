package com.pkatz.snowflake.proxy.dto;


import java.util.Optional;

public record QueryRequest(String sql, Optional<Object[]> bindParameters) {
}
