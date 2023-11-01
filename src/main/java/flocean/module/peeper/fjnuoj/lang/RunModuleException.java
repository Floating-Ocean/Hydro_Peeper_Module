package flocean.module.peeper.fjnuoj.lang;

public class RunModuleException extends RuntimeException {

    public RunModuleException(String s) {
        super("The silly program encountered into a strange problem. Here are the details: " + s);
    }

    public RunModuleException(String s, Throwable cause) {
        super("The silly program encountered into a strange problem. Here are the details: " + s, cause);
    }

    public RunModuleException(Throwable cause) {
        super(cause);
    }

}
