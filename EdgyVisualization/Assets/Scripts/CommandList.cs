public enum Command
{
    NULL = 999,

    TEST = 0,
    ECHO = 100,
    UPLOAD_MAP = 101,
    UPDATE_MAP = 102,
    FIND_PATH = 105,

    PRINT_EDGE = 201,
    PRINT_TASK_INFO = 202
}

public static class CommandList {
    public static string GetCommandCode(Command c)
    {
        return ((int)c).ToString();
    }
}
