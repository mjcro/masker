package io.github.mjcro.masker;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Composite masker that applies a fixed chain of maskers in order and can optionally
 * short-circuit as soon as any one of them modifies the value.
 * Short-circuit detection uses reference inequality and/or {@link Objects#equals}.
 * <p>
 * Leaf maskers participating in the chain must return the very same instance when
 * they do not change the input, otherwise reference-based short-circuit will misfire.
 *
 * @param <T> Type the chain operates on.
 */
public class SequentialMasker<T> implements Masker<T, T> {
    private final Masker<? super T, ? extends T>[] maskers;
    private final boolean stopOnFirstReferenceCheck;
    private final boolean stopOnFirstEqualityCheck;

    /**
     * Constructs new sequential masker.
     *
     * @param stopOnFirstReferenceCheck If {@code true}, the chain stops once a masker
     *                                  returns an instance different from its input by reference.
     * @param stopOnFirstEqualityCheck  If {@code true}, the chain stops once a masker
     *                                  returns a value not {@link Objects#equals equal} to its input.
     * @param maskers                   Non-null maskers to run, in order.
     */
    @SafeVarargs
    public SequentialMasker(
            boolean stopOnFirstReferenceCheck,
            boolean stopOnFirstEqualityCheck,
            @NonNull Masker<? super T, ? extends T> @NonNull ... maskers
    ) {
        this.maskers = Objects.requireNonNull(maskers, "maskers");
        this.stopOnFirstReferenceCheck = stopOnFirstReferenceCheck;
        this.stopOnFirstEqualityCheck = stopOnFirstEqualityCheck;
    }

    @Override
    public @Nullable T applyMasking(@Nullable T in) throws Exception {
        if (maskers.length == 0) {
            return in;
        }

        T out = in;
        for (Masker<? super T, ? extends T> m : maskers) {
            T result = m.applyMasking(out);
            if (stopOnFirstReferenceCheck && result != out) {
                return result;
            }
            if (stopOnFirstEqualityCheck && !Objects.equals(result, out)) {
                return result;
            }
            out = result;
        }
        return out;
    }
}
