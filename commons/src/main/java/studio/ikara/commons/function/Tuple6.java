package studio.ikara.commons.function;

import lombok.Getter;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class Tuple6<T1, T2, T3, T4, T5, T6> extends Tuple5<T1, T2, T3, T4, T5> {

    @Serial
    private static final long serialVersionUID = 770306356087176830L;

    @NonNull final T6 t6;

    Tuple6(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        super(t1, t2, t3, t4, t5);
        this.t6 = Objects.requireNonNull(t6, "t6");
    }

    public <R> Tuple6<R, T2, T3, T4, T5, T6> mapT1(Function<T1, R> mapper) {
        return new Tuple6<>(mapper.apply(t1), t2, t3, t4, t5, t6);
    }

    public <R> Tuple6<T1, R, T3, T4, T5, T6> mapT2(Function<T2, R> mapper) {
        return new Tuple6<>(t1, mapper.apply(t2), t3, t4, t5, t6);
    }

    public <R> Tuple6<T1, T2, R, T4, T5, T6> mapT3(Function<T3, R> mapper) {
        return new Tuple6<>(t1, t2, mapper.apply(t3), t4, t5, t6);
    }

    public <R> Tuple6<T1, T2, T3, R, T5, T6> mapT4(Function<T4, R> mapper) {
        return new Tuple6<>(t1, t2, t3, mapper.apply(t4), t5, t6);
    }

    public <R> Tuple6<T1, T2, T3, T4, R, T6> mapT5(Function<T5, R> mapper) {
        return new Tuple6<>(t1, t2, t3, t4, mapper.apply(t5), t6);
    }

    public <R> Tuple6<T1, T2, T3, T4, T5, R> mapT6(Function<T6, R> mapper) {
        return new Tuple6<>(t1, t2, t3, t4, t5, mapper.apply(t6));
    }

    @Nullable @Override
    public Object get(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            case 2:
                return t3;
            case 3:
                return t4;
            case 4:
                return t5;
            case 5:
                return t6;
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {t1, t2, t3, t4, t5, t6};
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof @SuppressWarnings("rawtypes") Tuple6 tuple6)) return false;
        if (!super.equals(o)) return false;

        return t6.equals(tuple6.t6);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + t6.hashCode();
        return result;
    }

    @Override
    public int size() {
        return 6;
    }
}
