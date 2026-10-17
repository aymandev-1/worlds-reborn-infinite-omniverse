package com.worldsreborn.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Ordered id -> definition registry. Duplicate ids are rejected so content ids stay stable. */
public final class ContentRegistry<T> {
    private final String name;
    private final Function<T, String> idGetter;
    private final Map<String, T> entries = new LinkedHashMap<>();
    private boolean frozen;

    public ContentRegistry(String name, Function<T, String> idGetter) {
        this.name = name;
        this.idGetter = idGetter;
    }

    public synchronized T register(T entry) {
        if (frozen) {
            throw new IllegalStateException(name + " registry is frozen");
        }
        String id = idGetter.apply(entry);
        if (id == null || !id.contains(":")) {
            throw new IllegalArgumentException(name + " id must be namespaced (namespace:path): " + id);
        }
        if (entries.putIfAbsent(id, entry) != null) {
            throw new IllegalArgumentException("Duplicate " + name + " id: " + id);
        }
        return entry;
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(id == null ? null : entries.get(id));
    }

    public T getOrNull(String id) {
        return id == null ? null : entries.get(id);
    }

    public boolean contains(String id) {
        return id != null && entries.containsKey(id);
    }

    public Collection<T> all() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public void freeze() {
        frozen = true;
    }

    public int size() {
        return entries.size();
    }
}
