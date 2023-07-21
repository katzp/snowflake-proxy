package com.pkatz.snowflake.proxy.repository;

import com.pkatz.snowflake.proxy.entity.SnowflakeConnection;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface SnowflakeConnectionRepository extends CrudRepository<SnowflakeConnection, String> {
}
