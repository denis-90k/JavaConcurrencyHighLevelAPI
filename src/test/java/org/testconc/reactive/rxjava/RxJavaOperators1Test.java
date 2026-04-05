package org.testconc.reactive.rxjava;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class RxJavaOperators1Test {

    @Test
    public void test_map() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.just("Just 1 item", "Just 2 item").map(item -> item + " transformed");

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_flatMap() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.just("Just 1 item", "Just 2 item").flatMap(item -> Observable.just(item + " transformed"));

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> System.out.println(item),             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_switchMap() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.just("Just 1 item", "Just 2 item", "Just 3 item")
                .switchMap(item -> {
                    String[] items = new String[9];
                    for (int t = 0; t < 9; t++)
                        items[t] = item + " transformed" + t;
                    final Observable<String> result = Observable.just(items[0], items[1], items[2], items[3], items[4], items[5], items[6], items[7], items[8]);
                    return result;
                });

        // Subscribe with separate action handlers
        observable.subscribe(
                item -> {
                    if (String.valueOf(item).contains("transformed4"))
                        Thread.sleep(500);
                    System.out.println(item);
                },             // onNext handler
                error -> error.printStackTrace(),             // onError handler
                () -> System.out.println("Completed")         // onComplete handler
        );
    }

    @Test
    public void test_switchMap_second() throws InterruptedException {
        log("main thread " + Thread.currentThread().getName());
        CountDownLatch latch = new CountDownLatch(1);
        var disposable = Observable
                .create(emitter -> {
                    IntStream.range(0, 4)
                            .peek(i -> {
                                log("sleep emit " + Thread.currentThread().getName());
                                sleep(TimeUnit.SECONDS, 1);
                            })
                            .forEach(emitter::onNext);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .switchMap(o ->
                        {
                            log("Thread in switchMap - " + Thread.currentThread().getName());
                            return Observable.create(emitter -> {
                                        IntStream.range(0, 2).forEach(value -> {
                                            log("sleep switch " + Thread.currentThread().getName());
                                            sleep(TimeUnit.MILLISECONDS, 900);
                                            emitter.onNext("original " + o + " | switchMap " + value + " " + Thread.currentThread().getName());
                                        });
                                        emitter.onComplete();
                                    })
                                    .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor(r -> {
                                        Thread thread = new Thread(r);
                                        thread.setName(thread.getName() + " - TET");
                                        thread.setDaemon(true);
                                        return thread;
                                    })));
                        }
                )
                .observeOn(Schedulers.newThread())
                .subscribe(this::log, throwable -> System.out.println(throwable), () -> {
                    log("complete " + Thread.currentThread().getName());
                    latch.countDown();
                });
        boolean await = latch.await(10, TimeUnit.SECONDS);
        assertTrue(await);
        disposable.dispose();
    }

    private void sleep(TimeUnit timeUnit, int timeout) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void log(Object message) {
        System.out.println(message);
    }

    @Test
    public void test_buffer() {
        // Creating and subscribing to a simple Observable
        final Observable<List<String>> observable = Observable.just("Just 1 item", "Just 2 item", "Just 3 item", "Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .buffer(2);

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
    public void test_scan() {
        // Creating and subscribing to a simple Observable
        final Observable<List<String>> observable = Observable.just("Just 1 item", "Just 2 item", "Just 3 item", "Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .scan(new ArrayList<>(), (prev, next) -> {
                    prev.add(next);
                    return prev;
                });

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
    public void test_filter() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable.just("Just 1 item", "Just 2 item", "Just 3 item", "Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .filter(elem -> elem.contains("2"));
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
    public void test_take() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable.just("Just 1 item", "Just 2 item", "Just 3 item", "Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .take(2);
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
    public void test_skip() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable.just("Just 1 item", "Just 2 item", "Just 3 item", "Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .skip(2);
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
    public void test_distinct() {
        // Creating and subscribing to a simple Observable
        final Observable<String> observable = Observable.just("Just 1 item", "Just 1 item", "Just 4 item", "Just 4 item", "Just 5 item", "Just 6 item", "Just 7 item", "Just 8 item")
                .distinct();
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
    public void test_debounce() {
        // Creating and subscribing to a simple Observable
        final Observable<Object> observable = Observable.create(emitter -> {
                    emitter.onNext(0);
                    Thread.sleep(100);
                    emitter.onNext(1);
                    Thread.sleep(100);

                    emitter.onNext(2);
                    Thread.sleep(300);
                    emitter.onNext(3);
                    Thread.sleep(400);
                    emitter.onNext(4);
                    Thread.sleep(400);

                    emitter.onNext(5);
                    Thread.sleep(100);
                    emitter.onNext(6);
                    Thread.sleep(100);
                    emitter.onNext(7);
                    Thread.sleep(100);
                    emitter.onNext(8);
                    Thread.sleep(100);
                    emitter.onNext(9);

                    emitter.onComplete();
                }).debounce(200, TimeUnit.MILLISECONDS);
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
    public void test_() throws InterruptedException {

        final ExecutorService pool = Executors.newFixedThreadPool(10);

        for (int i=0; i<10; i++) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    final long start = System.nanoTime();
                    System.out.println("Hello world");
                    System.out.println(System.nanoTime() - start);
                }
            });

            Thread.sleep(500);
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(16);
                    final long start = System.nanoTime();
                    queue.add("Hello world");
                    System.out.println(System.nanoTime() - start);
                }
            });
            Thread.sleep(5000);
        }

    }

}
