package core.yaoquan.hanxu.util;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * <p><h3>
 *     Method Result Type
 * </h3></p>
 * <p>
 *     This method aims to define a situation set that contains failed or success.
 * </p>
 * <p>
 *     Using {@link #isSuccess()} or {@link #isFailure()} to determine the situations of running method.
 * </p>
 * <p>
 *     To handle the situation and operate for a result value, you are encouraged to use {@link #matching(Supplier, BiFunction)}
 *     by matching{@code (() -> Success, (error, info) -> Failed)}.
 *     Otherwise, if you wish to handle the case in quick, using {@link #ifSuccess(Runnable)} and {@link #ifFailure(BiConsumer)}
 *     and receive a void return.
 * </p>
 * <p>
 *     In case of non-necessary result that contains two situation only or simple/mod IO methods,
 *     otherwise will use this {@link MethodResult} for result handling.
 * </p>
 *
 * @since 0.7.1 (Internal Development)
 */
public class MethodResult {
    private final Type type;
    private final Failure failure;

    public enum Type {
        SUCCESS, FAILURE,
    }

    public record Failure(String error, String info) {}

    private MethodResult(Type type, Failure failure) {
        this.type = type;
        this.failure = failure;
    }

    @CheckReturnValue
    public static @NotNull MethodResult success() {
        return new MethodResult(Type.SUCCESS, new Failure("success", "null"));
    }

    @CheckReturnValue
    public static @NotNull MethodResult failure(@NotNull String error, @NotNull String info) {
        return new MethodResult(Type.FAILURE, new Failure(error, info));
    }

    @CheckReturnValue
    public static @NotNull MethodResult failure(@NotNull String error) {
        return new MethodResult(Type.FAILURE, new Failure(error, "null"));
    }

    public @NotNull Type situation() {
        return type;
    }

    public boolean isSuccess() {
        return type == Type.SUCCESS;
    }

    public boolean isFailure() {
        return type == Type.FAILURE;
    }

    public @NotNull Failure getFailure() {
        return failure;
    }

    public @NotNull String getError() {
        return failure.error();
    }

    public @NotNull MethodResult ifSuccess(Runnable runnable) {
        if (isSuccess()) {
            runnable.run();
        }
        return this;
    }

    public @NotNull MethodResult ifFailure(BiConsumer<String, String> consumer) {
        if (isFailure()) {
            consumer.accept(failure.error(), failure.info());
        }
        return this;
    }

    public <R> R matching(Supplier<? extends R> successMapper, BiFunction<String, String, ? extends R> failureMapper) {
        return isSuccess()? successMapper.get() : failureMapper.apply(failure.error(), failure.info());
    }

    public @NotNull MethodResult then(Supplier<MethodResult> nextCondition) {
        if (isSuccess()) {
            return nextCondition.get();
        }
        return this;
    }

    @Override
    public String toString() {
        return isSuccess()?
                "MethodResult.Success" :
                "MethodResult.Failure(" + failure.error() + "," + failure.info() + ")";
    }

    @Override
    public int hashCode() {
        return isSuccess()? 0 : Objects.hash(failure.error(), failure.info());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodResult other)) {
            return false;
        }
        if (isSuccess() != other.isSuccess()) {
            return false;
        }
        if (isSuccess()) {
            return true;
        }
        return Objects.equals(failure, other.failure);
    }
}
