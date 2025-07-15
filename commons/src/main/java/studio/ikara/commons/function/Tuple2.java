package studio.ikara.commons.function;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

@Getter
public class Tuple2<T1, T2> implements Iterable<Object>, Serializable {

    @Serial
    private static final long serialVersionUID = -3518082018884860684L;

    @NonNull final T1 t1;

    @NonNull final T2 t2;

    Tuple2(T1 t1, T2 t2) {
        this.t1 = Objects.requireNonNull(t1, "t1");
        this.t2 = Objects.requireNonNull(t2, "t2");
    }

    public <R> Tuple2<R, T2> mapT1(Function<T1, R> mapper) {
        return new Tuple2<>(mapper.apply(t1), t2);
    }

    public <R> Tuple2<T1, R> mapT2(Function<T2, R> mapper) {
        return new Tuple2<>(t1, mapper.apply(t2));
    }

    @Nullable public Object get(int index) {
        switch (index) {
            case 0:
                return t1;
            case 1:
                return t2;
            default:
                return null;
        }
    }

    public List<Object> toList() {
        return Arrays.asList(toArray());
    }

    public Object[] toArray() {
        return new Object[] {t1, t2};
    }

    @Override
    public Iterator<Object> iterator() {
        return Collections.unmodifiableList(toList()).iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        return t1.equals(tuple2.t1) && t2.equals(tuple2.t2);
    }

    @Override
    public int hashCode() {
        int result = size();
        result = 31 * result + t1.hashCode();
        result = 31 * result + t2.hashCode();
        return result;
    }

    public int size() {
        return 2;
    }

    @Override
    public final String toString() {
        return Tuples.tupleStringRepresentation(toArray())
                .insert(0, '[')
                .append(']')
                .toString();
    }
}
