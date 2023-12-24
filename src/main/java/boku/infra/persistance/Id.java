package boku.infra.persistance;

import java.util.UUID;


public class Id {
    final UUID value;

    public Id() {
        value = UUID.randomUUID();
    }

    public Id(UUID id) {
        value = id;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        var id = (Id) o;
        return this.value.equals(id.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
