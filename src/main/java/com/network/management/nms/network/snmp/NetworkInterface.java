package com.network.management.nms.network.snmp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NetworkInterface {

    private int index;
    private String name;
    private int mtu;
    private long speed;
    private String adminStatus;
    private String operStatus;
    private long rxBytes;
    private long txBytes;
    private long rxPackets;
    private long txPackets;
}