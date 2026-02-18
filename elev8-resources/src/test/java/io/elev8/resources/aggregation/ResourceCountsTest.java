package io.elev8.resources.aggregation;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCountsTest {

    @Test
    void getShouldReturnCountForPresentType() {
        final Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, 5);
        final ResourceCounts counts = new ResourceCounts(map);

        assertThat(counts.get(ResourceType.POD)).isEqualTo(5);
    }

    @Test
    void getShouldReturnZeroForAbsentType() {
        final ResourceCounts counts = new ResourceCounts(new EnumMap<>(ResourceType.class));

        assertThat(counts.get(ResourceType.DEPLOYMENT)).isZero();
    }

    @Test
    void totalShouldSumAllCounts() {
        final Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, 3);
        map.put(ResourceType.SERVICE, 2);
        map.put(ResourceType.DEPLOYMENT, 4);
        final ResourceCounts counts = new ResourceCounts(map);

        assertThat(counts.total()).isEqualTo(9);
    }

    @Test
    void totalShouldReturnZeroForEmptyCounts() {
        final ResourceCounts counts = new ResourceCounts(new EnumMap<>(ResourceType.class));

        assertThat(counts.total()).isZero();
    }

    @Test
    void isEmptyShouldReturnTrueForEmptyCounts() {
        final ResourceCounts counts = new ResourceCounts(new EnumMap<>(ResourceType.class));

        assertThat(counts.isEmpty()).isTrue();
    }

    @Test
    void isEmptyShouldReturnFalseForNonEmptyCounts() {
        final Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, 1);
        final ResourceCounts counts = new ResourceCounts(map);

        assertThat(counts.isEmpty()).isFalse();
    }

    @Test
    void typesShouldReturnKeysWithNonZeroCounts() {
        final Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, 3);
        map.put(ResourceType.SERVICE, 1);
        final ResourceCounts counts = new ResourceCounts(map);

        assertThat(counts.types()).containsExactlyInAnyOrder(ResourceType.POD, ResourceType.SERVICE);
    }

    @Test
    void asMapShouldReturnUnmodifiableView() {
        final Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, 2);
        final ResourceCounts counts = new ResourceCounts(map);

        final Map<ResourceType, Integer> view = counts.asMap();
        assertThat(view).containsEntry(ResourceType.POD, 2);
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> view.put(ResourceType.SERVICE, 1));
    }

    @Test
    void shouldBeImmutableAgainstOriginalMapMutation() {
        final Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.POD, 5);
        final ResourceCounts counts = new ResourceCounts(map);

        map.put(ResourceType.POD, 99);

        assertThat(counts.get(ResourceType.POD)).isEqualTo(5);
    }
}
