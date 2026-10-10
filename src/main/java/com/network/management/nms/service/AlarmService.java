package com.network.management.nms.service;

import com.network.management.nms.model.Alarm;
import com.network.management.nms.model.AlarmState;
import com.network.management.nms.model.Device;
import com.network.management.nms.repository.AlarmRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public AlarmService(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
    }

    public Alarm generateDeviceDownAlarm(Device device) {

        return alarmRepository
                .findByDeviceIdAndAlarmTypeAndState(
                        device.getId(),
                        "DEVICE_DOWN",
                        AlarmState.ACTIVE
                )
                .orElseGet(() -> {

                    Alarm alarm = new Alarm();

                    alarm.setDevice(device);
                    alarm.setAlarmType("DEVICE_DOWN");
                    alarm.setMessage(
                            "Device " + device.getName()
                                    + " is unreachable"
                    );
                    alarm.setState(AlarmState.ACTIVE);
                    alarm.setCreatedAt(LocalDateTime.now());

                    return alarmRepository.save(alarm);
                });
    }

    public void clearDeviceDownAlarm(Device device) {

        alarmRepository
                .findByDeviceIdAndAlarmTypeAndState(
                        device.getId(),
                        "DEVICE_DOWN",
                        AlarmState.ACTIVE
                )
                .ifPresent(alarm -> {

                    alarm.setState(AlarmState.CLEARED);
                    alarm.setClearedAt(LocalDateTime.now());

                    alarmRepository.save(alarm);
                });
    }

    public List<Alarm> getAllAlarms() {
        return alarmRepository.findAll();
    }

    public List<Alarm> getActiveAlarms() {
        return alarmRepository.findByState(AlarmState.ACTIVE);
    }
}