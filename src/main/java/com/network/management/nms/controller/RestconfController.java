package com.network.management.nms.controller;

import com.network.management.nms.service.RestconfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restconf")
public class RestconfController {

    private final RestconfService restconfService;

    public RestconfController(RestconfService restconfService) {
        this.restconfService = restconfService;
    }

    @GetMapping("/interfaces")
    public ResponseEntity<String> getInterfaces() {

        try {
            String response =
                    restconfService.getInterfaces();

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("RESTCONF request failed: "
                            + e.getMessage());
        }
    }
}