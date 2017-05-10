#! /bin/bash

mvn clean install -N

mvn clean install -pl mrpc-common -am -Dmaven.test.skip=true -U
mvn clean install -pl mrpc-core -am -Dmaven.test.skip=true -U
mvn clean install -pl mrpc-registry-zk -am -Dmaven.test.skip=true -U
mvn clean install -pl mrpc-serialize/mrpc-serialize-kryo -am -Dmaven.test.skip=true -U
