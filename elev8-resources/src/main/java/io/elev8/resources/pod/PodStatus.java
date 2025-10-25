package io.elev8.resources.pod;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PodStatus {

    private String phase;
    private String reason;
    private String message;
    private String podIP;
    private String hostIP;
    private Instant startTime;
    private List<ContainerStatus> containerStatuses;

    public PodStatus() {
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPodIP() {
        return podIP;
    }

    public void setPodIP(String podIP) {
        this.podIP = podIP;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public List<ContainerStatus> getContainerStatuses() {
        return containerStatuses;
    }

    public void setContainerStatuses(List<ContainerStatus> containerStatuses) {
        this.containerStatuses = containerStatuses;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContainerStatus {
        private String name;
        private Boolean ready;
        private Integer restartCount;
        private String image;
        private String imageID;
        private String containerID;

        public ContainerStatus() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getReady() {
            return ready;
        }

        public void setReady(Boolean ready) {
            this.ready = ready;
        }

        public Integer getRestartCount() {
            return restartCount;
        }

        public void setRestartCount(Integer restartCount) {
            this.restartCount = restartCount;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getImageID() {
            return imageID;
        }

        public void setImageID(String imageID) {
            this.imageID = imageID;
        }

        public String getContainerID() {
            return containerID;
        }

        public void setContainerID(String containerID) {
            this.containerID = containerID;
        }
    }
}
