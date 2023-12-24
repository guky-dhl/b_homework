package boku.infra.persistance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicRepositoryTest {

    final TestEntity test = new TestEntity(new TestId());
    final TestRepository subject = new TestRepository();

    @Test
    public void should_find_user_by_id() {
        subject.save(test);

        var result = subject.find(test.id);

        assertTrue(result.isPresent());
        assertEquals(result.get(), test);
    }

    @Test
    public void should_not_store_same_users_twice() {
        subject.save(test);

        assertThrows(EntityExists.class, () -> subject.save(test));
    }

    @Test
    public void should_throw_on_get_when_not_found() {
        assertThrows(EntityNotFound.class, () -> subject.get(test.id));
    }

    static final class TestId extends Id {
    }

    static final class TestEntity extends Entity<TestId> {

        public TestEntity(TestId id) {
            super(id);
        }
    }

    static final class TestRepository extends BasicRepository<TestId, TestEntity> {
    }

}

