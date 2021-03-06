package com.sdu.spark.utils;

import com.sdu.spark.rpc.RpcAddress;
import com.sdu.spark.rpc.RpcEndPointRef;
import com.sdu.spark.rpc.RpcEnv;
import com.sdu.spark.rpc.SparkConf;

/**
 * @author hanhan.zhang
 * */
public class RpcUtils {

    private static int MAX_MESSAGE_SIZE_IN_MB = Integer.MAX_VALUE / 1024 / 1024;

    public static RpcEndPointRef makeDriverRef(String name, SparkConf conf, RpcEnv rpcEnv) {
        String driverHost = conf.get("spark.driver.host", "localhost");
        int driverPort = conf.getInt("spark.driver.port", 7077);
        return rpcEnv.setRpcEndPointRef(name, new RpcAddress(driverHost, driverPort));
    }

    public static int maxMessageSizeBytes(SparkConf conf) {
        int maxSizeInMB = conf.getInt("spark.rpc.message.maxSize", 128);
        if (maxSizeInMB > MAX_MESSAGE_SIZE_IN_MB) {
            throw new IllegalArgumentException(
                    String.format("spark.rpc.message.maxSize should not be greater than %s MB", MAX_MESSAGE_SIZE_IN_MB));
        }
        return maxSizeInMB * 1024 * 1024;
    }

}
