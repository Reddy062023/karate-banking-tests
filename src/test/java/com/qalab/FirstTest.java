package com.qalab;

import com.intuit.karate.junit5.Karate;

class FirstTest {
    @Karate.Test
    Karate testFirst() {
        return Karate.run("first-test")
            .relativeTo(getClass());
    }
}