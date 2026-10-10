package com.network.management.nms.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.network.management.nms.model.Device;
import com.network.management.nms.service.DeviceService;
import com.network.management.nms.network.snmp.SnmpService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final SnmpService snmpService;

    public DeviceController(DeviceService deviceService,
                            SnmpService snmpService) {
        this.deviceService = deviceService;
        this.snmpService = snmpService;
    }

    @PostMapping
    public Device registerDevice(@Valid @RequestBody Device device) {
        return deviceService.registerDevice(device);
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public Device getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
    }

    @PutMapping("/{id}")
    public Device updateDevice(
            @PathVariable Long id,
            @RequestBody Device device) {

        System.out.println("PUT DEVICE RECEIVED");
        System.out.println("ID: " + device.getId());
        System.out.println("Name: " + device.getName());
        System.out.println("Host: " + device.getHost());
        System.out.println("Type: " + device.getDeviceType());
        System.out.println("Protocols: " + device.getManagementProtocols());

        return deviceService.updateDevice(id, device);
    }

    @GetMapping("/{id}/snmp/system")
    public String getDeviceSystemDescription(@PathVariable Long id)
            throws Exception {

        Device device = deviceService.getDeviceById(id);

        return snmpService.getSystemDescription(device.getHost());
    }

    @GetMapping("/{id}/health")
    public Map<String, Object> getDeviceHealth(
            @PathVariable Long id) {

        Device device = deviceService.getDeviceById(id);

        boolean up = snmpService.isDeviceUp(device.getHost());

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("deviceId", device.getId());
        result.put("deviceName", device.getName());
        result.put("host", device.getHost());
        result.put("status", up ? "UP" : "DOWN");

        return result;
    }
}