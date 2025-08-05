package org.testconc.jmm;

import java.util.concurrent.atomic.AtomicInteger;

public class WithMain {

    private static AtomicInteger oneOneCounter = new AtomicInteger(0);
    private static AtomicInteger threadCounter = new AtomicInteger(0);

    private static SomeSampleData[] datas = new SomeSampleData[100_000_000];

    public static void main(String[] args) throws InterruptedException {

        for(int r=0; r<5; r++) {
            for(int i = 0; i < 100_000_000; i++) {
                datas[i] = new SomeSampleData();
            }

            Thread thread1 = new Thread(() -> {
                for(SomeSampleData ssd : datas) {
                    ssd.thread1();
                }
            });
            Thread thread2 = new Thread(() -> {
                for(SomeSampleData ssd : datas) {
                    ssd.thread2();
                }
            });

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            for(SomeSampleData ssd : datas) {
                if(ssd.x == 1 && ssd.y == 1) {
                    oneOneCounter.getAndIncrement();
                }
            }

            System.out.println(ProcessHandle.current().pid() + " The number of (x=1, y=1) outcomes is " + oneOneCounter);
        }

        /*for(int i=0; i < 1000_000_000; i++) {
            if(i % 100_000 == 0)
                System.out.println(i);
            SomeSampleData ssm = new SomeSampleData();


            Thread thread3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        thread1.join();
                        thread2.join();
                        if(ssm.x == 1 && ssm.y == 1) {
                            oneOneCounter.getAndIncrement();
                            System.out.println("Incremented");
                        }
                        threadCounter.set(threadCounter.get()-3);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                }
            });
            threadCounter.addAndGet(3);
            thread1.start();
            thread2.start();
            thread3.start();
        }

        while (threadCounter.get() > 0)
            Thread.yield();

        System.out.println("The number of (x=1, y=1) outcomes is " + oneOneCounter);*/
    }

    static class SomeSampleData {
        int x, y;
        int r1, r2;

        public void thread1() {
            x = r2;
            r1 = 1;
        }

        public void thread2() {
            y = r1;
            r2 = 1;
        }
    }


}

