package io.elev8.core.http;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp-based implementation of HttpClient.
 */
public class OkHttpClientImpl implements HttpClient {

    private static final Logger log = LoggerFactory.getLogger(OkHttpClientImpl.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    private OkHttpClientImpl(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public HttpResponse get(String url, Map<String, String> headers) throws HttpException {
        Request request = buildRequest(url, headers, null, "GET");
        return execute(request);
    }

    @Override
    public HttpResponse post(String url, Map<String, String> headers, String body) throws HttpException {
        Request request = buildRequest(url, headers, body, "POST");
        return execute(request);
    }

    @Override
    public HttpResponse put(String url, Map<String, String> headers, String body) throws HttpException {
        Request request = buildRequest(url, headers, body, "PUT");
        return execute(request);
    }

    @Override
    public HttpResponse patch(String url, Map<String, String> headers, String body) throws HttpException {
        Request request = buildRequest(url, headers, body, "PATCH");
        return execute(request);
    }

    @Override
    public HttpResponse delete(String url, Map<String, String> headers) throws HttpException {
        Request request = buildRequest(url, headers, null, "DELETE");
        return execute(request);
    }

    @Override
    public void close() {
        okHttpClient.dispatcher().executorService().shutdown();
        okHttpClient.connectionPool().evictAll();
        if (okHttpClient.cache() != null) {
            try {
                okHttpClient.cache().close();
            } catch (IOException e) {
                log.warn("Failed to close HTTP client cache", e);
            }
        }
    }

    private Request buildRequest(String url, Map<String, String> headers, String body, String method) {
        Request.Builder builder = new Request.Builder().url(url);

        // Add headers
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        // Add body for POST, PUT, PATCH
        RequestBody requestBody = body != null ? RequestBody.create(body, JSON) : null;
        builder.method(method, requestBody);

        return builder.build();
    }

    private HttpResponse execute(Request request) throws HttpException {
        try (Response response = okHttpClient.newCall(request).execute()) {
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            Map<String, String> responseHeaders = new HashMap<>();
            response.headers().forEach(pair -> responseHeaders.put(pair.getFirst(), pair.getSecond()));

            return new HttpResponse(statusCode, responseBody, responseHeaders);
        } catch (IOException e) {
            throw new HttpException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private String certificateAuthority;
        private boolean skipTlsVerify = false;

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder certificateAuthority(String certificateAuthority) {
            this.certificateAuthority = certificateAuthority;
            return this;
        }

        public Builder skipTlsVerify(boolean skipTlsVerify) {
            this.skipTlsVerify = skipTlsVerify;
            return this;
        }

        public OkHttpClientImpl build() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true);

            // Configure TLS
            if (skipTlsVerify) {
                configureTrustAll(builder);
            } else if (certificateAuthority != null) {
                configureCertificateAuthority(builder, certificateAuthority);
            }

            return new OkHttpClientImpl(builder.build());
        }

        private void configureTrustAll(OkHttpClient.Builder builder) {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure trust-all SSL", e);
            }
        }

        private void configureCertificateAuthority(OkHttpClient.Builder builder, String certificateAuthority) {
            try {
                // Decode base64 certificate
                byte[] certBytes = Base64.getDecoder().decode(certificateAuthority);

                // Create certificate from bytes
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                Certificate certificate = certificateFactory.generateCertificate(
                        new ByteArrayInputStream(certBytes));

                // Create KeyStore with certificate
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);

                // Create TrustManager
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                // Create SSLContext
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                builder.sslSocketFactory(sslContext.getSocketFactory(),
                        (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure certificate authority", e);
            }
        }
    }
}
