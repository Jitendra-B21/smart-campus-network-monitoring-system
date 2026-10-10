package com.network.management.nms.network.netconf;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelSubsystem;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class NetconfClient {

    private static final String NETCONF_END = "]]>]]>";

    public String getHello(
            String host,
            int port,
            String username,
            String password) throws Exception {

        SshClient client = SshClient.setUpDefaultClient();

        client.start();

        ClientSession session = null;

        try {

            session = client.connect(username, host, port)
                    .verify(Duration.ofSeconds(10))
                    .getSession();

            System.out.println(
                    "NETCONF username: " + username
                            + ", password configured: "
                            + (password != null && !password.isBlank())
            );

            session.addPasswordIdentity(password);

            session.setUserAuthFactories(
                    java.util.List.of(
                            org.apache.sshd.client.auth.password.UserAuthPasswordFactory.INSTANCE
                    )
            );

            session.auth()
                    .verify(Duration.ofSeconds(10));

            try (ChannelSubsystem channel =
                         session.createSubsystemChannel("netconf")) {

                channel.open()
                        .verify(Duration.ofSeconds(10));

                OutputStream output = channel.getInvertedIn();
                InputStream input = channel.getInvertedOut();

                // ---------------------------------------------------------
                // 1. Send NETCONF client hello
                // ---------------------------------------------------------

                String hello =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<hello xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                                + "<capabilities>"
                                + "<capability>"
                                + "urn:ietf:params:netconf:base:1.0"
                                + "</capability>"
                                + "</capabilities>"
                                + "</hello>"
                                + NETCONF_END;

                output.write(
                        hello.getBytes(StandardCharsets.UTF_8)
                );

                output.flush();

                System.out.println("NETCONF client hello sent.");

                // ---------------------------------------------------------
                // 2. Read Cisco server hello
                // ---------------------------------------------------------

                String serverHello =
                        readUntilDelimiter(
                                input,
                                Duration.ofSeconds(15)
                        );

                System.out.println(
                        "NETCONF server hello received."
                );

                // ---------------------------------------------------------
                // 3. Send targeted NETCONF GET
                // ---------------------------------------------------------

                String getInterfaces =
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<rpc message-id=\"101\" "
                                + "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"

                                + "<get>"

                                + "<filter type=\"subtree\">"

                                + "<interfaces "
                                + "xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">"

                                + "</interfaces>"

                                + "</filter>"

                                + "</get>"

                                + "</rpc>"
                                + NETCONF_END;

                output.write(
                        getInterfaces.getBytes(StandardCharsets.UTF_8)
                );

                output.flush();

                System.out.println(
                        "NETCONF interface <get> request sent."
                );

                // ---------------------------------------------------------
                // 4. Read NETCONF RPC response
                // ---------------------------------------------------------

                String rpcResponse =
                        readUntilDelimiter(
                                input,
                                Duration.ofSeconds(30)
                        );

                System.out.println(
                        "NETCONF interface response received."
                );

                // ---------------------------------------------------------
                // 5. Return response
                // ---------------------------------------------------------

                return serverHello + "\n\n" + rpcResponse;
            }

        } finally {

            // IMPORTANT:
            // Close the NETCONF SSH session before stopping the SSH client.
            // This prevents pending asynchronous SSH operations from using
            // an executor that has already been shut down.

            if (session != null) {
                try {
                    session.close(false);
                } catch (Exception e) {
                    System.out.println(
                            "NETCONF session close warning: "
                                    + e.getMessage()
                    );
                }
            }

            try {
                client.stop();
            } catch (Exception e) {
                System.out.println(
                        "NETCONF client stop warning: "
                                + e.getMessage()
                );
            }
        }
    }

    /**
     * Reads a NETCONF 1.0 message until ]]>]]> is received.
     */
    private String readUntilDelimiter(
            InputStream input,
            Duration timeout) throws Exception {

        ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        byte[] delimiter =
                NETCONF_END.getBytes(StandardCharsets.UTF_8);

        int matched = 0;

        long deadline =
                System.nanoTime()
                        + timeout.toNanos();

        while (System.nanoTime() < deadline) {

            if (input.available() == 0) {
                Thread.sleep(25);
                continue;
            }

            int value = input.read();

            if (value == -1) {
                throw new RuntimeException(
                        "NETCONF channel closed before receiving "
                                + "the complete response.\n"
                                + "Received so far:\n"
                                + buffer.toString(
                                StandardCharsets.UTF_8
                        )
                );
            }

            buffer.write(value);

            if (value == delimiter[matched]) {

                matched++;

                if (matched == delimiter.length) {

                    return buffer.toString(
                            StandardCharsets.UTF_8
                    );
                }

            } else {

                matched =
                        (value == delimiter[0])
                                ? 1
                                : 0;
            }
        }

        throw new RuntimeException(
                "NETCONF response timeout after "
                        + timeout.toMillis()
                        + " ms.\n"
                        + "Received so far:\n"
                        + buffer.toString(
                        StandardCharsets.UTF_8
                )
        );
    }
}