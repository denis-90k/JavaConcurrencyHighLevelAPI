package org.testconc.service.executors.forkjoin.countedcompleter;

import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

//MapReduce style
public class RecordingSubtasksCountedCompleter extends CountedCompleter<List<WordMap>> {

    final String[] array;
    final MyMapper mapper;
    final MyReducer reducer;
    final int lo, hi;
    RecordingSubtasksCountedCompleter sibling;
    List<WordMap> result;

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    RecordingSubtasksCountedCompleter(CountedCompleter<List<WordMap>> p, String[] array, MyMapper mapper, MyReducer reducer, int lo, int hi) {
        super(p);
        this.array = array;
        this.mapper = mapper;
        this.reducer = reducer;
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    public void compute() {
        if (hi - lo >= 2) {
            int mid = (lo + hi) >>> 1;
            var left = new RecordingSubtasksCountedCompleter(this, array, mapper, reducer, lo, mid);
            var right = new RecordingSubtasksCountedCompleter(this, array, mapper, reducer, mid, hi);
            left.sibling = right;
            right.sibling = left;
            setPendingCount(1);
            right.fork();
            left.compute();
        } else {
            if (hi > lo)
                result = mapper.map(array[lo]);
            tryComplete();
        }
    }


    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (caller != this) {
            RecordingSubtasksCountedCompleter child = (RecordingSubtasksCountedCompleter) caller;
            RecordingSubtasksCountedCompleter sib = child.sibling;
            if (sib == null || sib.result == null)
                result = child.result;
            else
                result = reducer.reduce(mapper.groupWords(child.result, sib.result));
        }
    }

    @Override
    public List<WordMap> getRawResult() {
        return result;
    }
}
