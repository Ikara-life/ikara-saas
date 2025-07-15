package studio.ikara.commons.function;

import java.io.Serial;
import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

@Getter
public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {

    @Serial
    private static final long serialVersionUID = -4430274211524723033L;

    @NonNull final T3 t3;

    Tuple3(T1 t1, T2 t2, T3 t3) {
        super(t1, t2);
        this.t3 = Objects.requireNonNull(t3, "t3");
    }

    public <R> Tuple3<R, T2, T3> mapT1(Function<T1, R> mapper) {
        return new Tuple3<>(mapper.apply(t1), t2, t3);
    }

    public <R> Tuple3<T1, R, T3> mapT2(Function<T2, R> mapper) {
        return new Tuple3<>(t1, mapper.apply(t2), t3);
    }

    public <R> Tuple3<T1, T2, R> mapT3(Function<T3, R> mapper) {
        return new Tuple3<>(t1, t2, mapper.apply(t3));
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
            default:
                return null;
        }
    }

    @Override
    public Object[] toArray() {
        return new Object[] {t1, t2, t3};
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof @SuppressWarnings("rawtypes") Tuple3 tuple3)) return false;
        if (!super.equals(o)) return false;

        return t3.equals(tuple3.t3);
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + t3.hashCode();
        return result;
    }
}
