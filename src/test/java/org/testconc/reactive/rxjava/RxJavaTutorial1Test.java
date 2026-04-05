package org.testconc.reactive.rxjava;

import io.reactivex.rxjava3.core.Observable;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RxJavaTutorial1Test {

    @Test
    public void test_createSimpleObservable() {

        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.create(emitter -> {
            System.out.println("From Emitter callback");
            try {
                emitter.onNext("Hello");
                emitter.onNext("World");
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item + " 123"),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_createJustObservable() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.just("Just 1 item", "Just 2 item");

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_createFromIterableObservable() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.fromIterable(List.of("Item1", "Item2"));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_createFromArrayObservable() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.fromArray("Item1", "Item2");

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_createFromCallableObservable() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.fromCallable(() -> {
            System.out.println("Hello from callable");
            return "Item1";
        });

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_createIntervalObservable() throws InterruptedException {
        final Observable<Long> observable = Observable.interval(3, 1, TimeUnit.SECONDS);

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

        Thread.sleep(10000);
    }
}
