#!/usr/bin/env bash

mvn clean package

java -jar -javaagent:/Users/biezhi/software/pinpoint-agent-1.7.3/pinpoint-bootstrap-1.7.3.jar -Dpinpoint.agentId=pinpoint-rpc-client -Dpinpoint.applicationName=test_rpc_client target/spring-boot-client.jar