package com.adventure.adventureaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeToolsTest {

    @Test
    void getCurrentDateTime() {
        DateTimeTools dateTimeTools = new DateTimeTools();
        String currentDateTime = dateTimeTools.getCurrentDateTime();
        System.out.println(currentDateTime);
    }
}