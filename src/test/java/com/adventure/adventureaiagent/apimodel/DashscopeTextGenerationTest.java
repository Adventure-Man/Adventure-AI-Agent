package com.adventure.adventureaiagent.apimodel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SuppressWarnings("unused")
@SpringBootTest
class DashscopeTextGenerationTest {

    @Autowired
    private DashscopeTextGeneration dashscopeTextGeneration;
    @Test
    void callApi() {
        dashscopeTextGeneration.callDashscopeText();
    }

}