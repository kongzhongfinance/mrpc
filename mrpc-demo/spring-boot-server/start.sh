#!/usr/bin/env bash

mvn clean package

java -jar -javaagent:/Users/biezhi/software/pinpoint-agent-1.7.3/pinpoint-bootstrap-1.7.3.jar -Dpinpoint.agentId=pinpoint-rpc-server -Dpinpoint.applicationName=test_rpc_server target/spring-boot-server.jar