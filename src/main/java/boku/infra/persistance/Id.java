package boku.infra.persistance;

import java.util.UUID;

public class Id {
    final UUID value = UUID.randomUUID();

    @Override
    public String toString() {
        return value.toString();
    }
}
