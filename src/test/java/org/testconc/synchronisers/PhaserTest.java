package org.testconc.synchronisers;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Phaser;

import static org.junit.Assert.assertEquals;

public class PhaserTest {

    @Test
    public void test_AdvanceArriveRegister() throws InterruptedException {
        Phaser p = new Phaser(1);
        int phase = 0;

        new Thread(() -> {
            assertEquals(1, p.awaitAdvance(phase));
        }).start();

        assertEquals(0, p.getPhase());
        p.arrive();

        p.register();
        assertEquals(1, p.getPhase());

        new Thread(() -> {
            assertEquals(2, p.awaitAdvance(phase));
        }).start();

        p.arrive();
        p.arrive();
        assertEquals(2, p.getPhase());

        Thread.sleep(500);
    }

    @Test
    public void test_arriveAndDeregister() throws InterruptedException {
        Phaser p = new Phaser(1);

        assertEquals(0, p.arriveAndDeregister());

    }

    @Test
    public void test_PhaseAfterSuccessor() throws InterruptedException {
        Phaser p = new Phaser(2);

        new Thread(() -> {
            while (p.awaitAdvance(1) != 2) {
                assertEquals(2, 2);
            }

        }).start();

        p.arrive();
        p.arrive();

        Thread.sleep(1000);

        p.arrive();
        p.arrive();

        Thread.sleep(1000);
    }

    @Test
    public void test_() throws InterruptedException {
        Phaser p = new Phaser(2);
    }
}
