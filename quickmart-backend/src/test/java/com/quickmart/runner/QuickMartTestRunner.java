package com.quickmart.runner;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuickMartTestRunner {

    @Test
    public void testAll() {
        Results results = Runner
                .path("src/test/resources/quickmart")
                .outputHtmlReport(true)
                .outputJunitXml(true)
                .outputCucumberJson(true)
                .parallel(1);
        assertEquals(0, results.getFailCount(),
                results.getErrorMessages());
    }
}