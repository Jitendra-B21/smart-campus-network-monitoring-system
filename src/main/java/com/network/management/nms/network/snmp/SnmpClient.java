package com.network.management.nms.network.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;
@Component
public class SnmpClient {

    public String get(String host, String oid) throws Exception {

        TransportMapping<?> transport =
                new DefaultUdpTransportMapping();

        Snmp snmp = new Snmp(transport);

        transport.listen();

        Address targetAddress =
                GenericAddress.parse("udp:" + host + "/161");

        CommunityTarget<Address> target =
                new CommunityTarget<>();

        target.setCommunity(
                new org.snmp4j.smi.OctetString("public")
        );
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(2);

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        ResponseEvent<Address> response =
                snmp.send(pdu, target);

        snmp.close();

        if (response == null || response.getResponse() == null) {
            throw new RuntimeException(
                    "No SNMP response from " + host
            );
        }

        return response.getResponse()
                .get(0)
                .toString();
    }
    public java.util.List<String> walk(String host, String oid) throws Exception {

        TransportMapping<?> transport =
                new DefaultUdpTransportMapping();

        Snmp snmp = new Snmp(transport);
        transport.listen();

        Address targetAddress =
                GenericAddress.parse("udp:" + host + "/161");

        CommunityTarget<Address> target =
                new CommunityTarget<>();

        target.setCommunity(
                new org.snmp4j.smi.OctetString("public")
        );
        target.setAddress(targetAddress);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(3000);
        target.setRetries(2);

        PDU request = new PDU();
        request.add(new VariableBinding(new OID(oid)));
        request.setType(PDU.GETNEXT);

        java.util.List<String> results = new java.util.ArrayList<>();

        OID rootOid = new OID(oid);

        while (true) {

            ResponseEvent<Address> response =
                    snmp.send(request, target);

            if (response == null || response.getResponse() == null) {
                break;
            }

            PDU responsePdu = response.getResponse();

            if (responsePdu.size() == 0) {
                break;
            }

            VariableBinding vb = responsePdu.get(0);

            if (vb.getOid() == null ||
                    !vb.getOid().startsWith(rootOid)) {
                break;
            }

            results.add(vb.toString());

            request.clear();
            request.add(new VariableBinding(vb.getOid()));
            request.setType(PDU.GETNEXT);
        }

        snmp.close();

        return results;
    }
}