package org.testconc.synchronisers;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Exchanger;

public class ExchangerTest {

    @Test
    public void test_justExchange() throws InterruptedException {
        Exchanger<String> exc = new Exchanger<>();

        new Thread(() -> {
            try {
                String world = exc.exchange("World");
                Assert.assertEquals("Hello", world);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Thread.sleep(1000);
        String hello = exc.exchange("Hello");
        Assert.assertEquals("World", hello);
    }

    @Test
    public void test_() throws InterruptedException {
        Exchanger<String> exc = new Exchanger<>();

       /* new Thread(() -> {
            try {
                String world = exc.exchange("World");
                Assert.assertEquals("Hello", world);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Thread.sleep(1000);*/
        String hello = exc.exchange("Hello");
        Assert.assertEquals("World", hello);
    }
}
