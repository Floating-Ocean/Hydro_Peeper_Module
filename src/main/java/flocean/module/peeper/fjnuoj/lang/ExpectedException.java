package flocean.module.peeper.fjnuoj.lang;

public class ExpectedException extends RuntimeException {

    public ExpectedException(String s) {
        super("An expected exception was thrown as: " + s);
    }

}

