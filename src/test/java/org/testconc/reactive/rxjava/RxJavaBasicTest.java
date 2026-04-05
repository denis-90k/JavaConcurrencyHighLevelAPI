package org.testconc.reactive.rxjava;

import io.reactivex.rxjava3.core.Observable;
import org.junit.Test;

public class RxJavaBasicTest {

    @Test
    public void test_basicObservable() {
        Observable<Integer> observable = Observable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

        observable.subscribe(
                item -> System.out.println(item),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed"));

    }

    @Test
    public void test_basicOperators() {
        Observable<Integer> observable = Observable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
                .filter(elem -> elem % 2 == 0)
                .map(elem -> elem * elem);

        observable.subscribe(
                item -> System.out.println(item),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed"));
    }
}
