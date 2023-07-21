## Snowflake Proxy
This is a toy project to trouble shoot one of the shortcomings of
building serverless applications on top of Snowflake, which is 
the overhead of creating & managing many connections as opposed
to using traditional connection pools.

This leads to many requests serviced where we see over 
half of the request is spent creating a connection with Snowflake.

The project is a plain and simple Spring Boot app, which can store
connection details in Dynamo DB and holds open a local connection pool
that can be queried via API request.

## Running locally
Ensure java 17 is installed
```
java --version
```
Start up local dynamo instance
```
docker compose up
```
Run spring boot server
```
./gradlew bootRun --args='--spring.profiles.active=local'
```