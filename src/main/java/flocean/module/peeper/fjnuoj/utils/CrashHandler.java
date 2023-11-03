package flocean.module.peeper.fjnuoj.utils;

import flocean.module.peeper.fjnuoj.lang.ExpectedException;
import flocean.module.peeper.fjnuoj.lang.RunModuleException;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.DateTimeException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.zip.DataFormatException;
import java.util.zip.ZipException;

public class CrashHandler {
    private static final Map<String, String> throwableMap;

    static {
        throwableMap = new HashMap<>();
        BiConsumer<Class<?>, String> put = (obj, detail) -> {
            String id = String.format(Locale.ROOT, "0x100000%02x", throwableMap.size() / 6 * 16 + (throwableMap.size()) % 6 + 10);
            throwableMap.put(obj.getCanonicalName(), id + " - " + detail);
        };
        put.accept(ExpectedException.class, "On schedule.");
        put.accept(RunModuleException.class, "In-module error.");
        put.accept(AssertionError.class, "Variable assertion error.");
        put.accept(ClassCircularityError.class, "Endless calling class.");
        put.accept(ClassFormatError.class, "Class format error.");
        put.accept(IllegalAccessError.class, "Access failed.");
        put.accept(NoClassDefFoundError.class, "No such class definition.");
        put.accept(NoSuchMethodError.class, "No such method.");
        put.accept(NoSuchFieldError.class, "No such field.");
        put.accept(OutOfMemoryError.class, "Memory overflowed.");
        put.accept(StackOverflowError.class, "Stack overflowed.");
        put.accept(ArrayIndexOutOfBoundsException.class, "Array index out of bounds.");
        put.accept(ClassCastException.class, "Class cast failed.");
        put.accept(ClassNotFoundException.class, "No such class.");
        put.accept(CloneNotSupportedException.class, "Clone failed.");
        put.accept(IllegalStateException.class, "Wrong state.");
        put.accept(IndexOutOfBoundsException.class, "Index out of bounds.");
        put.accept(IllegalAccessException.class, "Access failed.");
        put.accept(NoSuchFieldException.class, "No such field.");
        put.accept(NoSuchMethodException.class, "No such method.");
        put.accept(NullPointerException.class, "Null pointer.");
        put.accept(SecurityException.class, "Security error.");
        put.accept(StringIndexOutOfBoundsException.class, "String index out of bounds.");
        put.accept(UnsupportedOperationException.class, "Operation unsupported.");
        put.accept(RuntimeException.class, "Runtime error.");
        put.accept(IllegalArgumentException.class, "Illegal argument.");
        put.accept(DataFormatException.class, "Data format error.");
        put.accept(IOException.class, "Input or output error.");
        put.accept(DateTimeException.class, "Date time error.");
        put.accept(EOFException.class, "Unexpectedly end of file.");
        put.accept(FileNotFoundException.class, "File not found.");
        put.accept(NumberFormatException.class, "Number format error.");
        put.accept(ZipException.class, "Zipping error.");
        put.accept(SocketTimeoutException.class, "Socket Read Time Out.");
        put.accept(UnknownHostException.class, "Unknown Host.");
    }


    /**
     * 处理异常，生成错误堆栈信息
     *
     * @param throwable 异常
     * @return 堆栈信息以及其他详情
     */
    public static String handleError(Throwable throwable) {
        StringBuilder stackDetails = new StringBuilder();
        stackDetails.append(throwable.toString());
        for (StackTraceElement re : throwable.getStackTrace()) stackDetails.append("\n    at ").append(re.toString());
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            stackDetails.append("\n\nCaused by: ").append(cause.toString());
            for (StackTraceElement re : cause.getStackTrace())
                stackDetails.append("\n    at ").append(re.toString());
        }
        String crashInfo = throwableMap.get(cause.getClass().getCanonicalName());
        if (crashInfo == null) crashInfo = "0x10000fff - Unhandled error.";
        crashInfo = "Encountered an exception. Please contact maintainer.\n" + crashInfo;
        crashInfo += "\n\nDetailed Information: \n";
        crashInfo += stackDetails.toString().replaceAll("\\.", ". ");
        return crashInfo;
    }

}
