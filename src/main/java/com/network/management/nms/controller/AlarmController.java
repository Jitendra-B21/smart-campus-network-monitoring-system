package com.network.management.nms.controller;

import com.network.management.nms.model.Alarm;
import com.network.management.nms.service.AlarmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GetMapping
    public List<Alarm> getAllAlarms() {
        return alarmService.getAllAlarms();
    }

    @GetMapping("/active")
    public List<Alarm> getActiveAlarms() {
        return alarmService.getActiveAlarms();
    }
}