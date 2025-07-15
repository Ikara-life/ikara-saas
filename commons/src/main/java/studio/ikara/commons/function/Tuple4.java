package studio.ikara.commons.function;

import lombok.Getter;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;

@Getter
public class Tuple4<T1, T2, T3, T4> extends Tuple3<T1, T2, T3> {

    @Serial
    private static final long serialVersionUID = -4898704078143033129L;

    @NonNull final T4 t4;

    Tuple4(T1 t1, T2 t2, T3 t3, T4 t4) {
        super(t1, t2, t3);
        this.t4 = Objects.requireNonNull(t4, "t4");
    }

    public <R> Tuple4<R, T2, T3, T4> mapT1(Function<T1, R> mapper) {
        return new Tuple4<>(mapper.apply(t1), t2, t3, t4);
    }

    public <R> Tuple4<T1, R, T3, T4> mapT2(Function<T2, R> mapper) {
        return new Tuple4<>(t1, mapper.apply(t2), t3, t4);
    }

    public <R> Tuple4<T1, T2, R, T4> mapT3(Function<T3, R> mapper) {
        return new Tuple4<>(t1, t2, mapper.apply(t3), t4);
    }

    public <R> Tuple4<T1, T2, T3, R> mapT4(Function<T4, R> mapper) {
        return new Tuple4<>(t1, t2, t3, mapper.apply(t4));
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
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {t1, t2, t3, t4};
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof @SuppressWarnings("rawtypes") Tuple4 tuple4)) return false;
        if (!super.equals(o)) return false;

        return t4.equals(tuple4.t4);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + t4.hashCode();
        return result;
    }

    @Override
    public int size() {
        return 4;
    }
}
