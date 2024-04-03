package exceptions;

import java.io.IOException;

public class ManagerLoadException extends RuntimeException {

    public ManagerLoadException(String message, IOException e) {
        super(message, e);
    }

    public ManagerLoadException(String message) {
        super(message);
    }

}
