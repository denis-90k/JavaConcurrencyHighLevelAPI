package org.testconc.service.executors.forkjoin.countedcompleter;

//import jdk.internal.access.SharedSecrets;
//import jdk.internal.vm.SharedThreadContainer;
import org.junit.Assert;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.Z_Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Collectors;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;


//TODO It doesn't work
@JCStressTest
@Outcome(id = "true", expect = ACCEPTABLE, desc = "Expected MapReduce sizes")
@Outcome(expect = ACCEPTABLE_INTERESTING, desc = "Unexpected Sizes")
@State
public class RecordingSubtasksCountedCompleterStressTest {

    Map<String, List<Integer>> result = null;

    @Actor
    public void actor1() {
        String[] lines = new String[]{"complete", "part", "task", "complete task", "be part of", "used part only",
                "as part can be", "can not part", "can be part", "can be used as part"};

        RecordingSubtasksCountedCompleter rscc = new RecordingSubtasksCountedCompleter(null, lines, new MyMapper(), new MyReducer(), 0, lines.length);

        List<WordMap> invokeRes = rscc.invoke();
        this.result = invokeRes.stream()
                .collect(Collectors.toMap(WordMap::getWord, WordMap::getCount));
    }

    @Actor
    public void actor2() {
        addWorker();
    }

    @Actor
    public void actor3() {
        addWorker();
    }

    @Actor
    public void actor4() {
        addWorker();
    }

    @Actor
    public void actor5() {
        addWorker();
    }

    @Actor
    public void actor6() {
        addWorker();
    }

    @Actor
    public void actor7() {
        addWorker();
    }

    private static void addWorker() {
        ForkJoinPool commonPool = ForkJoinPool.commonPool();
        ForkJoinPool.ForkJoinWorkerThreadFactory factory = commonPool.getFactory();
        ForkJoinWorkerThread newThread = factory.newThread(commonPool);
        //SharedThreadContainer container = SharedThreadContainer.create("ThreadPool-worker-");
        newThread.run();
    }

    @Arbiter
    public void result(Z_Result r) {
        //r.r1 = scheduledExecutorService.numberOfThreads.get();
        try {
            Assert.assertEquals(2, result.get("complete").get(0).intValue());//{"complete":2}
            Assert.assertEquals(7, result.get("part").get(0).intValue());//{"part":7}
            Assert.assertEquals(2, result.get("task").get(0).intValue());//{"task":2}
            Assert.assertEquals(4, result.get("be").get(0).intValue());//{"be":4}
            Assert.assertEquals(1, result.get("of").get(0).intValue());//{"of":1}
            Assert.assertEquals(2, result.get("used").get(0).intValue());//{"used":2}
            Assert.assertEquals(1, result.get("only").get(0).intValue());//{"only":1}
            Assert.assertEquals(2, result.get("as").get(0).intValue());//{"as":2}
            Assert.assertEquals(4, result.get("can").get(0).intValue());//{"can":4}
            Assert.assertEquals(1, result.get("not").get(0).intValue());//{"not":1}
            Assert.assertEquals(1, result.get("not").get(0).intValue());//{"not":1}
            r.r1 = true;
        } catch (AssertionError e) {
            r.r1 = false;
        }
    }

}
