package boku.infra.persistance;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BasicRepository<I extends Id, E extends Entity<I>> {
    private final ConcurrentHashMap<I, E> storage = new ConcurrentHashMap<>();

    public Optional<E> find(I id) {
        return Optional.ofNullable(storage.get(id));
    }

    public E get(I id) {
        var e = storage.get(id);
        if (e == null) {
            throw new EntityNotFound(id);
        }
        return e;
    }

    public List<E> all() {
        return storage.values().stream().toList();
    }

    public void save(E e) {
        if (storage.containsKey(e.id)) {
            throw new EntityExists(e.id);
        }
        storage.put(e.id, e);
    }
}
