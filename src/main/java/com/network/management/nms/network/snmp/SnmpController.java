package com.network.management.nms.network.snmp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snmp")
public class SnmpController {

    private final SnmpService snmpService;

    public SnmpController(SnmpService snmpService) {
        this.snmpService = snmpService;
    }

    @GetMapping("/{host}/system")
    public String getSystemDescription(@PathVariable String host)
            throws Exception {

        return snmpService.getSystemDescription(host);
    }
    @GetMapping("/{host}/interfaces")
    public java.util.List<String> getInterfaces(
            @PathVariable String host) throws Exception {

        return snmpService.getInterfaces(host);
    }
    @GetMapping("/{host}/interfaces/details")
    public java.util.List<NetworkInterface> getInterfaceDetails(
            @PathVariable String host) throws Exception {

        return snmpService.getInterfaceDetails(host);
    }
}