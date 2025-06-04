package org.testconc.service.executors.executorservice;

import org.openjdk.jcstress.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

@JCStressTest(Mode.Termination)
@Outcome(id = "TERMINATED", expect = ACCEPTABLE, desc = "Gracefully finished.")
@Outcome(id = "STALE", expect = ACCEPTABLE_INTERESTING, desc = "Test hung up.")
@State
public class ThreadPoolExecutorTest2 {

    volatile ThreadPoolExecutor threadPoolExecutor;

    @Actor
    public void actor2() throws InterruptedException {
        threadPoolExecutor = new ThreadPoolExecutor(20, 20, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        List<Callable<String>> tasks = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            tasks.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "HelloWorld";
                }
            });
        }

        threadPoolExecutor.invokeAll(tasks);
    }

    @Signal
    public void signal() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
