#! /bin/bash

mvn clean install -P snapshots -N

cd mrpc-common && mvn clean install -P snapshots -Dmaven.test.skip=true
cd ..

cd mrpc-core && mvn clean install -P snapshots -Dmaven.test.skip=true
cd ..

cd mrpc-registry-zk && mvn clean install -P snapshots -Dmaven.test.skip=true
cd ..

cd mrpc-interceptor-validator && mvn clean install -P snapshots -Dmaven.test.skip=true
cd ..

cd mrpc-serialize && mvn clean install -P snapshots -N

cd mrpc-serialize-kryo && mvn clean install -P snapshots -Dmaven.test.skip=true
cd ../../

cd mrpc-spring-boot-starter && mvn clean install -P snapshots -Dmaven.test.skip=true
cd ..