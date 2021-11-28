# HashKV Wrapper

## Description
This is a YCSB database abstraction for HashKV.

## Uasage
Copy the jar.

```shell
mkdir ${YCSB_HOME}/hashkv/lib
cp ${HASHKVJNI_HOME}/hashkv_jni.jar ${YCSB_HOME}/hashkv/lib
```

## Build the HashKV binding.

```shell
mvn -pl site.ycsb:hashkv-binding -am clean package
```

## Env for hashkv
```shell
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${HASHKV_HOME}/bin:${HASHKV_HOME}/lib/leveldb/out-shared:${HASHKV_HOME}/lib/HdrHistogram_c-0.9.4/src
```

## Run the YCSB workloads. Example command

```shell
# need python2.7
./bin/ycsb load hashkv -s -P workloads/workloada
./bin/ycsb run hashkv -s -P workloads/workloada
```
