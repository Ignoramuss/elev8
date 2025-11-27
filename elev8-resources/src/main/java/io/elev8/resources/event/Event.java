package io.elev8.resources.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.AbstractResource;
import io.elev8.resources.Metadata;
import io.elev8.resources.ObjectReference;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Event extends AbstractResource {

    public static final String TYPE_NORMAL = "Normal";
    public static final String TYPE_WARNING = "Warning";

    private String action;
    private Instant eventTime;
    private String note;
    private String reason;
    private ObjectReference regarding;
    private ObjectReference related;
    private String reportingController;
    private String reportingInstance;
    private EventSeries series;
    private String type;

    public Event() {
        super("v1", "Event", null);
    }

    private Event(final Builder builder) {
        super("v1", "Event", builder.metadata);
        this.action = builder.action;
        this.eventTime = builder.eventTime;
        this.note = builder.note;
        this.reason = builder.reason;
        this.regarding = builder.regarding;
        this.related = builder.related;
        this.reportingController = builder.reportingController;
        this.reportingInstance = builder.reportingInstance;
        this.series = builder.series;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Metadata metadata;
        private String action;
        private Instant eventTime;
        private String note;
        private String reason;
        private ObjectReference regarding;
        private ObjectReference related;
        private String reportingController;
        private String reportingInstance;
        private EventSeries series;
        private String type;

        private Builder() {
        }

        public Builder metadata(final Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder name(final String name) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setName(name);
            return this;
        }

        public Builder namespace(final String namespace) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata.setNamespace(namespace);
            return this;
        }

        public Builder label(final String key, final String value) {
            if (this.metadata == null) {
                this.metadata = Metadata.builder().build();
            }
            this.metadata = Metadata.builder()
                    .name(this.metadata.getName())
                    .namespace(this.metadata.getNamespace())
                    .labels(this.metadata.getLabels())
                    .label(key, value)
                    .build();
            return this;
        }

        public Builder action(final String action) {
            this.action = action;
            return this;
        }

        public Builder eventTime(final Instant eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public Builder note(final String note) {
            this.note = note;
            return this;
        }

        public Builder reason(final String reason) {
            this.reason = reason;
            return this;
        }

        public Builder regarding(final ObjectReference regarding) {
            this.regarding = regarding;
            return this;
        }

        public Builder related(final ObjectReference related) {
            this.related = related;
            return this;
        }

        public Builder reportingController(final String reportingController) {
            this.reportingController = reportingController;
            return this;
        }

        public Builder reportingInstance(final String reportingInstance) {
            this.reportingInstance = reportingInstance;
            return this;
        }

        public Builder series(final EventSeries series) {
            this.series = series;
            return this;
        }

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Event build() {
            if (metadata == null || metadata.getName() == null || metadata.getName().isEmpty()) {
                throw new IllegalArgumentException("Event name is required");
            }
            return new Event(this);
        }
    }
}
