package tutinder.mad.uulm.de.tutinder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * @author snap10
 * @author 1uk4s
 */
public class GCMListenerService extends com.google.android.gms.gcm.GcmListenerService {

    private static final String TAG = GCMListenerService.class.getSimpleName();

    /**
     * Restart the service once it has been killed by the app launcher. Can't restart if the service
     * has been killed in the settings, this can't be done.
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT
        );

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);
    }

    /**
     * Send a BoradCast to the GCMNotificationService if a new GCM Push Notification arrives
     * on the device.
     * @param from
     * @param data
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + data.toString());
        final String action = data.getString("action");
        Log.d(TAG, "Action: " + action);
        if (action == null) {
            Log.w(TAG, "onMessageReceived: Action was null, skipping further processing.");
            return;
        }

        if(data == null) {
            Log.d(TAG, "data is null");
        }

        data.putString("action", action);

        // Send to GCMNotificationService
        Intent service = new Intent(getApplicationContext(), GCMNotificationService.class);
        service.putExtra("serverdata", data);
        getApplicationContext().startService(service);
    }

}
