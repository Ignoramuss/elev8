package io.elev8.core.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
@Slf4j
public final class OkHttpClientImpl implements HttpClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;

    private OkHttpClientImpl(final OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public HttpResponse get(final String url, final Map<String, String> headers) throws HttpException {
        final Request request = buildRequest(url, headers, null, "GET");
        return execute(request);
    }

    @Override
    public HttpResponse post(final String url, final Map<String, String> headers, final String body) throws HttpException {
        final Request request = buildRequest(url, headers, body, "POST");
        return execute(request);
    }

    @Override
    public HttpResponse put(final String url, final Map<String, String> headers, final String body) throws HttpException {
        final Request request = buildRequest(url, headers, body, "PUT");
        return execute(request);
    }

    @Override
    public HttpResponse patch(final String url, final Map<String, String> headers, final String body) throws HttpException {
        final Request request = buildRequest(url, headers, body, "PATCH");
        return execute(request);
    }

    @Override
    public HttpResponse delete(final String url, final Map<String, String> headers) throws HttpException {
        final Request request = buildRequest(url, headers, null, "DELETE");
        return execute(request);
    }

    @Override
    public void stream(final String url, final Map<String, String> headers, final StreamHandler handler)
            throws HttpException {
        final Request request = buildRequest(url, headers, null, "GET");

        try {
            final Call call = okHttpClient.newCall(request);
            final Response response = call.execute();

            if (!response.isSuccessful()) {
                final String errorBody = response.body() != null ? response.body().string() : "";
                response.close();
                throw new HttpException("Watch request failed with status " + response.code() + ": " + errorBody);
            }

            final ResponseBody body = response.body();
            if (body == null) {
                response.close();
                throw new HttpException("Watch request returned empty response body");
            }

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                        if (!line.trim().isEmpty()) {
                            handler.onLine(line);
                        }
                    }
                    handler.onClose();
                } catch (Exception e) {
                    log.error("Error during watch streaming", e);
                    handler.onError(e);
                } finally {
                    response.close();
                }
            }, "watch-stream-thread").start();

        } catch (IOException e) {
            throw new HttpException("Failed to initiate watch stream: " + e.getMessage(), e);
        }
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

    private Request buildRequest(final String url, final Map<String, String> headers, final String body, final String method) {
        final Request.Builder builder = new Request.Builder().url(url);

        if (headers != null) {
            headers.forEach(builder::addHeader);
        }

        final RequestBody requestBody = body != null ? RequestBody.create(body, JSON) : null;
        builder.method(method, requestBody);

        return builder.build();
    }

    private HttpResponse execute(final Request request) throws HttpException {
        try (Response response = okHttpClient.newCall(request).execute()) {
            final int statusCode = response.code();
            final String responseBody = response.body() != null ? response.body().string() : "";

            final Map<String, String> responseHeaders = new HashMap<>();
            response.headers().forEach(pair -> responseHeaders.put(pair.getFirst(), pair.getSecond()));

            return new HttpResponse(statusCode, responseBody, responseHeaders);
        } catch (IOException e) {
            throw new HttpException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private String certificateAuthority;
        private boolean skipTlsVerify = false;

        public Builder connectTimeout(final Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(final Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder writeTimeout(final Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }

        public Builder certificateAuthority(final String certificateAuthority) {
            this.certificateAuthority = certificateAuthority;
            return this;
        }

        public Builder skipTlsVerify(final boolean skipTlsVerify) {
            this.skipTlsVerify = skipTlsVerify;
            return this;
        }

        public OkHttpClientImpl build() {
            final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .writeTimeout(writeTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true);

            if (skipTlsVerify) {
                configureTrustAll(builder);
            } else if (certificateAuthority != null) {
                configureCertificateAuthority(builder, certificateAuthority);
            }

            return new OkHttpClientImpl(builder.build());
        }

        private void configureTrustAll(final OkHttpClient.Builder builder) {
            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {
                            }

                            @Override
                            public void checkServerTrusted(final java.security.cert.X509Certificate[] chain, final String authType) {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure trust-all SSL", e);
            }
        }

        private void configureCertificateAuthority(final OkHttpClient.Builder builder, final String certificateAuthority) {
            try {
                final byte[] certBytes = Base64.getDecoder().decode(certificateAuthority);

                final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                final Certificate certificate = certificateFactory.generateCertificate(
                        new ByteArrayInputStream(certBytes));

                final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", certificate);

                final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                builder.sslSocketFactory(sslContext.getSocketFactory(),
                        (X509TrustManager) trustManagerFactory.getTrustManagers()[0]);
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure certificate authority", e);
            }
        }
    }
}
