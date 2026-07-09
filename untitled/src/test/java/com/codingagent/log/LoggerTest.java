package com.codingagent.log;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    @Test void testInfoLog() {
        Logger.setLevel(Logger.Level.INFO);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
        Logger.setOut(ps);
        Logger.info("test message");
        ps.flush();
        String output = baos.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("test message"));
    }

    @Test void testLogLevel() {
        Logger.setLevel(Logger.Level.WARN);
        assertFalse(Logger.isEnabled(Logger.Level.DEBUG));
        assertTrue(Logger.isEnabled(Logger.Level.WARN));
        Logger.setLevel(Logger.Level.INFO);
    }
}