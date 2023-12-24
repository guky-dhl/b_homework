package boku.infra.persistance;

public class EntityExists extends IllegalStateException {
    public <T extends Id> EntityExists(T id) {
        super("Entity with id [%s] already exists".formatted(id));
    }
}
