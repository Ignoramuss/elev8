package io.elev8.core.exec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecStatusTest {

    @Test
    void shouldParseSuccessStatusWithExitCodeZero() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {},
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "0"
                  }
                ]
              }
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals("Success", status.getStatus());
        assertTrue(status.isSuccess());
        assertEquals(0, status.getExitCode());
    }

    @Test
    void shouldParseFailureStatusWithNonZeroExitCode() throws Exception {
        final String json = """
            {
              "status": "Failure",
              "metadata": {},
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "127"
                  }
                ]
              }
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals("Failure", status.getStatus());
        assertFalse(status.isSuccess());
        assertEquals(127, status.getExitCode());
    }

    @Test
    void shouldHandleMultipleCauses() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {},
              "details": {
                "causes": [
                  {
                    "reason": "OtherReason",
                    "message": "some message"
                  },
                  {
                    "reason": "ExitCode",
                    "message": "42"
                  }
                ]
              }
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals(42, status.getExitCode());
    }

    @Test
    void shouldDefaultToZeroWhenExitCodeNotFound() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {},
              "details": {
                "causes": [
                  {
                    "reason": "OtherReason",
                    "message": "some message"
                  }
                ]
              }
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals(0, status.getExitCode());
    }

    @Test
    void shouldDefaultToZeroWhenDetailsNull() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {}
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals(0, status.getExitCode());
    }

    @Test
    void shouldDefaultToZeroWhenCausesNull() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {},
              "details": {}
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals(0, status.getExitCode());
    }

    @Test
    void shouldHandleInvalidExitCodeNumber() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {},
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "not-a-number"
                  }
                ]
              }
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals(0, status.getExitCode());
    }

    @Test
    void shouldIgnoreUnknownFields() throws Exception {
        final String json = """
            {
              "status": "Success",
              "metadata": {},
              "details": {
                "causes": [
                  {
                    "reason": "ExitCode",
                    "message": "0"
                  }
                ]
              },
              "unknownField": "should be ignored",
              "anotherField": 123
            }
            """;

        final ExecStatus status = ExecStatus.fromJson(json);

        assertEquals("Success", status.getStatus());
        assertEquals(0, status.getExitCode());
    }

    @Test
    void shouldHandleEmptyJson() {
        final String json = "{}";

        assertDoesNotThrow(() -> {
            final ExecStatus status = ExecStatus.fromJson(json);
            assertNotNull(status);
            assertEquals(0, status.getExitCode());
            assertFalse(status.isSuccess());
        });
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        final String json = "not valid json";

        assertThrows(Exception.class, () -> ExecStatus.fromJson(json));
    }
}
