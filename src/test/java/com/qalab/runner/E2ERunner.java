package com.qalab.runner;

import com.intuit.karate.junit5.Karate;

class E2ERunner {

    @Karate.Test
    Karate testE2E() {
        return Karate.run("classpath:e2e")
                .tags("@e2e");
    }
}