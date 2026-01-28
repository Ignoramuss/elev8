package io.elev8.core.discovery;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public final class CachedDiscoveryClient implements DiscoveryClient {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    private final DiscoveryClient delegate;
    private final Duration cacheTtl;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile CachedEntry<APIGroupList> cachedGroups;
    private volatile CachedEntry<List<String>> cachedCoreVersions;
    private final Map<String, CachedEntry<APIResourceList>> cachedResourceLists = new ConcurrentHashMap<>();

    public CachedDiscoveryClient(final DiscoveryClient delegate) {
        this(delegate, DEFAULT_TTL);
    }

    public CachedDiscoveryClient(final DiscoveryClient delegate, final Duration cacheTtl) {
        this.delegate = delegate;
        this.cacheTtl = cacheTtl;
    }

    @Override
    public APIGroupList getServerGroups() throws DiscoveryException {
        lock.readLock().lock();
        try {
            if (cachedGroups != null && !cachedGroups.isExpired(cacheTtl)) {
                log.debug("Returning cached API groups");
                return cachedGroups.getValue();
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (cachedGroups != null && !cachedGroups.isExpired(cacheTtl)) {
                return cachedGroups.getValue();
            }
            log.debug("Fetching and caching API groups");
            final APIGroupList groups = delegate.getServerGroups();
            cachedGroups = new CachedEntry<>(groups);
            return groups;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public APIResourceList getServerResources(final String groupVersion) throws DiscoveryException {
        final CachedEntry<APIResourceList> cached = cachedResourceLists.get(groupVersion);
        if (cached != null && !cached.isExpired(cacheTtl)) {
            log.debug("Returning cached resources for {}", groupVersion);
            return cached.getValue();
        }

        log.debug("Fetching and caching resources for {}", groupVersion);
        final APIResourceList resources = delegate.getServerResources(groupVersion);
        cachedResourceLists.put(groupVersion, new CachedEntry<>(resources));
        return resources;
    }

    @Override
    public List<APIResource> getPreferredResources() throws DiscoveryException {
        return delegate.getPreferredResources();
    }

    @Override
    public Optional<APIResource> findResource(final String kind) throws DiscoveryException {
        return delegate.findResource(kind);
    }

    @Override
    public Optional<APIResource> findResource(final String group, final String version, final String kind)
            throws DiscoveryException {
        return delegate.findResource(group, version, kind);
    }

    @Override
    public boolean isResourceAvailable(final String group, final String version, final String kind)
            throws DiscoveryException {
        return delegate.isResourceAvailable(group, version, kind);
    }

    @Override
    public List<String> getCoreAPIVersions() throws DiscoveryException {
        lock.readLock().lock();
        try {
            if (cachedCoreVersions != null && !cachedCoreVersions.isExpired(cacheTtl)) {
                log.debug("Returning cached core API versions");
                return cachedCoreVersions.getValue();
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (cachedCoreVersions != null && !cachedCoreVersions.isExpired(cacheTtl)) {
                return cachedCoreVersions.getValue();
            }
            log.debug("Fetching and caching core API versions");
            final List<String> versions = delegate.getCoreAPIVersions();
            cachedCoreVersions = new CachedEntry<>(versions);
            return versions;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void invalidateCache() {
        lock.writeLock().lock();
        try {
            log.debug("Invalidating discovery cache");
            cachedGroups = null;
            cachedCoreVersions = null;
            cachedResourceLists.clear();
            delegate.invalidateCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    private static final class CachedEntry<T> {
        private final T value;
        private final Instant cachedAt;

        CachedEntry(final T value) {
            this.value = value;
            this.cachedAt = Instant.now();
        }

        T getValue() {
            return value;
        }

        boolean isExpired(final Duration ttl) {
            return Instant.now().isAfter(cachedAt.plus(ttl));
        }
    }
}
