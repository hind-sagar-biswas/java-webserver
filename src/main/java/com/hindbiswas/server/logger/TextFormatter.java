package com.hindbiswas.server.logger;

public class TextFormatter {
    // Reset
    public static final String RESET = "\u001B[0m";

    // Regular Colors
    public static final String BLACK = "\u001B[0;30m";
    public static final String RED = "\u001B[0;31m";
    public static final String GREEN = "\u001B[0;32m";
    public static final String YELLOW = "\u001B[0;33m";
    public static final String BLUE = "\u001B[0;34m";
    public static final String PURPLE = "\u001B[0;35m";
    public static final String CYAN = "\u001B[0;36m";
    public static final String WHITE = "\u001B[0;37m";

    // Bold
    public static final String BLACK_BOLD = "\u001B[1;30m";
    public static final String RED_BOLD = "\u001B[1;31m";
    public static final String GREEN_BOLD = "\u001B[1;32m";
    public static final String YELLOW_BOLD = "\u001B[1;33m";
    public static final String BLUE_BOLD = "\u001B[1;34m";
    public static final String PURPLE_BOLD = "\u001B[1;35m";
    public static final String CYAN_BOLD = "\u001B[1;36m";
    public static final String WHITE_BOLD = "\u001B[1;37m";

    // Underline
    public static final String BLACK_UNDERLINED = "\u001B[4;30m";
    public static final String RED_UNDERLINED = "\u001B[4;31m";
    public static final String GREEN_UNDERLINED = "\u001B[4;32m";
    public static final String YELLOW_UNDERLINED = "\u001B[4;33m";
    public static final String BLUE_UNDERLINED = "\u001B[4;34m";
    public static final String PURPLE_UNDERLINED = "\u001B[4;35m";
    public static final String CYAN_UNDERLINED = "\u001B[4;36m";
    public static final String WHITE_UNDERLINED = "\u001B[4;37m";

    // Background
    public static final String BLACK_BACKGROUND = "\u001B[40m";
    public static final String RED_BACKGROUND = "\u001B[41m";
    public static final String GREEN_BACKGROUND = "\u001B[42m";
    public static final String YELLOW_BACKGROUND = "\u001B[43m";
    public static final String BLUE_BACKGROUND = "\u001B[44m";
    public static final String PURPLE_BACKGROUND = "\u001B[45m";
    public static final String CYAN_BACKGROUND = "\u001B[46m";
    public static final String WHITE_BACKGROUND = "\u001B[47m";

    // High Intensity (Bright)
    public static final String BLACK_BRIGHT = "\u001B[0;90m";
    public static final String RED_BRIGHT = "\u001B[0;91m";
    public static final String GREEN_BRIGHT = "\u001B[0;92m";
    public static final String YELLOW_BRIGHT = "\u001B[0;93m";
    public static final String BLUE_BRIGHT = "\u001B[0;94m";
    public static final String PURPLE_BRIGHT = "\u001B[0;95m";
    public static final String CYAN_BRIGHT = "\u001B[0;96m";
    public static final String WHITE_BRIGHT = "\u001B[0;97m";

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\u001B[1;90m";
    public static final String RED_BOLD_BRIGHT = "\u001B[1;91m";
    public static final String GREEN_BOLD_BRIGHT = "\u001B[1;92m";
    public static final String YELLOW_BOLD_BRIGHT = "\u001B[1;93m";
    public static final String BLUE_BOLD_BRIGHT = "\u001B[1;94m";
    public static final String PURPLE_BOLD_BRIGHT = "\u001B[1;95m";
    public static final String CYAN_BOLD_BRIGHT = "\u001B[1;96m";
    public static final String WHITE_BOLD_BRIGHT = "\u001B[1;97m";

    // High Intensity Backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\u001B[0;100m";
    public static final String RED_BACKGROUND_BRIGHT = "\u001B[0;101m";
    public static final String GREEN_BACKGROUND_BRIGHT = "\u001B[0;102m";
    public static final String YELLOW_BACKGROUND_BRIGHT = "\u001B[0;103m";
    public static final String BLUE_BACKGROUND_BRIGHT = "\u001B[0;104m";
    public static final String PURPLE_BACKGROUND_BRIGHT = "\u001B[0;105m";
    public static final String CYAN_BACKGROUND_BRIGHT = "\u001B[0;106m";
    public static final String WHITE_BACKGROUND_BRIGHT = "\u001B[0;107m";

    private TextFormatter() {
        // Prevent instantiation
    }

    public static String label(String text, Color color) {
        String colorValue = switch (color) {
            case BLACK -> BLACK_BACKGROUND;
            case RED -> RED_BACKGROUND;
            case GREEN -> GREEN_BACKGROUND + BLACK_BOLD;
            case YELLOW -> YELLOW_BACKGROUND + BLACK_BOLD;
            case BLUE -> BLUE_BACKGROUND;
            case PURPLE -> PURPLE_BACKGROUND;
            case CYAN -> CYAN_BACKGROUND + BLACK_BOLD;
            case WHITE -> WHITE + BLACK_BOLD;
            case RESET -> RESET;
        };
        return colorValue + text + RESET;
    }

    public static String text(String text, Color color) {
        String colorValue = switch (color) {
            case BLACK -> BLACK;
            case RED -> RED;
            case GREEN -> GREEN;
            case YELLOW -> YELLOW;
            case BLUE -> BLUE;
            case PURPLE -> PURPLE;
            case CYAN -> CYAN;
            case WHITE -> WHITE;
            case RESET -> RESET;
        };
        return colorValue + text + RESET;
    }

    public static String bold(String text, Color color) {
        String colorValue = switch (color) {
            case BLACK -> BLACK_BOLD;
            case RED -> RED_BOLD;
            case GREEN -> GREEN_BOLD;
            case YELLOW -> YELLOW_BOLD;
            case BLUE -> BLUE_BOLD;
            case PURPLE -> PURPLE_BOLD;
            case CYAN -> CYAN_BOLD;
            case WHITE -> WHITE_BOLD;
            case RESET -> RESET;
        };
        return colorValue + text + RESET;
    }

    public static String underline(String text, Color color) {
        String colorValue = switch (color) {
            case BLACK -> BLACK_UNDERLINED;
            case RED -> RED_UNDERLINED;
            case GREEN -> GREEN_UNDERLINED;
            case YELLOW -> YELLOW_UNDERLINED;
            case BLUE -> BLUE_UNDERLINED;
            case PURPLE -> PURPLE_UNDERLINED;
            case CYAN -> CYAN_UNDERLINED;
            case WHITE -> WHITE_UNDERLINED;
            case RESET -> RESET;
        };
        return colorValue + text + RESET;
    }

    public static String redLabel(String text) {
        return label(text, Color.RED);
    }

    public static String yellowLabel(String text) {
        return label(text, Color.YELLOW);
    }

    public static String greenLabel(String text) {
        return label(text, Color.GREEN);
    }

    public static String blueLabel(String text) {
        return label(text, Color.BLUE);
    }

    public static String cyanLabel(String text) {
        return label(text, Color.CYAN);
    }

    public static String whiteLabel(String text) {
        return label(text, Color.WHITE);
    }

    public static String red(String text) {
        return bold(text, Color.RED);
    }

    public static String yellow(String text) {
        return bold(text, Color.YELLOW);
    }

    public static String green(String text) {
        return bold(text, Color.GREEN);
    }

    public static String blue(String text) {
        return bold(text, Color.BLUE);
    }

    public static String cyan(String text) {
        return bold(text, Color.CYAN);
    }

    public static String white(String text) {
        return bold(text, Color.WHITE);
    }
}
