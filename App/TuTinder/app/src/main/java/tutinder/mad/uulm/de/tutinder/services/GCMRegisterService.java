package tutinder.mad.uulm.de.tutinder.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.configs.GcmAction;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class GCMRegisterService extends IntentService {

    private static final String TAG = "GCMRegisterService";
    private static final String[] TOPICS = {"global"};
    private VolleySingleton volley;
    private Tutinder helper = Tutinder.getInstance();


    public GCMRegisterService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.

            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.

        }

    }

    private void sendRegistrationToServer(final String token) {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        volley = VolleySingleton.getInstance(getApplicationContext());
        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                CustomJsonRequest updateRequest = new CustomJsonRequest(Request.Method.PUT, volley.getAPIRoot() + "/user/gcmtoken/" + token, null, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        sharedPreferences.edit().putBoolean("sent_token_to_server", true).apply();
                        Intent broadcastIntent = new Intent(GcmAction.REGISTRATION_COMPLETE);
                        LocalBroadcastManager.getInstance(GCMRegisterService.this).sendBroadcast(broadcastIntent);

                        Log.d(TAG, "Successfully send token to server ");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        sharedPreferences.edit().putBoolean("sent_token_to_server", false).apply();
                        Log.d(TAG, "Failed to complete token refresh");
                    }
                });
                volley.addToRequestQueue(updateRequest);
            }
        });

    }


    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
                        pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}


