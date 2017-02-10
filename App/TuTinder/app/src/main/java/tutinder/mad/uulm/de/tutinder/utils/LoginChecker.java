package tutinder.mad.uulm.de.tutinder.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;

import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

/**
 * Created by Snap10 on 08.07.16.
 */
public class LoginChecker {

    public static final int NO_CREDENTIALS_PROVIDED = 1;
    public static final int SUCCESS = 0;
    public static final int PARSING_ERROR = 2;
    public static final int STRANGE_ERROR = 3;
    public static final int INVALID_CREDENTIALS = 4;

    public LoginChecker() {

    }

    /**
     * Checks if the TutinderInstance has already a userobject and fires the listener if not null
     * Else it takes credentials from SharedPreferences and trys to connect to the server and geht the userobject back
     * If Success it fires the listener with statuscode Success
     * Else it provides some Errorflags
     *
     * @param context
     * @param listener
     */
    public static void logInCurrentUser(final Context context, final LoginCheckerListener listener) {
        if (Tutinder.getInstance().getLoggedInUser() != null) {
            listener.onLoginComplete(SUCCESS);
        } else {
            VolleySingleton volley = VolleySingleton.getInstance(context);
            SharedPreferences prefs = context.getSharedPreferences("logged_in_user", context.MODE_PRIVATE);
            if (prefs != null) {
                try {
                    final int matrikelnr = prefs.getInt("matrikelnr", 0);
                    final String password = prefs.getString("password", null);
                    HashMap<String, String> credentials = new HashMap<String, String>();
                    if (matrikelnr != 0 && password != null) {

                        credentials.put("matrikelnr", "" + matrikelnr);
                        credentials.put("password", password);
                        GsonRequest req = new GsonRequest<User>(Request.Method.GET, volley.getAPIRoot() + "/user/login", null, User.class, credentials, new Response.Listener<User>() {

                            @Override
                            public void onResponse(User user) {
                                if (user.getMatrikelnr() == matrikelnr) {
                                    Tutinder helper = Tutinder.getInstance();

                                    //Login correct
                                    //saves the logged in User in the ApplicationSingleton as GlobalUser Object
                                    user.setPassword(password);
                                    helper.setLoggedInUser(user);
                                    listener.onLoginComplete(SUCCESS);
                                } else {
                                    //Something strange happened, can possibly not be the case...
                                    listener.onLoginComplete(STRANGE_ERROR);

                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                    listener.onLoginComplete(INVALID_CREDENTIALS);

                                } else {
                                    listener.onLoginComplete(STRANGE_ERROR);
                                }
                            }
                        });
                        req.setRetryPolicy(new DefaultRetryPolicy(3000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        volley.addToRequestQueue(req);
                    } else {

                        listener.onLoginComplete(NO_CREDENTIALS_PROVIDED);
                    }
                } catch (Exception e) {
                    listener.onLoginComplete(PARSING_ERROR);
                }
            } else {
                listener.onLoginComplete(NO_CREDENTIALS_PROVIDED);
            }
        }
    }


    public interface LoginCheckerListener {

        public void onLoginComplete(int status);

    }

}
