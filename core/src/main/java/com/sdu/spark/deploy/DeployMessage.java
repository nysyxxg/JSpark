package com.sdu.spark.deploy;

import com.sdu.spark.rpc.RpcEndPointRef;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * JSpark集群部署消息
 *
 * @author hanhan.zhang
 * */
public interface DeployMessage extends Serializable {

    /**
     * 工作节点心跳信息
     * */
    @AllArgsConstructor
    class WorkerHeartbeat implements DeployMessage {
        public String workerId;
        public RpcEndPointRef worker;
    }

    class SendHeartbeat implements DeployMessage {}

    /**
     * 工作节点注册消息
     * */
    @AllArgsConstructor
    class RegisterWorker implements DeployMessage {
        public String workerId;
        public String host;
        public int port;
        public int cores;
        public long memory;
        public RpcEndPointRef worker;
    }

    /**
     * 工作节点注册响应父类
     * */
    interface RegisteredWorkerResponse {}

    /**
     * 工作节点注册响应消息
     * */
    @AllArgsConstructor
    class RegisteredWorker implements DeployMessage, RegisteredWorkerResponse {
        public RpcEndPointRef master;
    }

    /**
     * 工作节点注册失败消息
     * */
    @AllArgsConstructor
    class RegisterWorkerFailed implements DeployMessage, RegisteredWorkerResponse {
        public String message;
    }

    /**
     * 工作节点状态消息
     * */
    @AllArgsConstructor
    class WorkerLatestState implements DeployMessage {
        public String workerId;
        public List<ExecutorDescription> executors;
        public List<String> driverIds;
    }

    /**
     * 工作节点资源调度响应消息
     * */
    @AllArgsConstructor
    class WorkerSchedulerStateResponse implements DeployMessage {
        public String workerId;
        public List<ExecutorDescription> executors;
        public List<String> driverIds;
    }


    /**
     * Worker重连消息
     * */
    @AllArgsConstructor
    class ReconnectWorker implements DeployMessage {
        public RpcEndPointRef master;
    }

    /**
     * Executor变更消息
     * */
    @AllArgsConstructor
    class ExecutorStateChanged implements DeployMessage {
        public int executorId;
        public String appId;
        public ExecutorState state;
        public String message;
        public int exitStatus;
    }


    /**
     * Driver变更消息
     * */
    @AllArgsConstructor
    class DriverStateChanged implements DeployMessage {
        public String driverId;
        public DriverState state;
        public Exception exception;
    }
    /**
     * 注册Driver信息
     * */
    @AllArgsConstructor
    class RequestSubmitDriver implements DeployMessage {
        public DriverDescription driverDescription;
    }

    /**
     * 杀死Driver消息
     * */
    @AllArgsConstructor
    class RequestKillDriver implements DeployMessage {
        public String driverId;
    }

    /**
     * 工作节点启动Driver
     * */
    @AllArgsConstructor
    class LaunchDriver implements DeployMessage {
        public String driverId;
        public DriverDescription desc;
    }

    /**
     * 工作节点启动Executor
     * */
    @AllArgsConstructor
    class LaunchExecutor implements DeployMessage {
        public String appId;
        public int execId;
        public ApplicationDescription appDesc;
        public int cores;
        public int memory;
    }

    @AllArgsConstructor
    class ApplicationFinished implements DeployMessage {
        String appId;
    }

    /**
     * Driver状态查询消息
     * */
    @AllArgsConstructor
    class RequestDriverStatus implements DeployMessage {
        public String driverId;
    }


    /**
     * 向工作节点发送KillExecutor消息
     * */
    @AllArgsConstructor
    class KillExecutor implements DeployMessage {
        public String appId;
        public int execId;
    }



    /*******************************Spark App注册及资源申请(StandaloneAppClient)*****************************/
    @AllArgsConstructor
    class RegisterApplication implements DeployMessage {
        public ApplicationDescription appDescription;
        public RpcEndPointRef driver;
    }

    @AllArgsConstructor
    class RegisteredApplication implements DeployMessage {
        public String appId;
        public RpcEndPointRef master;
    }

    @AllArgsConstructor
    class UnregisterApplication implements DeployMessage {
        public String appId;
    }

    @AllArgsConstructor
    class ApplicationRemoved implements DeployMessage {
        public String message;
    }

    class StopAppClient implements DeployMessage {}

    @AllArgsConstructor
    class RequestExecutors implements DeployMessage {
        public String appId;
        public int requestedTotal;
    }

    @AllArgsConstructor
    class ExecutorAdded implements DeployMessage {
        public int execId;
        public String workerId;
        public String host;
        public int cores;
        public int memory;
    }

    @AllArgsConstructor
    class ExecutorUpdated implements DeployMessage {
        public int id;
        public ExecutorState state;
        public String message;
        public int exitStatus;
        public boolean workerLost;
    }

    @AllArgsConstructor
    class KillExecutors implements DeployMessage {
        public String appId;
        public List<String> executorIds;
    }

    @AllArgsConstructor
    class WorkerRemoved implements DeployMessage {
        public String id;
        public String host;
        public String message;
    }

    @AllArgsConstructor
    class MasterChanged implements DeployMessage {
        public RpcEndPointRef master;
        public String masterWebUiUrl;
    }

    @AllArgsConstructor
    class MasterChangeAcknowledged implements DeployMessage {
        public String appId;
    }
    /************************************************************************************************/
}
