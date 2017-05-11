#! /bin/bash

mvn clean deploy -Durl=http://10.230.200.15:8081/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases -N

cd mrpc-common
mvn clean deploy -Durl=http://10.230.200.15:8081/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..

cd mrpc-core
mvn clean deploy -Durl=http://10.230.200.15:8081/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..

cd mrpc-serialize/mrpc-serialize-kryo
mvn clean deploy -Durl=http://10.230.200.15:8081/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ../../

cd mrpc-registry-zk
mvn clean deploy -Durl=http://10.230.200.15:8081/nexus/content/repositories/releases/ -DrepositoryId=releases -P releases
cd ..
