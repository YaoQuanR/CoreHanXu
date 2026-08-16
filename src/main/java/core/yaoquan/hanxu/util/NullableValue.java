package core.yaoquan.hanxu.util;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p><b>
 *     Nullable Value Type
 * </b></p>
 * <p>
 *     This method aims to define null value declaration in explicitly for handle.
 *     It returns a value that may be null or exact value.
 * </p>
 * <p>
 *     Using {@link #get()} to receive value that ensured it is not null (Or else it throws {@link NoSuchElementException}).
 *     It similar to the behavior of Rust "unwrap".
 * </p>
 * <p>
 *     If you wish to control the null behavior, you are able to use {@link #getOrElse(Object)}, {@link #getOrDo(Supplier)}
 *     for reclaim the value.
 * </p>
 * <p>
 *     If you wish to modify the value if existed, using {@link #modify(Function)} for operations.
 *     Then, take the value by getter.
 * </p>
 * <p>
 *     In case otherwise specific (Such as networking), general method that provided by HanXu (Core) Powered Engine
 *     will use {@link NullableValue} to return value for handle nullable situations.
 * </p>
 *
 * @param <T> Class of the storage value.
 * @since 0.7.0 (Internal Development)
 */
public class NullableValue<T> {
    private final T value;
    private final Type type;

    public enum Type {
        NULL, VALUE,
    }

    private NullableValue(T value, Type type) {
        this.value = value;
        this.type = type;
    }

    private static final NullableValue<?> instances = new NullableValue<>(null, Type.NULL);

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> NullableValue<T> none() {
        return (NullableValue<T>) instances;
    }

    @NotNull
    public static <T> NullableValue<T> ofNotNull(T value) throws NoSuchElementException {
        if (value == null) {
            throw new NoSuchElementException("Nullable value is null.");
        }

        return new NullableValue<>(value, Type.VALUE);
    }

    @NotNull
    public static <T> NullableValue<T> ofNullable(T value) {
        if (value == null) {
            return none();
        }

        return new NullableValue<>(value, Type.VALUE);
    }

    public boolean isPresent() {
        return type == Type.VALUE;
    }

    public boolean isNull() {
        return type == Type.NULL;
    }

    @NotNull
    public T get() throws NoSuchElementException {
        if (isNull()) {
            throw new NoSuchElementException("Nullable value is null.");
        }
        return value;
    }

    public T getOrElse(T defaultValue) {
        return isPresent()? value : defaultValue;
    }

    public T getOrDo(Supplier<? extends T> supplier) {
        return isPresent()? value : supplier.get();
    }

    public <E extends Throwable> T getOrThrow(Supplier<? extends E> exceptionSupplier) throws E {
        if (isNull()) {
            throw exceptionSupplier.get();
        }
        return value;
    }


    public NullableValue<T> getOrOther(NullableValue<? extends T> other) {
        if (isNull()) {
            @SuppressWarnings("unchecked")
            NullableValue<T> result = (NullableValue<T>) other;
            return result;
        }
        return this;
    }

    @NotNull
    public NullableValue.Type situation() {
        return type;
    }

    public <R> NullableValue<R> modify(Function<? super T, ? extends R> mapper) {
        if (isNull()) {
            return none();
        }
        return ofNullable(mapper.apply(value));
    }

    @NotNull
    public NullableValue<T> ifPresent(Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(value);
        }
        return this;
    }

    @NotNull
    public NullableValue<T> ifNull(Runnable runnable) {
        if (isNull()) {
            runnable.run();
        }
        return this;
    }

    public NullableValue<T> passOrDrop(Predicate<? super T> predicate) {
        if (isPresent()) {
            return predicate.test(value)? this : none();
        }
        return this;
    }

    public <R> R matching(Function<? super T, ? extends R> valueMapper, Supplier<? extends R> noneMapper) {
        return isPresent()? valueMapper.apply(value) : noneMapper.get();
    }

    public <R> NullableValue<R> unzipModify(Function<? super T, ? extends NullableValue<? extends R>> valueMapper) {
        if (isNull()) {
            return none();
        }

        @SuppressWarnings("unchecked")
        NullableValue<R> result = (NullableValue<R>) valueMapper.apply(value);
        return result;
    }

    public Stream<T> stream() {
        return isPresent()? Stream.of(value) : Stream.empty();
    }

    @Override
    public String toString() {
        return isNull()? "Nullable.NULL" : "Nullable." + value;
    }

    @Override
    public int hashCode() {
        return isNull()? 0 : value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NullableValue<?> other)) {
            return false;
        }
        if (isNull() != other.isNull()) {
            return false;
        }
        if (isNull()) {
            return true;
        }
        return value.equals(other.value);
    }
}
