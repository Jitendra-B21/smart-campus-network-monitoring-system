package com.network.management.nms.controller;

import com.network.management.nms.service.NetconfService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/netconf")
public class NetconfController {

    private final NetconfService netconfService;

    public NetconfController(NetconfService netconfService) {
        this.netconfService = netconfService;
    }

    @GetMapping("/hello")
    public String getHello() throws Exception {
        return netconfService.getHello();
    }
}