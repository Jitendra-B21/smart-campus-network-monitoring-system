package com.network.management.nms.controller;

import com.network.management.nms.model.Alarm;
import com.network.management.nms.model.AlarmState;
import com.network.management.nms.model.Device;
import com.network.management.nms.model.DeviceStatus;
import com.network.management.nms.model.ManagementProtocol;
import com.network.management.nms.network.snmp.NetworkInterface;
import com.network.management.nms.repository.AlarmRepository;
import com.network.management.nms.repository.DeviceRepository;
import com.network.management.nms.service.DeviceMonitoringService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DeviceRepository deviceRepository;
    private final AlarmRepository alarmRepository;
    private final DeviceMonitoringService deviceMonitoringService;

    public DashboardController(
            DeviceRepository deviceRepository,
            AlarmRepository alarmRepository,
            DeviceMonitoringService deviceMonitoringService) {

        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
        this.deviceMonitoringService = deviceMonitoringService;
    }

    @GetMapping
    public Map<String, Object> getDashboard() {

        // -------------------------------------------------------------
        // Read current database state
        // -------------------------------------------------------------

        List<Device> devices =
                deviceRepository.findAll();

        List<Alarm> activeAlarms =
                alarmRepository.findByState(
                        AlarmState.ACTIVE
                );

        // -------------------------------------------------------------
        // Device counts
        // -------------------------------------------------------------

        long devicesUp =
                devices.stream()
                        .filter(device ->
                                device.getStatus() != null
                                        && device.getStatus()
                                        == DeviceStatus.UP)
                        .count();

        long devicesDown =
                devices.stream()
                        .filter(device ->
                                device.getStatus() != null
                                        && device.getStatus()
                                        == DeviceStatus.DOWN)
                        .count();

        // -------------------------------------------------------------
        // Device summaries
        // -------------------------------------------------------------

        List<Map<String, Object>> deviceSummaries =
                new ArrayList<>();

        for (Device device : devices) {

            Map<String, Object> summary =
                    new HashMap<>();

            summary.put(
                    "id",
                    device.getId()
            );

            summary.put(
                    "name",
                    device.getName()
            );

            summary.put(
                    "host",
                    device.getHost()
            );

            summary.put(
                    "vendor",
                    device.getVendor()
            );

            summary.put(
                    "model",
                    device.getModel()
            );

            summary.put(
                    "deviceType",
                    device.getDeviceType()
            );

            summary.put(
                    "status",
                    device.getStatus()
            );

            summary.put(
                    "managementProtocols",
                    device.getManagementProtocols()
            );

            // ---------------------------------------------------------
            // SNMP status
            // ---------------------------------------------------------

            boolean snmpConfigured =
                    device.getManagementProtocols()
                            .contains(
                                    ManagementProtocol.SNMP
                            );

            summary.put(
                    "snmpStatus",
                    snmpConfigured
                            ? (
                            device.getStatus()
                                    == DeviceStatus.UP
                                    ? "UP"
                                    : "DOWN"
                    )
                            : "N/A"
            );

            // ---------------------------------------------------------
            // NETCONF status
            // ---------------------------------------------------------

            boolean netconfConfigured =
                    device.getManagementProtocols()
                            .contains(
                                    ManagementProtocol.NETCONF
                            );

            summary.put(
                    "netconfStatus",
                    netconfConfigured
                            ? deviceMonitoringService
                            .getNetconfStatus(
                                    device.getId()
                            )
                            : "N/A"
            );

            // ---------------------------------------------------------
            // RESTCONF status
            // ---------------------------------------------------------

            boolean restconfConfigured =
                    device.getManagementProtocols()
                            .contains(
                                    ManagementProtocol.RESTCONF
                            );

            summary.put(
                    "restconfStatus",
                    restconfConfigured
                            ? deviceMonitoringService
                            .getRestconfStatus(
                                    device.getId()
                            )
                            : "N/A"
            );

            // ---------------------------------------------------------
            // SNMP interface information
            // ---------------------------------------------------------

            List<NetworkInterface> interfaces =
                    deviceMonitoringService.getInterfaces(
                            device.getId()
                    );

            summary.put(
                    "interfaces",
                    interfaces
            );

            deviceSummaries.add(summary);
        }

        // -------------------------------------------------------------
        // Final dashboard response
        // -------------------------------------------------------------

        Map<String, Object> dashboard =
                new HashMap<>();

        dashboard.put(
                "totalDevices",
                devices.size()
        );

        dashboard.put(
                "devicesUp",
                devicesUp
        );

        dashboard.put(
                "devicesDown",
                devicesDown
        );

        dashboard.put(
                "activeAlarms",
                activeAlarms.size()
        );

        dashboard.put(
                "devices",
                deviceSummaries
        );

        dashboard.put(
                "alarms",
                activeAlarms
        );

        return dashboard;
    }
}