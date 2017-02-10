package tutinder.mad.uulm.de.tutinder.models;

/**
 * Created by Lukas on 26.06.2016.
 */
public class CardStackUser extends CardStackObject{

    private User user;

    public CardStackUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
