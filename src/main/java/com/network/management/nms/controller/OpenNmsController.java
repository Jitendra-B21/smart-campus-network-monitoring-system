package com.network.management.nms.controller;

import com.network.management.nms.service.OpenNmsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opennms")
public class OpenNmsController {

    private final OpenNmsService openNmsService;

    public OpenNmsController(OpenNmsService openNmsService) {
        this.openNmsService = openNmsService;
    }

    @GetMapping("/nodes")
    public String getNodes() throws Exception {
        return openNmsService.getNodes();
    }
}