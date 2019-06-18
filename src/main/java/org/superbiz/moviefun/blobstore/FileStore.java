package org.superbiz.moviefun.blobstore;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class FileStore implements BlobStore {
    private final ConcurrentMap<String, Blob> store = new ConcurrentHashMap<>();

    @Override
    public void put(Blob blob) throws IOException {
        store.put(blob.name, blob);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        return Optional.ofNullable(store.get(name));
    }

    @Override
    public void deleteAll() {
        store.clear();
    }
}