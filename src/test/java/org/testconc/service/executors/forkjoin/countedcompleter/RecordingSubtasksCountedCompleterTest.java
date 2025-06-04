package org.testconc.service.executors.forkjoin.countedcompleter;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class RecordingSubtasksCountedCompleterTest {

    @Test
    public void commonPoolTest() {
        String[] lines = new String[]{"complete", "part", "task", "complete task", "be part of", "used part only",
                "as part can be", "can not part", "can be part", "can be used as part"};

        RecordingSubtasksCountedCompleter rscc = new RecordingSubtasksCountedCompleter(null, lines, new MyMapper(), new MyReducer(), 0, lines.length);

        List<WordMap> invokeRes = rscc.invoke();
        Map<String, List<Integer>> result = invokeRes.stream()
                .collect(Collectors.toMap(WordMap::getWord, WordMap::getCount));

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

    }
}
