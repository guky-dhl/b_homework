package boku.infra.persistance;

public class Entity<T extends Id> {
    public final T id;

    public Entity(T id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        var e = (Entity<T>) o;
        return this.id.equals(e.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
