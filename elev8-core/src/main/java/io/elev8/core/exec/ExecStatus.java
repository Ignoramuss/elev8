package io.elev8.core.exec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a Kubernetes Status object received on the ERROR channel during exec operations.
 * Used to parse exit codes and error messages from the protocol.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "status": "Success",
 *   "metadata": {},
 *   "details": {
 *     "causes": [
 *       {
 *         "reason": "ExitCode",
 *         "message": "0"
 *       }
 *     ]
 *   }
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecStatus {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String status;
    private Map<String, Object> metadata;
    private Details details;

    /**
     * Details section of the Status object.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Details {
        private List<Cause> causes;
    }

    /**
     * Cause entry containing reason and message.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cause {
        private String reason;
        private String message;
    }

    /**
     * Parses a JSON string into an ExecStatus object.
     *
     * @param json the JSON string to parse
     * @return the parsed ExecStatus
     * @throws Exception if parsing fails
     */
    public static ExecStatus fromJson(final String json) throws Exception {
        return MAPPER.readValue(json, ExecStatus.class);
    }

    /**
     * Extracts the exit code from the Status object.
     * Looks for a cause with reason "ExitCode" and parses its message.
     *
     * @return the exit code, or 0 if not found or parsing fails
     */
    public int getExitCode() {
        if (details == null || details.getCauses() == null) {
            return 0;
        }

        return details.getCauses().stream()
                .filter(cause -> "ExitCode".equals(cause.getReason()))
                .findFirst()
                .map(cause -> {
                    try {
                        return Integer.parseInt(cause.getMessage());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .orElse(0);
    }

    /**
     * Checks if this status represents a successful operation.
     *
     * @return true if status is "Success"
     */
    public boolean isSuccess() {
        return "Success".equals(status);
    }
}
