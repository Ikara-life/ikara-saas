package studio.ikara.commons.function;

import lombok.Getter;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class Tuple7<T1, T2, T3, T4, T5, T6, T7> extends Tuple6<T1, T2, T3, T4, T5, T6> {

    @Serial
    private static final long serialVersionUID = -8002391247456579281L;

    @NonNull final T7 t7;

    Tuple7(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
        super(t1, t2, t3, t4, t5, t6);
        this.t7 = Objects.requireNonNull(t7, "t7");
    }

    public <R> Tuple7<R, T2, T3, T4, T5, T6, T7> mapT1(Function<T1, R> mapper) {
        return new Tuple7<>(mapper.apply(t1), t2, t3, t4, t5, t6, t7);
    }

    public <R> Tuple7<T1, R, T3, T4, T5, T6, T7> mapT2(Function<T2, R> mapper) {
        return new Tuple7<>(t1, mapper.apply(t2), t3, t4, t5, t6, t7);
    }

    public <R> Tuple7<T1, T2, R, T4, T5, T6, T7> mapT3(Function<T3, R> mapper) {
        return new Tuple7<>(t1, t2, mapper.apply(t3), t4, t5, t6, t7);
    }

    public <R> Tuple7<T1, T2, T3, R, T5, T6, T7> mapT4(Function<T4, R> mapper) {
        return new Tuple7<>(t1, t2, t3, mapper.apply(t4), t5, t6, t7);
    }

    public <R> Tuple7<T1, T2, T3, T4, R, T6, T7> mapT5(Function<T5, R> mapper) {
        return new Tuple7<>(t1, t2, t3, t4, mapper.apply(t5), t6, t7);
    }

    public <R> Tuple7<T1, T2, T3, T4, T5, R, T7> mapT6(Function<T6, R> mapper) {
        return new Tuple7<>(t1, t2, t3, t4, t5, mapper.apply(t6), t7);
    }

    public <R> Tuple7<T1, T2, T3, T4, T5, T6, R> mapT7(Function<T7, R> mapper) {
        return new Tuple7<>(t1, t2, t3, t4, t5, t6, mapper.apply(t7));
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
            case 6:
                return t7;
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {t1, t2, t3, t4, t5, t6, t7};
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof @SuppressWarnings("rawtypes") Tuple7 tuple7)) return false;
        if (!super.equals(o)) return false;

        return t7.equals(tuple7.t7);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + t7.hashCode();
        return result;
    }

    @Override
    public int size() {
        return 7;
    }
}
