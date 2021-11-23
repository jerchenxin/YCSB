# LevelDB Wrapper

## Description
This is a YCSB database abstraction for LevelDB.

## Uasage
Copy the jar.

```shell
cp ${LEVELDBJNI_HOME}/leveldb_jni.jar ${YCSB_HOME}/leveldb/lib
```

## Build the LevelDB binding.

```shell
mvn -pl com.yahoo.ycsb:leveldb-binding -am clean package
```

## Run the YCSB workloads. Example command

```shell
java -cp leveldb/target/*:leveldb/target/dependency/*:leveldb/lib/*: com.yahoo.ycsb.Client -load -db com.yahoo.ycsb.db.LevelDBClient -P workloads/workloada
```