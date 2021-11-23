# LevelDB Wrapper

## Description
This is a YCSB database abstraction for LevelDB.

## Uasage
Copy the jar.

```shell
mkdir ${YCSB_HOME}/leveldb/lib
cp ${LEVELDBJNI_HOME}/leveldb_jni.jar ${YCSB_HOME}/leveldb/lib
```

## Build the LevelDB binding.

```shell
mvn -pl site.ycsb:leveldb-binding -am clean package
```

## Env for leveldb
```shell
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${LEVELDB_HOME}/build
```

## Run the YCSB workloads. Example command

```shell
# need python2.7
./bin/ycsb load leveldb -s -P workloads/workloada
./bin/ycsb run leveldb -s -P workloads/workloada
```
