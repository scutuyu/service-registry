package com.tuyu.zk;

/**
 * 注册服务
 * @author tuyu
 * @date 8/10/18
 * Talk is cheap, show me the code.
 */
public interface Registry {

    /**
     * 注册服务
     * @param serviceName 服务名
     * @param serviceAddress 提供服务的地址，即ip:port
     */
    void register(String serviceName, String serviceAddress);
}
