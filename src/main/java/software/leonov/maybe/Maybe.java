/*
 * Copyright (C) 2023 Zhenya Leonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.leonov.maybe;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A immutable container which may or may not hold a {@code nullable} value. This class is the analog of Java's
 * {@link Optional} class with the capability to differentiate between {@code null} values and <i>absent</i> values.
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html" target=
 * "_blank">value-based</a> class; use of identity-sensitive operations (including reference equality, identity hash
 * code, or synchronization) on instances of this class may have unpredictable results and should be avoided.
 *
 * @apiNote The purpose of this class is to allow users to differentiate between a {@code null} value and an
 *          {@link #isPresent() absent} or unspecified value. It is gently encouraged to use this class as a method
 *          return type rather than a parameter. In either case a reference to this class <b>should never itself</b> be
 *          {@code null}.
 * 
 * @author Zhenya Leonov
 */
public class Maybe<T> {

    private static final Maybe<?> ABSENT = new Maybe<>();

    private final T       value;
    private final boolean isPresent;

    private volatile Optional<T> optional;

    private Maybe() {
        value     = null;
        isPresent = false;
    }

    private Maybe(final T value) {
        this.value = value;
        isPresent  = true;
    }

    /**
     * Returns a {@link Maybe} instance which contains no value.
     * 
     * @return a {@link Maybe} instance which contains no value
     */
    @SuppressWarnings("unchecked")
    public static <T> Maybe<T> absent() {
        return (Maybe<T>) ABSENT;
    }

    /**
     * Returns a {@link Maybe} instance containing the result of {@link Map#get(Object) map.get(key)} if such a mapping
     * exists or an {@link #absent() absent} instance otherwise.
     * <p>
     * <b>Discussion:</b> For maps which permit {@code null} values the {@link Map#get(Object)} method returns a
     * {@code null} both when a key is not found in the map, or if the key is explicitly mapped to {@code null}, requiring
     * users to call {@link Map#containsKey(Object) containsKey(Object)} to distinguish between the two cases. This has
     * historically been an overwhelmingly common source of errors.
     * <p>
     * This method is intended to be used as a safer a replacement which always produces deterministic results.
     * <p>
     * For example:
     * 
     * <pre>{@code
     * if (map.containsKey(key)) {
     *     final Object value = map.get(key);
     *     System.out.println(value);
     * }
     * }</pre>
     * 
     * Can be rewritten as:
     * 
     * <pre>{@link
     * Maybe}.{@link #get(Map, Object) get get(map, key)}.{@link #ifPresent(Consumer) ifPresent(System.out::println)};
     * }</pre>
     * 
     * @param <V> the type of values stored in the specified map
     * @param map the specified map
     * @param key the key whose associated value is to be returned
     * @return a {@link Maybe} instance containing the result of {@link Map#get(Object) Map.get(key)} if such a mapping
     *         exists or an {@link #absent() absent} instance otherwise
     * @throws ClassCastException   if the {@code key} is of an inappropriate class type for this map
     * @throws NullPointerException if the {@code key} is {@code null} and this map does not permit {@code null} keys
     */
    public static <V> Maybe<V> get(final Map<?, ? extends V> map, final Object key) {
        requireNonNull(map, "map == null");
        return map.containsKey(key) ? Maybe.of(map.get(key)) : Maybe.absent();
    }

    /**
     * Returns a {@link Maybe} instance which contains the specified possibly {@link #isNull null} value.
     * 
     * @param value the specified value
     * @return a {@link Maybe} instance which contains the specified possibly {@link #isNull null} value
     */
    public static <T> Maybe<T> of(final T value) {
        return new Maybe<>(value);
    }

    /**
     * Returns the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or the {@code defaultValue}
     * otherwise.
     * 
     * @param defaultValue the default value to return if no value is {@link #isPresent() present}
     * @return the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or the {@code defaultValue}
     *         otherwise
     */
    public T orElse(final T defaultValue) {
        return isPresent ? value : defaultValue;
    }

