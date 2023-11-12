package flocean.module.peeper.fjnuoj.utils;

import java.util.Objects;

public class Pair<A, B> {
    public A A;
    public B B;

    Pair(A a, B b) {
        A = a;
        B = b;
    }

    public static <A, B> Pair<A, B> of(A A, B B) {
        return new Pair<>(A, B);
    }

    public static <A, B> Pair<A, B> copy(Pair<A, B> old) {
        return new Pair<>(old.A, old.B);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        Pair<?, ?> pair;
        if (!(o instanceof Pair<?, ?>)) return false;
        pair = (Pair<?, ?>) o;
        return Objects.equals(A, pair.A) && Objects.equals(B, pair.B);
    }

    @Override
    public int hashCode() {
        return Objects.hash(A, B);
    }
}
