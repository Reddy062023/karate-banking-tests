package com.qalab.runner;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import io.qameta.allure.karate.AllureKarate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRunner {

    @Test
    void testAll() {
        Results results = Runner
                .path("classpath:banking")
                .tags("~@ignore")
                .hook(new AllureKarate())
                .outputHtmlReport(true)
                .outputCucumberJson(true)
                .parallel(1);
        assertEquals(0, results.getFailCount(),
                results.getErrorMessages());
    }
}