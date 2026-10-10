package com.network.management.nms.service;

import com.network.management.nms.network.restconf.RestconfClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RestconfService {

    private final RestconfClient restconfClient;

    @Value("${nms.restconf.host}")
    private String host;

    @Value("${nms.restconf.port}")
    private int port;

    @Value("${nms.restconf.username}")
    private String username;

    @Value("${nms.restconf.password}")
    private String password;

    public RestconfService(RestconfClient restconfClient) {
        this.restconfClient = restconfClient;
    }

    public String getInterfaces() throws Exception {
        return restconfClient.getInterfaces(
                host,
                port,
                username,
                password
        );
    }
    public boolean isAvailable() {
        try {
            getInterfaces();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}