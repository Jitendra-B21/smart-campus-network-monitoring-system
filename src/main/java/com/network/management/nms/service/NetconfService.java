package com.network.management.nms.service;

import com.network.management.nms.network.netconf.NetconfClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NetconfService {

    private final NetconfClient netconfClient;

    @Value("${nms.netconf.host}")
    private String host;

    @Value("${nms.netconf.port}")
    private int port;

    @Value("${nms.netconf.username}")
    private String username;

    @Value("${nms.netconf.password}")
    private String password;

    public NetconfService(NetconfClient netconfClient) {
        this.netconfClient = netconfClient;
    }

    public String getHello() throws Exception {
        return netconfClient.getHello(
                host,
                port,
                username,
                password
        );
    }
    public boolean isAvailable() {
        try {
            getHello();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}