package io.elev8.resources.aggregation;

import io.elev8.resources.KubernetesResource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AggregateResultTest {

    @Test
    void getShouldReturnResourcesForPresentType() {
        final KubernetesResource pod = mock(KubernetesResource.class);
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(pod));
        final AggregateResult result = new AggregateResult(map);

        assertThat(result.get(ResourceType.POD)).singleElement().isEqualTo(pod);
    }

    @Test
    void getShouldReturnEmptyListForAbsentType() {
        final AggregateResult result = new AggregateResult(new EnumMap<>(ResourceType.class));

        assertThat(result.get(ResourceType.DEPLOYMENT)).isEmpty();
    }

    @Test
    void getAllShouldReturnFlattenedResources() {
        final KubernetesResource pod = mock(KubernetesResource.class);
        final KubernetesResource svc = mock(KubernetesResource.class);
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(pod));
        map.put(ResourceType.SERVICE, List.of(svc));
        final AggregateResult result = new AggregateResult(map);

        assertThat(result.getAll()).containsExactlyInAnyOrder(pod, svc);
    }

    @Test
    void typesShouldReturnPresentTypes() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(mock(KubernetesResource.class)));
        map.put(ResourceType.DEPLOYMENT, List.of(mock(KubernetesResource.class)));
        final AggregateResult result = new AggregateResult(map);

        assertThat(result.types()).containsExactlyInAnyOrder(ResourceType.POD, ResourceType.DEPLOYMENT);
    }

    @Test
    void sizeShouldReturnTotalCount() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(mock(KubernetesResource.class), mock(KubernetesResource.class)));
        map.put(ResourceType.SERVICE, List.of(mock(KubernetesResource.class)));
        final AggregateResult result = new AggregateResult(map);

        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void sizeWithTypeShouldReturnCountForType() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(mock(KubernetesResource.class), mock(KubernetesResource.class)));
        final AggregateResult result = new AggregateResult(map);

        assertThat(result.size(ResourceType.POD)).isEqualTo(2);
        assertThat(result.size(ResourceType.SERVICE)).isZero();
    }

    @Test
    void isEmptyShouldReturnTrueForNoResources() {
        final AggregateResult result = new AggregateResult(new EnumMap<>(ResourceType.class));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void isEmptyShouldReturnFalseWhenResourcesExist() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(mock(KubernetesResource.class)));
        final AggregateResult result = new AggregateResult(map);

        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void countsShouldDeriveResourceCounts() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(mock(KubernetesResource.class), mock(KubernetesResource.class)));
        map.put(ResourceType.SERVICE, List.of(mock(KubernetesResource.class)));
        final AggregateResult result = new AggregateResult(map);

        final ResourceCounts counts = result.counts();
        assertThat(counts.get(ResourceType.POD)).isEqualTo(2);
        assertThat(counts.get(ResourceType.SERVICE)).isEqualTo(1);
        assertThat(counts.total()).isEqualTo(3);
    }

    @Test
    void shouldBeImmutableAgainstOriginalMapMutation() {
        final List<KubernetesResource> mutableList = new ArrayList<>();
        mutableList.add(mock(KubernetesResource.class));
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, mutableList);
        final AggregateResult result = new AggregateResult(map);

        mutableList.add(mock(KubernetesResource.class));
        map.put(ResourceType.SERVICE, List.of(mock(KubernetesResource.class)));

        assertThat(result.get(ResourceType.POD)).hasSize(1);
        assertThat(result.get(ResourceType.SERVICE)).isEmpty();
    }

    @Test
    void returnedListsShouldBeUnmodifiable() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, new ArrayList<>(List.of(mock(KubernetesResource.class))));
        final AggregateResult result = new AggregateResult(map);

        final List<? extends KubernetesResource> pods = result.get(ResourceType.POD);
        assertThrows(UnsupportedOperationException.class, () -> ((List<KubernetesResource>) pods).add(mock(KubernetesResource.class)));
    }

    @Test
    void getAllShouldReturnUnmodifiableList() {
        final Map<ResourceType, List<? extends KubernetesResource>> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, List.of(mock(KubernetesResource.class)));
        final AggregateResult result = new AggregateResult(map);

        final List<KubernetesResource> all = result.getAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(mock(KubernetesResource.class)));
    }
}
