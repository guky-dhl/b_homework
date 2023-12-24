package boku.business.account.user;

import java.math.BigDecimal;

public record Balance(BigDecimal free, BigDecimal frozen) {
    public Balance {
        if (free.compareTo(BigDecimal.ZERO) <= -1 || frozen.compareTo(BigDecimal.ZERO) <= -1) {
            throw new LowBalance(free, frozen);
        }
    }

    public static Balance zero() {
        return new Balance(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Balance add(BigDecimal amount) {
        return new Balance(this.free.add(amount), this.frozen);
    }

    public Balance subtract(BigDecimal amount) {
        return new Balance(free.subtract(amount), this.frozen);
    }

    public Balance freeze(BigDecimal amount) {
        return new Balance(free.subtract(amount), this.frozen.add(amount));
    }

    public Balance unfreeze(BigDecimal amount) {
        return new Balance(free.add(amount), this.frozen.subtract(amount));
    }

    public Balance release(BigDecimal amount) {
        return new Balance(free, this.frozen.subtract(amount));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Balance balance))
            return false;
        return this.free.compareTo(balance.free) == 0 && this.frozen.compareTo(balance.frozen) == 0;
    }

    public static final class LowBalance extends IllegalStateException {
        public LowBalance(BigDecimal free, BigDecimal frozen) {
            super("Balance can't be negative free:[%s] frozen:[%s]".formatted(free, frozen));
        }
    }
}
