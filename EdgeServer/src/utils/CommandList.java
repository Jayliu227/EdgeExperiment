package utils;

public class CommandList {

    public enum Command {
        NULL("999"),

        TEST("000"),
        ECHO("100"),
        UPLOAD_MAP("101"),
        UPDATE_MAP("102"),
        FIND_PATH("105"),

        PRINT_EDGE("201"),
        PRINT_TASK_INFO("202");

        private final String commandCode;

        Command(String code) {
            this.commandCode = code;
        }
    }

    public static String GetCommandCode(Command command) {
        return command.commandCode;
    }
}
