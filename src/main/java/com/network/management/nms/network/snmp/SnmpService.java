package com.network.management.nms.network.snmp;

import org.springframework.stereotype.Service;

@Service
public class SnmpService {

    private final SnmpClient snmpClient;

    public SnmpService(SnmpClient snmpClient) {
        this.snmpClient = snmpClient;
    }

    public String getSystemDescription(String host) throws Exception {
        return snmpClient.get(
                host,
                "1.3.6.1.2.1.1.1.0"
        );
    }
    public java.util.List<String> getInterfaces(String host)
            throws Exception {

        return snmpClient.walk(
                host,
                "1.3.6.1.2.1.2.2.1"
        );
    }
    public java.util.List<NetworkInterface> getInterfaceDetails(String host)
            throws Exception {

        java.util.List<String> rawData = snmpClient.walk(
                host,
                "1.3.6.1.2.1.2.2.1"
        );

        java.util.Map<Integer, NetworkInterface> interfaces =
                new java.util.LinkedHashMap<>();

        for (String entry : rawData) {

            String[] parts = entry.split(" = ", 2);

            if (parts.length != 2) {
                continue;
            }

            String oid = parts[0];
            String value = parts[1];

            String[] oidParts = oid.split("\\.");

            int column = Integer.parseInt(
                    oidParts[oidParts.length - 2]
            );

            int index = Integer.parseInt(
                    oidParts[oidParts.length - 1]
            );

            NetworkInterface networkInterface =
                    interfaces.computeIfAbsent(
                            index,
                            key -> {
                                NetworkInterface ni =
                                        new NetworkInterface();
                                ni.setIndex(key);
                                return ni;
                            }
                    );

            switch (column) {

                case 2 -> networkInterface.setName(value);

                case 4 -> networkInterface.setMtu(
                        Integer.parseInt(value)
                );

                case 5 -> networkInterface.setSpeed(
                        Long.parseLong(value)
                );

                case 7 -> networkInterface.setAdminStatus(
                        status(value)
                );

                case 8 -> networkInterface.setOperStatus(
                        status(value)
                );

                case 10 -> networkInterface.setRxBytes(
                        Long.parseLong(value)
                );

                case 11 -> networkInterface.setRxPackets(
                        Long.parseLong(value)
                );

                case 16 -> networkInterface.setTxBytes(
                        Long.parseLong(value)
                );

                case 17 -> networkInterface.setTxPackets(
                        Long.parseLong(value)
                );
            }
        }

        return new java.util.ArrayList<>(interfaces.values());
    }
    private String status(String value) {

        return switch (value) {
            case "1" -> "UP";
            case "2" -> "DOWN";
            case "3" -> "TESTING";
            default -> "UNKNOWN";
        };
    }
    public boolean isDeviceUp(String host) {

        try {
            snmpClient.get(
                    host,
                    "1.3.6.1.2.1.1.3.0"
            );

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}