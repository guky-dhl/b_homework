package boku.business.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BalanceTest {

    Balance subject;

    @BeforeEach
    void setup_up() {
        subject = new Balance(BigDecimal.valueOf(10), BigDecimal.valueOf(11));
    }

    @Test
    void should_add_to_free() {
        var subject = this.subject.add(BigDecimal.TEN);

        assertEquals(subject.free(), this.subject.free().add(BigDecimal.TEN));
        assertEquals(subject.frozen(), this.subject.frozen());
    }

    @Test
    void should_subtract_from_free() {
        var subject = this.subject.subtract(BigDecimal.TEN);

        assertEquals(subject.free(), BigDecimal.ZERO);
        assertEquals(subject.frozen(), this.subject.frozen());
    }

    @Test
    void should_on_freeze_transfer_from_free_to_frozen() {
        var subject = this.subject.freeze(BigDecimal.TEN);

        assertEquals(subject.free(), this.subject.free().subtract(BigDecimal.TEN));
        assertEquals(subject.frozen(), this.subject.frozen().add(BigDecimal.TEN));
    }

    @Test
    void should_on_unfreeze_transfer_from_frozen_to_free() {
        var subject = this.subject.unfreeze(BigDecimal.TEN);

        assertEquals(subject.free(), this.subject.free().add(BigDecimal.TEN));
        assertEquals(subject.frozen(), this.subject.frozen().subtract(BigDecimal.TEN));
    }

    @Test
    void should_on_release_reduce_frozen_amount() {
        var subject = this.subject.release(BigDecimal.TEN);

        assertEquals(subject.free(), this.subject.free());
        assertEquals(subject.frozen(), this.subject.frozen().subtract(BigDecimal.TEN));
    }

    @Test
    void should_not_allow_free_drop_below_zero() {
        assertThrows(Balance.NegativeBalance.class, () -> this.subject.subtract(this.subject.free().add(BigDecimal.ONE)));
    }

    @Test
    void should_not_allow_frozen_drop_below_zero() {
        assertThrows(Balance.NegativeBalance.class, () -> this.subject.release(this.subject.frozen().add(BigDecimal.ONE)));
    }

}