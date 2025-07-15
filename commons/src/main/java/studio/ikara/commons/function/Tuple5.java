package studio.ikara.commons.function;

import lombok.Getter;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class Tuple5<T1, T2, T3, T4, T5> extends Tuple4<T1, T2, T3, T4> {

    @Serial
    private static final long serialVersionUID = 3541548454198133275L;

    @NonNull final T5 t5;

    Tuple5(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        super(t1, t2, t3, t4);
        this.t5 = Objects.requireNonNull(t5, "t5");
    }

    public <R> Tuple5<R, T2, T3, T4, T5> mapT1(Function<T1, R> mapper) {
        return new Tuple5<>(mapper.apply(t1), t2, t3, t4, t5);
    }

    public <R> Tuple5<T1, R, T3, T4, T5> mapT2(Function<T2, R> mapper) {
        return new Tuple5<>(t1, mapper.apply(t2), t3, t4, t5);
    }

    public <R> Tuple5<T1, T2, R, T4, T5> mapT3(Function<T3, R> mapper) {
        return new Tuple5<>(t1, t2, mapper.apply(t3), t4, t5);
    }

    public <R> Tuple5<T1, T2, T3, R, T5> mapT4(Function<T4, R> mapper) {
        return new Tuple5<>(t1, t2, t3, mapper.apply(t4), t5);
    }

    public <R> Tuple5<T1, T2, T3, T4, R> mapT5(Function<T5, R> mapper) {
        return new Tuple5<>(t1, t2, t3, t4, mapper.apply(t5));
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
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {t1, t2, t3, t4, t5};
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof @SuppressWarnings("rawtypes") Tuple5 tuple5)) return false;
        if (!super.equals(o)) return false;

        return t5.equals(tuple5.t5);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + t5.hashCode();
        return result;
    }

    @Override
    public int size() {
        return 5;
    }
}
