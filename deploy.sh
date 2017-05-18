#! /bin/bash

mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases -N

cd mrpc-common
mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..

cd mrpc-core
mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..

cd mrpc-registry-zk
mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..

cd mrpc-spring-boot-starter
mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..

cd mrpc-serialize
mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases -N

cd mrpc-serialize-kryo
mvn clean deploy -Durl=http://106.75.120.228:8083/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ../../