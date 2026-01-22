package io.elev8.core.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OkHttpClientImplConnectionPoolTest {

    @Test
    void shouldBuildWithoutConnectionPoolConfig() {
        final HttpClient client = OkHttpClientImpl.builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void shouldBuildWithNullConnectionPoolConfig() {
        final HttpClient client = OkHttpClientImpl.builder()
                .connectionPoolConfig(null)
                .build();

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void shouldBuildWithDefaultConnectionPoolConfig() {
        final ConnectionPoolConfig poolConfig = ConnectionPoolConfig.defaults();

        final HttpClient client = OkHttpClientImpl.builder()
                .connectionPoolConfig(poolConfig)
                .build();

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void shouldBuildWithCustomConnectionPoolConfig() {
        final ConnectionPoolConfig poolConfig = ConnectionPoolConfig.builder()
                .maxIdleConnections(10)
                .keepAliveDuration(Duration.ofMinutes(10))
                .build();

        final HttpClient client = OkHttpClientImpl.builder()
                .connectionPoolConfig(poolConfig)
                .build();

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void shouldBuildWithZeroIdleConnections() {
        final ConnectionPoolConfig poolConfig = ConnectionPoolConfig.builder()
                .maxIdleConnections(0)
                .build();

        final HttpClient client = OkHttpClientImpl.builder()
                .connectionPoolConfig(poolConfig)
                .build();

        assertThat(client).isNotNull();
        client.close();
    }

    @Test
    void shouldBuildWithAllConfigurations() {
        final ConnectionPoolConfig poolConfig = ConnectionPoolConfig.builder()
                .maxIdleConnections(15)
                .keepAliveDuration(Duration.ofMinutes(3))
                .build();

        final HttpClient client = OkHttpClientImpl.builder()
                .connectTimeout(Duration.ofSeconds(20))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(15))
                .connectionPoolConfig(poolConfig)
                .skipTlsVerify(false)
                .build();

        assertThat(client).isNotNull();
        client.close();
    }
}
