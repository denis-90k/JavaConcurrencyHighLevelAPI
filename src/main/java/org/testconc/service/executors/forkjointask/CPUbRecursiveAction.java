package org.testconc.service.executors.forkjointask;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RecursiveAction;

public class CPUbRecursiveAction extends RecursiveAction {

    final List<String> lowerCaseWords;
    private static final BlockingQueue<String> capitalizedWords = new LinkedBlockingQueue<>();

    public CPUbRecursiveAction(List<String> lowerCaseWords) {
        this.lowerCaseWords = lowerCaseWords;
    }


    @Override
    protected void compute() {
        if (lowerCaseWords.size() > 100) {
            invokeAll(List.of(new CPUbRecursiveAction(lowerCaseWords.subList(0, lowerCaseWords.size() / 2)),
                    new CPUbRecursiveAction(lowerCaseWords.subList(lowerCaseWords.size() / 2, lowerCaseWords.size()))));
        } else {
            for (final String word : lowerCaseWords) {
                capitalizedWords.add(word.toUpperCase());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
