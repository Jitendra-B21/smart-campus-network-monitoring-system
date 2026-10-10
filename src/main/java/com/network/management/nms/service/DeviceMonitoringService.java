package com.network.management.nms.service;

import com.network.management.nms.model.Device;
import com.network.management.nms.model.DeviceStatus;
import com.network.management.nms.model.ManagementProtocol;
import com.network.management.nms.network.snmp.NetworkInterface;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DeviceMonitoringService {

    private final DeviceService deviceService;
    private final AlarmService alarmService;
    private final com.network.management.nms.network.snmp.SnmpService snmpService;
    private final MeterRegistry meterRegistry;

    @Value("${nms.netconf.host}")
    private String netconfHost;

    @Value("${nms.netconf.port}")
    private int netconfPort;

    @Value("${nms.restconf.host}")
    private String restconfHost;

    private final Map<String, AtomicLong> rxMetrics =
            new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> txMetrics =
            new ConcurrentHashMap<>();

    private final Map<Long, String> netconfStatusCache =
            new ConcurrentHashMap<>();

    private final Map<Long, String> restconfStatusCache =
            new ConcurrentHashMap<>();

    // Stores the latest SNMP interface information for every device
    private final Map<Long, List<NetworkInterface>> interfaceCache =
            new ConcurrentHashMap<>();

    private final AtomicLong devicesUp = new AtomicLong(0);
    private final AtomicLong devicesDown = new AtomicLong(0);
    private final AtomicLong activeAlarms = new AtomicLong(0);

    public DeviceMonitoringService(
            DeviceService deviceService,
            AlarmService alarmService,
            com.network.management.nms.network.snmp.SnmpService snmpService,
            MeterRegistry meterRegistry) {

        this.deviceService = deviceService;
        this.alarmService = alarmService;
        this.snmpService = snmpService;
        this.meterRegistry = meterRegistry;

        meterRegistry.gauge("nms_devices_up", devicesUp);
        meterRegistry.gauge("nms_devices_down", devicesDown);
        meterRegistry.gauge("nms_active_alarms", activeAlarms);
    }

    @Scheduled(fixedDelay = 30000)
    public synchronized void monitorDevices() {

        List<Device> devices = deviceService.getAllDevices();

        long upCount = 0;
        long downCount = 0;

        for (Device device : devices) {

            // =========================================================
            // SNMP DEVICE HEALTH
            // =========================================================

            boolean up = snmpService.isDeviceUp(device.getHost());

            if (up) {

                device.setStatus(DeviceStatus.UP);
                upCount++;

                alarmService.clearDeviceDownAlarm(device);

                // =====================================================
                // SNMP INTERFACE MONITORING
                // =====================================================

                try {

                    List<NetworkInterface> interfaces =
                            snmpService.getInterfaceDetails(
                                    device.getHost()
                            );

                    // Store latest interface information
                    interfaceCache.put(
                            device.getId(),
                            List.copyOf(interfaces)
                    );

                    for (NetworkInterface iface : interfaces) {

                        String key =
                                device.getName()
                                        + "_"
                                        + iface.getName();

                        AtomicLong rxValue =
                                rxMetrics.computeIfAbsent(
                                        key,
                                        k -> {

                                            AtomicLong value =
                                                    new AtomicLong();

                                            meterRegistry.gauge(
                                                    "nms_interface_rx_bytes",
                                                    List.of(
                                                            Tag.of(
                                                                    "device",
                                                                    device.getName()
                                                            ),
                                                            Tag.of(
                                                                    "interface",
                                                                    iface.getName()
                                                            )
                                                    ),
                                                    value
                                            );

                                            return value;
                                        }
                                );

                        AtomicLong txValue =
                                txMetrics.computeIfAbsent(
                                        key,
                                        k -> {

                                            AtomicLong value =
                                                    new AtomicLong();

                                            meterRegistry.gauge(
                                                    "nms_interface_tx_bytes",
                                                    List.of(
                                                            Tag.of(
                                                                    "device",
                                                                    device.getName()
                                                            ),
                                                            Tag.of(
                                                                    "interface",
                                                                    iface.getName()
                                                            )
                                                    ),
                                                    value
                                            );

                                            return value;
                                        }
                                );

                        rxValue.set(iface.getRxBytes());
                        txValue.set(iface.getTxBytes());
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Interface monitoring failed for "
                                    + device.getName()
                                    + ": "
                                    + e.getMessage()
                    );

                    // Keep the previous cached data if the latest
                    // SNMP interface collection fails.
                    interfaceCache.putIfAbsent(
                            device.getId(),
                            List.of()
                    );
                }

            } else {

                device.setStatus(DeviceStatus.DOWN);
                downCount++;

                alarmService.generateDeviceDownAlarm(device);

                // Device is unreachable, therefore no current
                // interface information is available.
                interfaceCache.put(
                        device.getId(),
                        List.of()
                );
            }

            // =========================================================
            // NETCONF HEALTH
            // =========================================================

            if (device.getManagementProtocols()
                    .contains(ManagementProtocol.NETCONF)) {

                boolean netconfUp =
                        isPortOpen(
                                netconfHost,
                                netconfPort,
                                5000
                        );

                netconfStatusCache.put(
                        device.getId(),
                        netconfUp ? "UP" : "DOWN"
                );

            } else {

                netconfStatusCache.put(
                        device.getId(),
                        "N/A"
                );
            }

            // =========================================================
            // RESTCONF HEALTH
            // =========================================================

            if (device.getManagementProtocols()
                    .contains(ManagementProtocol.RESTCONF)) {

                boolean restconfUp =
                        isPortOpen(
                                restconfHost,
                                443,
                                5000
                        );

                restconfStatusCache.put(
                        device.getId(),
                        restconfUp ? "UP" : "DOWN"
                );

            } else {

                restconfStatusCache.put(
                        device.getId(),
                        "N/A"
                );
            }

            // =========================================================
            // SAVE DEVICE
            // =========================================================

            deviceService.registerDevice(device);
        }

        devicesUp.set(upCount);
        devicesDown.set(downCount);

        activeAlarms.set(
                alarmService.getActiveAlarms().size()
        );
    }

    // =============================================================
    // Lightweight TCP connectivity check
    // =============================================================

    private boolean isPortOpen(
            String host,
            int port,
            int timeoutMillis) {

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(host, port),
                    timeoutMillis
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "Protocol endpoint unavailable: "
                            + host
                            + ":"
                            + port
            );

            return false;
        }
    }

    // =============================================================
    // Dashboard status accessors
    // =============================================================

    public String getNetconfStatus(Long deviceId) {

        return netconfStatusCache.getOrDefault(
                deviceId,
                "UNKNOWN"
        );
    }

    public String getRestconfStatus(Long deviceId) {

        return restconfStatusCache.getOrDefault(
                deviceId,
                "UNKNOWN"
        );
    }

    // =============================================================
    // Interface data accessor
    // =============================================================

    public List<NetworkInterface> getInterfaces(Long deviceId) {

        return interfaceCache.getOrDefault(
                deviceId,
                List.of()
        );
    }
}