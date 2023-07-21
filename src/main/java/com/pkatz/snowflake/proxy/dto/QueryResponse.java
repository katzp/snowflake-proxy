package com.pkatz.snowflake.proxy.dto;

import java.util.ArrayList;

public record QueryResponse(
        ArrayList<ArrayList<Object>> records,
        ArrayList<String> columns,
        long recordCount,
        String queryId,
        String connectionId
) {
}
