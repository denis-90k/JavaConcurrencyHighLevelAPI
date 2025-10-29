package org.testconc.service.executors.forkjoin.countedcompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMapper {

    public String dummyString = "HELLO"; // for ThreadTest::test_ClassLoader

    public List<WordMap> map(String v) {
        List<WordMap> mappings = new ArrayList<>();
        for (String w : v.split(" ")) {
            mappings.add(new WordMap(w, 1));
        }
        return mappings;
    }

    public List<WordMap> groupWords(List<WordMap> v1, List<WordMap> v2) {
        var intermediaryMap = new HashMap<String, WordMap>();
        List<WordMap> mappings = new ArrayList<>();
        mappings.addAll(v1);
        mappings.addAll(v2);

        mappings.forEach(e -> intermediaryMap.merge(e.getWord(), e, (oldV, newV) -> {
            if (oldV == null) {
                return newV;
            } else {
                newV.getCount().forEach(oldV::addCount);
                return oldV;
            }
        }));

        return new ArrayList<>(intermediaryMap.values());
    }
}
