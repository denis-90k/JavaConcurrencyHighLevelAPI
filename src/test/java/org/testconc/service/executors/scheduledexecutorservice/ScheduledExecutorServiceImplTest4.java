package org.testconc.service.executors.scheduledexecutorservice;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.openjdk.jcstress.annotations.Expect.*;

@JCStressTest
@Outcome(id="5, 5", expect = ACCEPTABLE, desc = "Expected sizes")
@Outcome(       expect = ACCEPTABLE_INTERESTING, desc = "Sizes")
@State
public class ScheduledExecutorServiceImplTest4 {

    ScheduledExecutorServiceImpl scheduledExecutorService = new ScheduledExecutorServiceImpl();

    @Actor
    public void actor1()  {

        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "";
                }
            });
        }
        try {
            scheduledExecutorService.invokeAll(c);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Actor
    public void actor2() {
        List<Callable<String>> c = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            c.add(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "";
                }
            });
        }
        try {
            scheduledExecutorService.invokeAll(c);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Arbiter
    public void result(II_Result r) {
        //r.r1 = scheduledExecutorService.numberOfThreads.get();
        r.r2 = scheduledExecutorService.runnableWorkers.size();
    }
}
