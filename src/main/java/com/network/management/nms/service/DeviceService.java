package com.network.management.nms.service;
import java.util.List;
import com.network.management.nms.model.Device;
import com.network.management.nms.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    public Device registerDevice(Device device) {
        return deviceRepository.save(device);
    }
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
    }
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }
    public Device updateDevice(Long id, Device updatedDevice) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));

        existingDevice.setName(updatedDevice.getName());
        existingDevice.setHost(updatedDevice.getHost());
        existingDevice.setVendor(updatedDevice.getVendor());
        existingDevice.setModel(updatedDevice.getModel());
        existingDevice.setDeviceType(updatedDevice.getDeviceType());
        existingDevice.setManagementProtocols(updatedDevice.getManagementProtocols());

        return deviceRepository.save(existingDevice);
    }
}