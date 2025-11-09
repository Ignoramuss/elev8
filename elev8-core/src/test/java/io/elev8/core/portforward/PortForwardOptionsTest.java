package io.elev8.core.portforward;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortForwardOptionsTest {

    @Test
    void shouldCreateOptionsWithSinglePort() {
        final PortForwardOptions options = PortForwardOptions.of(8080);

        assertNotNull(options);
        assertEquals(1, options.getPortCount());
        assertEquals(8080, options.getPorts()[0]);
        assertTrue(options.hasPort(8080));
        assertNull(options.getContainer());
    }

    @Test
    void shouldCreateOptionsWithMultiplePorts() {
        final PortForwardOptions options = PortForwardOptions.of(8080, 9090, 3000);

        assertNotNull(options);
        assertEquals(3, options.getPortCount());
        assertArrayEquals(new int[]{8080, 9090, 3000}, options.getPorts());
        assertTrue(options.hasPort(8080));
        assertTrue(options.hasPort(9090));
        assertTrue(options.hasPort(3000));
        assertFalse(options.hasPort(8081));
    }

    @Test
    void shouldCreateSinglePortOptions() {
        final PortForwardOptions options = PortForwardOptions.single(3000);

        assertNotNull(options);
        assertEquals(1, options.getPortCount());
        assertEquals(3000, options.getPorts()[0]);
        assertTrue(options.hasPort(3000));
    }

    @Test
    void shouldCreateOptionsWithContainer() {
        final PortForwardOptions options = PortForwardOptions.withContainer(
                new int[]{8080, 9090}, "nginx");

        assertNotNull(options);
        assertEquals(2, options.getPortCount());
        assertEquals("nginx", options.getContainer());
        assertTrue(options.hasPort(8080));
        assertTrue(options.hasPort(9090));
    }

    @Test
    void shouldThrowExceptionWhenNoPortsSpecified() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.of());
    }

    @Test
    void shouldThrowExceptionWhenNullPortsSpecified() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.of((int[]) null));
    }

    @Test
    void shouldThrowExceptionForInvalidPortTooLow() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.of(0));
    }

    @Test
    void shouldThrowExceptionForInvalidPortTooHigh() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.of(65536));
    }

    @Test
    void shouldThrowExceptionForInvalidPortNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.of(-1));
    }

    @Test
    void shouldAcceptMinimumValidPort() {
        final PortForwardOptions options = PortForwardOptions.of(1);

        assertEquals(1, options.getPorts()[0]);
    }

    @Test
    void shouldAcceptMaximumValidPort() {
        final PortForwardOptions options = PortForwardOptions.of(65535);

        assertEquals(65535, options.getPorts()[0]);
    }

    @Test
    void shouldThrowExceptionWhenNullContainerProvided() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.withContainer(new int[]{8080}, null));
    }

    @Test
    void shouldThrowExceptionWhenEmptyContainerProvided() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.withContainer(new int[]{8080}, ""));
    }

    @Test
    void shouldThrowExceptionWhenWhitespaceContainerProvided() {
        assertThrows(IllegalArgumentException.class, () ->
                PortForwardOptions.withContainer(new int[]{8080}, "   "));
    }

    @Test
    void shouldValidateSuccessfully() {
        final PortForwardOptions options = PortForwardOptions.of(8080);

        assertDoesNotThrow(options::validate);
    }

    @Test
    void shouldThrowExceptionOnValidationWhenPortsNull() {
        final PortForwardOptions options = PortForwardOptions.builder()
                .ports(null)
                .build();

        assertThrows(IllegalStateException.class, options::validate);
    }

    @Test
    void shouldThrowExceptionOnValidationWhenPortsEmpty() {
        final PortForwardOptions options = PortForwardOptions.builder()
                .ports(new int[]{})
                .build();

        assertThrows(IllegalStateException.class, options::validate);
    }

    @Test
    void shouldReturnZeroPortCountWhenPortsNull() {
        final PortForwardOptions options = PortForwardOptions.builder()
                .ports(null)
                .build();

        assertEquals(0, options.getPortCount());
    }

    @Test
    void shouldReturnFalseForHasPortWhenPortsNull() {
        final PortForwardOptions options = PortForwardOptions.builder()
                .ports(null)
                .build();

        assertFalse(options.hasPort(8080));
    }

    @Test
    void shouldBuildWithToBuilder() {
        final PortForwardOptions original = PortForwardOptions.of(8080, 9090);

        final PortForwardOptions modified = original.toBuilder()
                .container("nginx")
                .build();

        assertEquals(2, modified.getPortCount());
        assertEquals("nginx", modified.getContainer());
        assertArrayEquals(original.getPorts(), modified.getPorts());
    }

    @Test
    void shouldProduceReadableToString() {
        final PortForwardOptions options = PortForwardOptions.withContainer(
                new int[]{8080, 9090}, "app");

        final String str = options.toString();

        assertTrue(str.contains("8080"));
        assertTrue(str.contains("9090"));
        assertTrue(str.contains("app"));
    }
}
