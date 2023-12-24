package boku.infra.persistance;

public class EntityNotFound extends IllegalStateException {
    public <T extends Id> EntityNotFound(T id) {
        super("Entity with id [%s] not found".formatted(id));
    }
}