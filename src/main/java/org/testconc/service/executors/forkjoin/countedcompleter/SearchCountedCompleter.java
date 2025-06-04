package org.testconc.service.executors.forkjoin.countedcompleter;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SearchCountedCompleter<E extends SearchCountedCompleter.SearchObject> extends CountedCompleter<E> {


    private E[] elements;
    private Consumer<E> action;
    private int low, high;
    private AtomicReference<E> result;

    public SearchCountedCompleter(SearchCountedCompleter parent, int lo, int hi, E[] elements, AtomicReference<E> result) {
        super(parent);
        this.low = lo;
        this.high = hi;
        this.elements = elements;
        this.result = result;
    }

    @Override
    public void compute() {
        int l = low;
        int h = high;
        while(result.get() == null && h >= l) {
            if(h - l >= 2) {
                int mid = (l + h) >>> 1;
                addToPendingCount(1);
                new SearchCountedCompleter<E>(this, mid, high, elements, result).fork();
                h = mid;
            } else {
                E x = elements[low];
                if(matches(x) && result.compareAndSet(null, x))
                    quietlyCompleteRoot();
                break;
            }
        }
        tryComplete();
    }

    private boolean matches(E x) {
        return x.getName().equals("Hello world");
    }

    public static class SearchObject {

        private String name;

        SearchObject(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }

    }
}
