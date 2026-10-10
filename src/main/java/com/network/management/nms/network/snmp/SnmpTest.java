package com.network.management.nms.network.snmp;

    public class SnmpTest {

        public static void main(String[] args) throws Exception {

            SnmpClient client = new SnmpClient();

            String response = client.get(
                    "172.21.175.109",
                    "1.3.6.1.2.1.1.1.0"
            );

            System.out.println("SNMP Response:");
            System.out.println(response);
        }
    }

