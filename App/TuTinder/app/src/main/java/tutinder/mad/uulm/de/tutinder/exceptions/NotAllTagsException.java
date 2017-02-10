package tutinder.mad.uulm.de.tutinder.exceptions;

/**
 * Created by Snap10 on 13.06.16.
 */
public class NotAllTagsException extends Exception {
    public NotAllTagsException() {
        super();
    }

    public NotAllTagsException(String detailMessage) {
        super(detailMessage);
    }

    public NotAllTagsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NotAllTagsException(Throwable throwable) {
        super(throwable);
    }
}
