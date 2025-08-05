package org.testconc.service.executors.forkjoin.countedcompleter;


import java.util.List;
import java.util.concurrent.CountedCompleter;

//Completion traversal loops
public class CompletionTraversalsCountedCompleter extends CountedCompleter<List<WordMap>> {

    final String[] array;
    final MyMapper mapper;
    final MyReducer reducer;
    final int lo, hi;
    RecordingSubtasksCountedCompleter sibling;
    List<WordMap> result;
    CompletionTraversalsCountedCompleter next;
    CompletionTraversalsCountedCompleter forks;

    CompletionTraversalsCountedCompleter(CountedCompleter<List<WordMap>> p, String[] array, MyMapper mapper,
                                         MyReducer reducer, int lo, int hi, CompletionTraversalsCountedCompleter next) {
        super(p);
        this.array = array;
        this.mapper = mapper;
        this.reducer = reducer;
        this.lo = lo;
        this.hi = hi;
        this.next = next;
    }

    @Override
    public void compute() {
        int l = lo, h = hi;
        while (h - l >= 2) {
            int mid = (l + h) >>> 1;
            addToPendingCount(1);
            (forks = new CompletionTraversalsCountedCompleter(this, array, mapper, reducer, mid, h, forks)).fork();
            h = mid;
        }
        if (h > l)
            result = mapper.map(array[lo]);
        // process completions by reducing along and advancing subtask links
        for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
            for (CompletionTraversalsCountedCompleter t = (CompletionTraversalsCountedCompleter)c, s = t.forks; s != null; s = t.forks = s.next)
                t.result = reducer.reduce(mapper.groupWords(t.result, s.result));
        }
    }

    @Override
    public List<WordMap> getRawResult() {
        return result;
    }
}
