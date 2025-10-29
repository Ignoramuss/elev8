package io.elev8.resources.replicaset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReplicaSetStatusTest {

    @Test
    void shouldCreateEmptyStatus() {
        final ReplicaSetStatus status = new ReplicaSetStatus();

        assertThat(status.getReplicas()).isNull();
        assertThat(status.getReadyReplicas()).isNull();
        assertThat(status.getAvailableReplicas()).isNull();
        assertThat(status.getFullyLabeledReplicas()).isNull();
        assertThat(status.getObservedGeneration()).isNull();
    }

    @Test
    void shouldSetAndGetReplicas() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setReplicas(5);

        assertThat(status.getReplicas()).isEqualTo(5);
    }

    @Test
    void shouldSetAndGetReadyReplicas() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setReadyReplicas(3);

        assertThat(status.getReadyReplicas()).isEqualTo(3);
    }

    @Test
    void shouldSetAndGetAvailableReplicas() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setAvailableReplicas(4);

        assertThat(status.getAvailableReplicas()).isEqualTo(4);
    }

    @Test
    void shouldSetAndGetFullyLabeledReplicas() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setFullyLabeledReplicas(5);

        assertThat(status.getFullyLabeledReplicas()).isEqualTo(5);
    }

    @Test
    void shouldSetAndGetObservedGeneration() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setObservedGeneration(10L);

        assertThat(status.getObservedGeneration()).isEqualTo(10L);
    }

    @Test
    void shouldSetAllFields() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setReplicas(5);
        status.setReadyReplicas(5);
        status.setAvailableReplicas(5);
        status.setFullyLabeledReplicas(5);
        status.setObservedGeneration(3L);

        assertThat(status.getReplicas()).isEqualTo(5);
        assertThat(status.getReadyReplicas()).isEqualTo(5);
        assertThat(status.getAvailableReplicas()).isEqualTo(5);
        assertThat(status.getFullyLabeledReplicas()).isEqualTo(5);
        assertThat(status.getObservedGeneration()).isEqualTo(3L);
    }

    @Test
    void shouldHandlePartialStatus() {
        final ReplicaSetStatus status = new ReplicaSetStatus();
        status.setReplicas(5);
        status.setReadyReplicas(3);

        assertThat(status.getReplicas()).isEqualTo(5);
        assertThat(status.getReadyReplicas()).isEqualTo(3);
        assertThat(status.getAvailableReplicas()).isNull();
        assertThat(status.getFullyLabeledReplicas()).isNull();
        assertThat(status.getObservedGeneration()).isNull();
    }
}
