package org.testconc.service.executors.forkjointask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class CPUbRecursiveTask extends RecursiveTask<List<String>> {

    private List<String> wordsToBeSorted;
    private int low, high;

    public CPUbRecursiveTask(final List<String> words, int lo, int hi) {
        this.wordsToBeSorted = words;
        this.low = lo;
        this.high = hi;
    }

    @Override
    protected List<String> compute() {
        int mid = (low + high) >>> 1;
        if (wordsToBeSorted.size() > 100) {
            invokeAll(List.of(new CPUbRecursiveTask(wordsToBeSorted, low, mid),
                    new CPUbRecursiveTask(wordsToBeSorted, mid, high)));
            mergeHalfs();
        } else {
            String[] array = (String[]) wordsToBeSorted.toArray();
            Arrays.sort(array, (String w1, String w2) -> w1.compareTo(w2));
            for (int i = 0; i <= high; i++) {
                wordsToBeSorted.set(low + i, array[i]);
            }
        }
        return wordsToBeSorted;
    }

    private void mergeHalfs() {
        String[] aux = new String[high - low];
        for (int k = 0; k < aux.length; k++) {
            aux[k] = wordsToBeSorted.get(low + k);
        }
        int a = low;
        int aa = 0;
        int sa = aux.length / 2;
        for (int k = 0; k < aux.length; k++) {
            if (aa >= sa) wordsToBeSorted.set(a++, aux[sa++]);
            else if (sa > high) wordsToBeSorted.set(a++, aux[aa++]);
            else if (aux[aa].compareTo(aux[sa]) < 0) wordsToBeSorted.set(a++, aux[aa++]);
            else wordsToBeSorted.set(a++, aux[sa++]);
        }
    }

}
