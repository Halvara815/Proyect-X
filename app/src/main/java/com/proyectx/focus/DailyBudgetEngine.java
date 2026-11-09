package com.proyectx.focus;

/** Pure assessment for a usage total already obtained with explicit consent. */
public final class DailyBudgetEngine {
    public enum State {
        AVAILABLE,
        EXHAUSTED
    }

    public static final class Assessment {
        private final State state;
        private final long usedMillis;
        private final long remainingMillis;
        private final long limitMillis;

        private Assessment(State state, long usedMillis, long remainingMillis, long limitMillis) {
            this.state = state;
            this.usedMillis = usedMillis;
            this.remainingMillis = remainingMillis;
            this.limitMillis = limitMillis;
        }

        public State state() {
            return state;
        }

        public long usedMillis() {
            return usedMillis;
        }

        public long remainingMillis() {
            return remainingMillis;
        }

        public long limitMillis() {
            return limitMillis;
        }

        public boolean shouldBlock() {
            return state == State.EXHAUSTED;
        }
    }

    private DailyBudgetEngine() {}

    public static Assessment assess(DailyBudget budget, long usedMillis) {
        if (budget == null) throw new NullPointerException("budget");
        if (usedMillis < 0) throw new IllegalArgumentException("El uso no puede ser negativo.");
        long limitMillis = budget.limitMinutes() * 60_000L;
        long remainingMillis = Math.max(0L, limitMillis - usedMillis);
        State state = usedMillis >= limitMillis ? State.EXHAUSTED : State.AVAILABLE;
        return new Assessment(state, usedMillis, remainingMillis, limitMillis);
    }
}
