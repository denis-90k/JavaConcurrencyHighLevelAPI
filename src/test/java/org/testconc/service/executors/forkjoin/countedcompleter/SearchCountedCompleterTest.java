package org.testconc.service.executors.forkjoin.countedcompleter;

import org.apache.commons.text.RandomStringGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class SearchCountedCompleterTest {

    @Test
    public void commonPoolTest() {
        SearchCountedCompleter.SearchObject[] strs = new SearchCountedCompleter.SearchObject[1000];
        RandomStringGenerator rsg = RandomStringGenerator.builder().withinRange('A', 'z').get();
        for (int i = 0; i < 1000; i++)
            strs[i] = new SearchCountedCompleter.SearchObject(rsg.generate(10));
        strs[477] = new SearchCountedCompleter.SearchObject("Hello world");
        var result = new AtomicReference<SearchCountedCompleter.SearchObject>();
        SearchCountedCompleter scc = new SearchCountedCompleter(null, 0, strs.length, strs, result);
        scc.invoke();

        Assert.assertEquals("Hello world", result.get().getName());
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
