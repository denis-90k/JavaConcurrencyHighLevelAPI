package org.testconc.service.executors.forkjoin.countedcompleter;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

public class RecursiveDecompositionCountedCompleterTest {

    @Test
    public void commonPoolTest() {
        StringHolder[] strs = new StringHolder[1000];
        RandomStringGenerator rsg = RandomStringGenerator.builder().withinRange('A', 'Z').get();
        for (int i = 0; i < 1000; i++)
            strs[i] = new StringHolder(rsg.generate(10));
        long start = System.nanoTime();
        var rdcc = new RecursiveDecompositionCountedCompleter(null, 0, strs.length, strs,
                (Consumer<StringHolder>) o -> o.setStr(o.getStr().toLowerCase()));
        rdcc.invoke();
        long end = System.nanoTime();

        long convert = TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS);
        System.out.println(convert);
        for (StringHolder s : strs) {
            assertTrue(s.getStr().matches("[a-z]+"));
        }
    }

    public static class StringHolder {
        private String str;

        public StringHolder(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }
}