    /**
     * Returns {@code this} {@link Maybe} instance if the value is {@link #isPresent() present} and satisfies the given
     * {@code Predicate} or an {@link #absent() absent} instance otherwise.
     *
     * @param predicate the predicate to apply to the value if is present
     * @return {@code this} {@link Maybe} instnace if the value is {@link #isPresent() present} and satisfies the given
     *         {@code Predicate} or an {@link #absent() absent} instance otherwise
     */
    public Maybe<T> filter(final Predicate<? super T> predicate) {
        requireNonNull(predicate, "predicate == null");
        if (!isPresent)
            return this;
        else
            return predicate.test(value) ? this : absent();
    }

    /**
     * Returns the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or throws a
     * {@code NoSuchElementException} otherwise.
     * 
     * @return the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or throws a
     *         {@code NoSuchElementException} otherwise
     * @throws NoSuchElementException if no value is {@link #isPresent() present}
     */
    public T get() {
        if (isPresent)
            return value;
        throw new NoSuchElementException();
    }

    /**
     * {@link Consumer#accept(Object) Invokes} the specified {@code Consumer} if the value is {@link #isPresent() present}
     * and not {@link #isNull() null}.
     * 
     * @param consumer the specified consumer
     * @return {@code this} {@link Maybe} instance
     */
    public Maybe<T> ifNotNull(final Consumer<? super T> consumer) {
        requireNonNull(consumer, "consumer == null");
        if (value != null)
            consumer.accept(value);
        return this;
    }

    /**
     * Returns {@code this} {@link Maybe} instance if the value is not {@link #isNull() null} or the {@code other} instance
     * otherwise.
     * 
     * @param other the {@link Maybe} instance to return if the value is {@link #isNull() isNull}
     * @return {@code this} {@link Maybe} instance if the value is not {@link #isNull() null} or the {@code other} instance
     *         otherwise
     */
    @SuppressWarnings("unchecked")
    public Maybe<T> ifNull(final Maybe<? extends T> other) {
        requireNonNull(other, "other == null");
        return isNull() ? (Maybe<T>) other : this;
    }

    /**
     * {@link Runnable#run() Invokes} the specified {@code Runnable} if the value is is {@link #isNull() null}.
     * 
     * @param runnable the specified runnable
     * @return {@code this} {@link Maybe} instance
     */
    public Maybe<T> ifNull(final Runnable runnable) {
        requireNonNull(runnable, "runnable == null");
        if (isNull())
            runnable.run();
        return this;
    }

    /**
     * Throws an exception produced by the specified {@code Supplier} if the value is {@link #isNull() null} or returns
     * {@code this} {@link Maybe} instance otherwise.
     * 
     * @param <X>      the type of exception to be thrown
     * @param supplier the exception supplier
     * @return this {@link Maybe} instance if the value is not {@link #isPresent present} or is not {@link #isNull() null}
     * @throws X if the value is {@link #isNull() null}
     */
    public <X extends Throwable> Maybe<T> ifNullThrow(final Supplier<? extends X> supplier) throws X {
        requireNonNull(supplier, "supplier == null");
        if (isNull())
            throw requireNonNull(supplier.get(), "Supplier.get() == null");
        return this;
    }

    /**
     * {@link Consumer#accept(Object) Invokes} the specified {@code Consumer} if the value is {@link #isPresent() present}.
     * 
     * @param consumer the specified consumer
     * @return {@code this} {@link Maybe} instance
     */
    public Maybe<T> ifPresent(final Consumer<? super T> consumer) {
        requireNonNull(consumer, "consumer == null");
        if (isPresent)
            consumer.accept(value);
        return this;
    }

    /**
     * Returns {@code true} if the value is {@link #isPresent() present} and is {@code null} or {@code false} otherwise.
     * 
     * @return {@code true} if the value is {@link #isPresent() present} and is {@code null} or {@code false} otherwise
     */
    public boolean isNull() {
        return isPresent && value == null;
    }

    /**
     * Returns {@code true} if the value is present or {@code false} otherwise.
     * 
     * @return {@code true} if the value is present or {@code false} otherwise
     */
    public boolean isPresent() {
        return isPresent;
    }

