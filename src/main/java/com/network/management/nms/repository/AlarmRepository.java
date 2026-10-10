package com.network.management.nms.repository;

import com.network.management.nms.model.Alarm;
import com.network.management.nms.model.AlarmState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Optional<Alarm> findByDeviceIdAndAlarmTypeAndState(
            Long deviceId,
            String alarmType,
            AlarmState state
    );

    List<Alarm> findByState(AlarmState state);
}