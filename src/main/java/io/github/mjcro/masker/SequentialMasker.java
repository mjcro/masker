package io.github.mjcro.masker;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class SequentialMasker<T> implements Masker<T, T> {
    private final Masker<? super T, ? extends T>[] maskers;
    private final boolean stopOnFirstReferenceCheck;
    private final boolean stopOnFirstEqualityCheck;

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
