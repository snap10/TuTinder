package tutinder.mad.uulm.de.tutinder.singletons;

import android.app.Application;

import tutinder.mad.uulm.de.tutinder.models.User;

/**
 * Created by Snap10 on 29.04.16.
 */
public class Tutinder extends Application {


    private static Tutinder ourInstance = new Tutinder();
    private User loggedInUser;


    public static synchronized Tutinder getInstance() {

            return ourInstance;

    }


    public Tutinder() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ourInstance = this;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }


}
