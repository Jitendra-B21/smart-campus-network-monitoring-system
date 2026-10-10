package com.network.management.nms.network.restconf;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class RestconfClient {

    public String getInterfaces(
            String host,
            int port,
            String username,
            String password) throws Exception {
        System.out.println(
                "RESTCONF username: " + username
                        + ", password configured: "
                        + (password != null && !password.isBlank())
        );

        // Cisco sandbox only:
        // Trust all certificates and disable hostname verification.
        SSLContext sslContext =
                SSLContextBuilder.create()
                        .loadTrustMaterial(
                                null,
                                (certificate, authType) -> true
                        )
                        .build();

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(
                        sslContext,
                        NoopHostnameVerifier.INSTANCE
                );

        PoolingHttpClientConnectionManager connectionManager =
                PoolingHttpClientConnectionManagerBuilder
                        .create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build();

        try (CloseableHttpClient client =
                     HttpClients.custom()
                             .setConnectionManager(connectionManager)
                             .build()) {

            String url =
                    "https://" + host + ":" + port
                            + "/restconf/data/ietf-interfaces:interfaces";

            HttpGet request = new HttpGet(url);

            request.setHeader(
                    "Accept",
                    "application/yang-data+json"
            );

            String credentials =
                    username + ":" + password;

            String encodedCredentials =
                    Base64.getEncoder()
                            .encodeToString(
                                    credentials.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            );

            request.setHeader(
                    "Authorization",
                    "Basic " + encodedCredentials
            );

            System.out.println(
                    "RESTCONF request: GET " + url
            );

            try (CloseableHttpResponse response =
                         client.execute(request)) {

                int statusCode = response.getCode();

                String responseBody =
                        response.getEntity() != null
                                ? EntityUtils.toString(
                                response.getEntity()
                        )
                                : "";

                System.out.println(
                        "RESTCONF HTTP status: "
                                + statusCode
                );

                return responseBody;
            }
        }
    }
}