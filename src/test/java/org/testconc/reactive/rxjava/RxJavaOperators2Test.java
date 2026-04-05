package org.testconc.reactive.rxjava;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.schedulers.Timed;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RxJavaOperators2Test {

    @Test
    public void test_merge() throws InterruptedException {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable
                .merge(List.of(Observable.create(emitter -> {
                            emitter.onNext("Just 1 item");
                            Thread.sleep(100);
                            emitter.onNext("Just 2 item");
                            Thread.sleep(200);
                            emitter.onNext("Just 3 item");
                            Thread.sleep(150);
                            emitter.onNext("Just 4 item");
                            Thread.sleep(300);
                            emitter.onComplete();
                        }).subscribeOn(Schedulers.io()),
                        Observable.create(emitter -> {
                            emitter.onNext("Just 5 item");
                            Thread.sleep(50);
                            emitter.onNext("Just 6 item");
                            Thread.sleep(200);
                            emitter.onNext("Just 7 item");
                            Thread.sleep(100);
                            emitter.onNext("Just 8 item");
                            Thread.sleep(300);
                            emitter.onComplete();
                        }).subscribeOn(Schedulers.io())));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

        Thread.sleep(10000);
    }

    @Test
    public void test_concat() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable
                .concat(List.of(Observable.just("Just 1 item", "Just 2 item", "Just 3 item"),
                        Observable.just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_zip() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable
                .zip(List.of(Observable.just("Just 1 item", "Just 2 item", "Just 3 item"),
                        Observable.just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")),
                        (items) -> items[0] + " " + items[1]);

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_combineLatest() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable
                .combineLatest(List.of(Observable.just("Just 1 item", "Just 2 item", "Just 3 item"),
                                Observable.just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")),
                        (items) -> items[0] + " " + items[1]);

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_withLatestFrom() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable.just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .withLatestFrom(List.of(Observable.just("Just 1 item", "Just 2 item", "Just 3 item"),
                                Observable.just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")),
                        (items) -> items[0] + " " + items[1] + " " + items[2]);

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_onErrorReturn() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.create(emitter -> {
            emitter.onNext("Just 4 item");
            emitter.onNext("Just 5 item");
            emitter.onError(new RuntimeException("Something went wrong"));
        })
                .onErrorReturn((error) -> "Error happend");

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_onErrorResumeNext() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.create(emitter -> {
                    emitter.onNext("Just 4 item");
                    emitter.onNext("Just 5 item");
                    emitter.onError(new RuntimeException("Something went wrong"));
                })
                .onErrorResumeNext((error) -> Observable.just("Error happend"));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_retry() {
        AtomicBoolean flag = new AtomicBoolean(true);
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.create(emitter -> {
                    emitter.onNext("Just 4 item");
                    emitter.onNext("Just 5 item");
                    emitter.onError(new RuntimeException("Something went wrong"));
                })
                .retry((error) -> flag.getAndSet(false));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_retryWhen() {
//        AtomicBoolean flag = new AtomicBoolean(true);
//        // Creating and subscribing to a simple Observable
//        final Observable<Object> observable = Observable.create(emitter -> {
//                    emitter.onNext("Just 4 item");
//                    emitter.onNext("Just 5 item");
//                    emitter.onError(new RuntimeException("Something went wrong"));
//                })
//                .retryWhen((error) -> flag.getAndSet(false));
//
//        // Subscribe with separate action handlers
//        observable.subscribe(
//                item -> {
//                    System.out.println(item);
//                },             // onNext handler
//                error -> error.printStackTrace(),             // onError handler
//                () -> System.out.println("Completed")         // onComplete handler
//        );
    }

    @Test
    public void test_delay() throws InterruptedException {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable
                .just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .delay(5, TimeUnit.SECONDS);

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

        Thread.sleep(7000);
    }

    @Test
    public void test_doOnNext() throws InterruptedException {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable
                .just("Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .doOnNext((item)-> System.out.println(item + " TET"));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

    }

    @Test
    public void test_doOnError() throws InterruptedException {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable
                .create(emitter -> {
                    emitter.onNext("Just 4 item");
                    emitter.onNext("Just 5 item");
                    emitter.onError(new RuntimeException("Something went wrong"));
                })
                .doOnError((item)-> System.out.println(item + " TET"));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

    }

    @Test
    public void test_timeout() throws InterruptedException {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable
                .create(emitter -> {
                    emitter.onNext("Just 4 item");
                    Thread.sleep(1200);
                    emitter.onNext("Just 5 item");
                    emitter.onError(new RuntimeException("Something went wrong"));
                })
                .timeout(1, TimeUnit.SECONDS);

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

    }

    @Test
    public void test_timestamp() throws InterruptedException {
        // Creating and subscribing to a simple Observable
        final Observable<Timed<Object>> observable = Observable
                .create(emitter -> {
                    emitter.onNext("Just 4 item");
                    emitter.onNext("Just 5 item");
                    emitter.onComplete();
                })
                .timestamp();

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );

    }

}
