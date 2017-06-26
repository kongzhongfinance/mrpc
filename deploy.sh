#! /bin/bash

mvn clean deploy -P snapshots -N

cd mrpc-common && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ..

cd mrpc-core && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ..

cd mrpc-registry-zk && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ..

cd mrpc-interceptor-validator && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ..

cd mrpc-metric-influxdb && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ..

cd mrpc-serialize && mvn clean deploy -P snapshots -N

cd mrpc-serialize-kryo && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ../../

cd mrpc-spring-boot-starter && mvn clean deploy -P snapshots -Dmaven.test.skip=true -U
cd ..