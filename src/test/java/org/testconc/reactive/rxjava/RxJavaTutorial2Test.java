package org.testconc.reactive.rxjava;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

public class RxJavaTutorial2Test {

    @Test
    public void test_schedulers() throws InterruptedException {

        Observable.just("Long running operation")
                .map(s -> {
                    System.out.println("Processing on thread: " + Thread.currentThread().getName() + " With item: " + s);
                    // Simulate long-running operation
                    Thread.sleep(1000);
                    return s + " completed";
                })
                .subscribeOn(Schedulers.io())           // Specify thread for the source Observable
                .observeOn(Schedulers.computation())    // Specify thread for downstream operations
                .subscribe(
                        result -> System.out.println("Received on thread: " +
                                Thread.currentThread().getName() + ", Result: " + result),
                        Throwable::printStackTrace
                );

        Thread.sleep(3000);
    }
}
