package core.yaoquan.hanxu.util.type;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;

/**
 * <p><h3>
 *     Exceptionable Type
 * </h3></p>
 * <p>
 *     This method aims to define an exceptionable situation that contains necessary result to operation.
 *     In general cases, query and compilation methods may lead to exception, or otherwise receive a boolean value.
 * </p>
 * <p>
 *     General method that provided by HanXu (Core) Powered Engine will return a value
 *     that contains two situation usual(T)/exception(error, info) for operations.
 *     You can use {@link #isUsual()} or {@link #isExcept()} to determine where it is contains error.
 * </p>
 * <p>
 *     Using {@link #getUsual()} to receive the usual value after checking.
 *     If you wish to handle the situation in different way, you are able to use
 *     {@link #getUsualOrDefault(Object)} and {@link #getUsualOrDo(Supplier)} method.
 * </p>
 * <p>
 *     For handling both case in final step, you can use {@link #matching(Function, BiFunction)} by
 *     the using method of {@code matching(object -> Usual, (error, info) -> Except} and receive a value from your inner return.
 *     Otherwise, handle the case in quick by using {@link #ifUsual(Consumer)} or {@link #ifExcept(Runnable)},
 *     which return void.
 * </p>
 *
 * @param <T> Class of usual value.
 * @since 0.7.1 (Internal Development)
 */
public final class Exceptionable<T> {
    private final Type type;
    private final T value;
    private final Failure failure;

    public enum Type {
        USUAL, EXCEPT,
    }

    public record Failure(String error, String info) {}

    private Exceptionable(Type type, T value, Failure failure) {
        this.type = type;
        this.value = value;
        this.failure = failure;
    }

    @CheckReturnValue
    public static @NotNull <T> Exceptionable<T> usual(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Usual value cannot be null.");
        }
        return new Exceptionable<>(Type.USUAL, value, null);
    }

    @CheckReturnValue
    public static @NotNull <T> Exceptionable<T> exception(@NotNull String error, @Nullable String info) {
        return new Exceptionable<>(Type.EXCEPT, null, new Failure(error, info));
    }

    @CheckReturnValue
    public static @NotNull <T> Exceptionable<T> exception(@NotNull String error) {
        return new Exceptionable<>(Type.EXCEPT, null, new Failure(error, "null"));
    }

    public @NotNull Type situation() {
        return type;
    }

    public boolean isUsual() {
        return type == Type.USUAL;
    }

    public boolean isExcept() {
        return type == Type.EXCEPT;
    }

    public @NotNull T getUsual() throws NoSuchElementException {
        if (isExcept()) {
            throw new NoSuchElementException("Exceptionable value is except: " + failure.error() + ", " + failure.info());
        }
        return value;
    }

    public @NotNull T getUsualOrDefault(T defaultValue) {
        return isUsual()? value : defaultValue;
    }

    public @NotNull T getUsualOrDo(Supplier<? extends T> supplier) {
        return isUsual()? value : supplier.get();
    }

    public @NotNull Failure getFailure() {
        return failure;
    }

    public @NotNull String getError() {
        return failure.error();
    }

    public @NotNull String getFailureInfo() {
        return failure.info();
    }

    public @NotNull Exceptionable<T> ifUsual(Consumer<? super T> consumer) {
        if (isUsual()) {
            consumer.accept(value);
        }
        return this;
    }

    public @NotNull Exceptionable<T> ifExcept(BiConsumer<String, String> consumer) {
        if (isExcept()) {
            consumer.accept(failure.error(), failure.info());
        }
        return this;
    }

    public @NotNull Exceptionable<T> ifExcept(Runnable runnable) {
        if (isExcept()) {
            runnable.run();
        }
        return this;
    }

    public <R> R matching(Function<? super T, ? extends R> usualMapper, BiFunction<String, String, ? extends R> exceptMapper) {
        return isUsual()? usualMapper.apply(value) : exceptMapper.apply(failure.error(), failure.info());
    }

    public @NotNull NullableValue<T> asNullable() {
        return isUsual()? NullableValue.ofNotNull(value) : NullableValue.none();
    }

    @Override
    public String toString() {
        return isUsual()?
                "Exceptionable.Usual(" + value + ")" :
                "Exceptionable.Except(" + failure.error() + ", " + failure.info() + ")";
    }

    @Override
    public int hashCode() {
        return isUsual()? value.hashCode() : failure.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Exceptionable<?> other)) {
            return false;
        }
        if (isUsual() != other.isUsual()) {
            return false;
        }
        if (isUsual()) {
            return value.equals(other.value);
        }
        return Objects.equals(failure, other.failure);
    }
}
