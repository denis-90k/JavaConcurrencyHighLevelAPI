package org.testconc.service.executors;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMain {

    public static void main(String[] args) throws InterruptedException {

        Thread.UncaughtExceptionHandler ueh = (t, e) -> {
            System.out.println("INSIDE UEH");
            if(t instanceof ForkJoinWorkerThread fjwt) {
                System.out.println("In Uncaught Exception");
                e.printStackTrace();
            }
        };

        ForkJoinPool fjp = new ForkJoinPool(15, ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                ueh, true, 15, 15, 1, null, 60, TimeUnit.SECONDS);

        AtomicInteger count = new AtomicInteger();
        class RecursiveActionExc extends RecursiveAction {

            List<String> words;


            RecursiveActionExc(List<String> words) {
                this.words = words;
            }

            @Override
            protected void compute() {
                if (words.size() != 1) {
                    int k = 0;
                    ForkJoinTask[] ts = new ForkJoinTask[words.size()];
                    while (words.size() != k) {
                        ts[k] = new RecursiveActionExc(List.of(words.get(k++))).fork();
                    }
                    System.out.println("Thread name => " + Thread.currentThread().getName());
                    for(int i=0; i<k; i++)
                        ts[i].join();
                    System.out.println("Thread name => " + Thread.currentThread().getName());
                } else {
                    if(count.addAndGet(1) >= 10)
                        throw new IllegalStateException("Wrong state");
                    System.out.println(count.get());
                }
            }
        }

        List<String> data = new ArrayList<>();
        for(int i=0; i<100; i++)
            data.add("");
        RecursiveActionExc task = new RecursiveActionExc(data);
        try {
            fjp.invoke(task);
        } finally {
            Thread.sleep(3000);
        }


        System.out.println("END------------------------------");
    }


}