    /**
     * Returns a {@link Maybe} instance which contains the result of applying the given {@code Function} to the current
     * value if it is {@link #isPresent() present} or an {@link #absent() absent} instance otherwise.
     *
     * 
     * @param function the mapping function to apply to the value if it is {@link #isPresent() present}
     * @return a {@link Maybe} instance which contains the result of applying the given {@code Function} to the current
     *         value if it is {@link #isPresent() present} or an {@link #absent() absent} instance otherwise
     */
    public <U> Maybe<U> map(final Function<? super T, ? extends U> function) {
        requireNonNull(function, "function == null");
        return isPresent ? Maybe.of(function.apply(value)) : absent();
    }

    /**
     * Returns {@code this} {@link Maybe} instance if the value is {@link #isPresent() present} or the {@code other}
     * instance otherwise.
     * 
     * @param other the {@link Maybe} instance to return if the value is not {@link #isPresent() present}
     * @return {@code this} {@link Maybe} instance if the value is {@link #isPresent() present} or the {@code other}
     *         instance otherwise
     */
    @SuppressWarnings("unchecked")
    public Maybe<T> or(final Maybe<? extends T> other) {
        requireNonNull(other, "other == null");
        return isPresent ? this : (Maybe<T>) other;
    }

    /**
     * {@link Runnable#run() Invokes} the specified {@code Runnable} if the value is not {@link #isPresent() present}.
     * 
     * @param runnable the specified runnable
     * @return {@code this} {@link Maybe} instance
     */
    public Maybe<T> orElse(final Runnable runnable) {
        requireNonNull(runnable, "runnable == null");
        if (!isPresent)
            runnable.run();
        return this;
    }

    /**
     * Returns the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or {@link Supplier#get()}
     * otherwise.
     * 
     * @param supplier the {@code Supplier} whose result is returned if no value is {@link #isPresent() present}
     * @return the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or {@link Supplier#get()}
     *         otherwise
     */
    public T orElse(final Supplier<? extends T> supplier) {
        requireNonNull(supplier, "supplier == null");
        return isPresent ? value : supplier.get();
    }

    /**
     * Returns the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or throws an exception
     * produced by the specified {@link Supplier} otherwise.
     * 
     * @param <X>      the type of exception to be thrown
     * @param supplier the exception supplier
     * @return the possibly {@link #isNull() null} value if it is {@link #isPresent() present}
     * @throws X if the value is not {@link #isPresent() present}
     */
    public <X extends Throwable> T orElseThrow(final Supplier<? extends X> supplier) throws X {
        requireNonNull(supplier, "supplier == null");
        if (isPresent)
            return value;
        else
            throw requireNonNull(supplier.get(), "Supplier.get() == null");
    }

    /**
     * Returns the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or the {@code null}
     * otherwise.
     * 
     * @return the possibly {@link #isNull() null} value if it is {@link #isPresent() present} or the {@code null} otherwise
     */
    public T orNull() {
        return orElse((T) null);
    }

    /**
     * Returns a sequential {@link Stream} containing the value if it is {@link isPresent present} or an
     * {@link Stream#empty() empty} {@code Stream} otherwise.
     *
     * @return a sequential {@link Stream} containing the value if it is {@link isPresent present} or an
     *         {@link Stream#empty() empty} {@code Stream} otherwise
     */
    public Stream<T> stream() {
        return isPresent ? Stream.of(value) : Stream.empty();
    }

    /**
     * Returns this {@link Maybe} as a Java {@link Optional}.
     * <p>
     * <b>Note:</b> A {@link #isNull() null} value will be treated as an {@link Optional#ofNullable(Object) empty} value by
     * Java's {@code Optional}.
     * 
     * @implNote This method uses <a href="https://en.wikipedia.org/wiki/Double-checked_locking" target="_blank">
     *           Double-Checked Locking</a> to lazily return a single {@code Optional} instance and is safe to use in a
     *           concurrent environment.
     * 
     * @return {@code this} {@link Maybe} as a Java {@link Optional}
     */
    public Optional<T> toOptional() {
        Optional<T> ref = optional;
        if (ref == null)
            synchronized (this) {
                ref = optional;
                if (ref == null)
                    optional = ref = Optional.ofNullable(value);
            }
        return ref;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        final Maybe<?> other = (Maybe<?>) obj;

        return isPresent == other.isPresent && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isPresent, value);
    }

    @Override
    public String toString() {
        return String.format("%s[%s", Maybe.class.getSimpleName(), isPresent() ? value + "]" : "]");
    }

}
