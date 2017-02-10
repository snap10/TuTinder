package tutinder.mad.uulm.de.tutinder.exceptions;

/**
 * Created by Snap10 on 03.07.16.
 */
public class UsersNotLoadedException extends Exception {
    public UsersNotLoadedException() {
    }

    public UsersNotLoadedException(String detailMessage) {
        super(detailMessage);
    }

    public UsersNotLoadedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UsersNotLoadedException(Throwable throwable) {
        super(throwable);
    }
}
