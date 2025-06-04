package org.testconc.service.executors.forkjoin.countedcompleter;

import java.util.ArrayList;
import java.util.List;

public class MyReducer {

    List<WordMap> reduce(List<WordMap> v1) {
        var result = new ArrayList<WordMap>();
        result.addAll(v1);
        result.forEach(WordMap::sumCount);
        return result;
    }
}
