package org.testconc.service.executors.forkjoin.countedcompleter;

import java.util.ArrayList;
import java.util.List;

public class WordMap {
    private String word;
    private List<Integer> count;

    public WordMap(String word, int count) {
        this.word = word;
        this.count = new ArrayList<>();
        this.count.add(count);
    }

    public List<Integer> getCount() {
        return count;
    }

    public void addCount(int count) {
        this.count.add(count);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void sumCount() {
        int sum = count.stream().mapToInt(Integer::intValue).sum();
        this.count = new ArrayList<>();
        this.count.add(sum);
    }
}
