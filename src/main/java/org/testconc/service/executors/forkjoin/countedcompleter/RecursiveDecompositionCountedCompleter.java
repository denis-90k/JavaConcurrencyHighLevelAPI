package org.testconc.service.executors.forkjoin.countedcompleter;

import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

public class RecursiveDecompositionCountedCompleter<E> extends CountedCompleter<E> {

    // Recursive decomposition
    private E[] elements;
    private Consumer<E> action;
    private int low, high;

    RecursiveDecompositionCountedCompleter(RecursiveDecompositionCountedCompleter parent, int lo, int hi, E[] elements, Consumer<E> action) {
        super(parent, 31 - Integer.numberOfLeadingZeros(hi - lo));;
        this.low = lo;
        this.high = hi;
        this.elements = elements;
        this.action = action;
    }

    @Override
    public void compute() {
        for (int n = high - low; n >= 2; n /= 2)
            new RecursiveDecompositionCountedCompleter<E>(this, low + n/2, low + n, elements, action).fork();
        action.accept(elements[low]);
        propagateCompletion();
    }
}
