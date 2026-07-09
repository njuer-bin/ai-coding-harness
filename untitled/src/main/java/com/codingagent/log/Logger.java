package com.codingagent.log;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum Level { DEBUG, INFO, WARN, ERROR }
    private static Level currentLevel = Level.INFO;
    private static PrintStream out = System.out;
    private static final String RESET = "[0m";
    private static final String GREEN = "[32m";
    private static final String YELLOW = "[33m";
    private static final String RED = "[31m";
    private static final String CYAN = "[36m";

    public static void setLevel(Level level) { currentLevel = level; }
    public static void setOut(PrintStream stream) { out = stream; }
    public static boolean isEnabled(Level level) { return level.ordinal() >= currentLevel.ordinal(); }

    public static void debug(String msg) { log(Level.DEBUG, CYAN, msg); }
    public static void info(String msg) { log(Level.INFO, GREEN, msg); }
    public static void warn(String msg) { log(Level.WARN, YELLOW, msg); }
    public static void error(String msg) { log(Level.ERROR, RED, msg); }

    private static void log(Level level, String color, String msg) {
        if (!isEnabled(level)) return;
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        out.println(color + time + " [" + level + "] " + msg + RESET);
    }
}