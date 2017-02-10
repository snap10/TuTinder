package tutinder.mad.uulm.de.tutinder.singletons;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.HashMap;
import java.util.Map;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.FriendActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupChatActivity;
import tutinder.mad.uulm.de.tutinder.activities.MatchActivity;
import tutinder.mad.uulm.de.tutinder.configs.GcmAction;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;

/**
 * @author snap10
 * @author 1uk4s
 */

public class TutinderBroadcastReceiver extends WakefulBroadcastReceiver  {

    private final String TAG = TutinderBroadcastReceiver.class.getSimpleName();


    private final String NOTIFICATION_GROUP_KEY_MESSAGES = "group_key_messages";

    private static TutinderBroadcastReceiver mInstance;

    private Map<String, Integer> chatIds;
    int globalID = 0;

    /**
     * Default constructor.
     */
    public TutinderBroadcastReceiver() {
        super();
        chatIds = new HashMap<String, Integer>();
    }

    /**
     * Returns an instance of the TutinderBoradCastReceiver.
     *
     * @return
     */
    public static synchronized TutinderBroadcastReceiver getInstance() {
        if (mInstance == null) {
            mInstance = new TutinderBroadcastReceiver();
        }
        return mInstance;
    }

    /**
     * This method gets called after a GCM Push Notification arrives on the device.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Context: " + context.getPackageName());

        // Get the delivered action
        final String action = intent.getStringExtra("action");
        Log.d(TAG, "Action: " + action);

        // Get the delivered data
        Bundle serverData = intent.getBundleExtra("serverdata");
        // Put some additional data
        if(serverData != null) {
            serverData.putString("action", action);
        }

        // Send to GCMNotificationService
        Intent service = new Intent(context, GCMNotificationService.class);
        service.putExtra("serverdata", serverData);
        context.startService(service);

        if(true) return;
        // kann deinen shit net auskommentieren :P

        Log.d(TAG, "Context: " + context.getPackageName());
        // Get VolleySingleton
        VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
        // Get delivered Bundle
        //Bundle data = intent.getBundleExtra("serverdata");
        // Get the default Notificationsound Uri
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Get the NotificationManager
        final NotificationManagerCompat notificationManager = (NotificationManagerCompat) NotificationManagerCompat.from(context);
        // Get the delivered Action
        //final String action = intent.getAction();
        Log.d(TAG, "onReceive: " + action);

        // Further processing depending on delivered Action
        switch (action) {
            /*
                REGISTRATION COMPLETE
             */
            case GcmAction.REGISTRATION_COMPLETE:
                break;
            /*
                NEW FRIEND REQUEST
             */
            case GcmAction.NEW_FRIEND_REQUEST:
                //TODO go to FriendView not the list, but the actual new friend
                Intent intentFriend = new Intent(context, FriendActivity.class);
                intentFriend.putExtra("serverdata", serverData);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentFriend,
                        PendingIntent.FLAG_ONE_SHOT);

                final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_tutinder_250dp)
                        .setContentTitle("New Friend")
                        .setContentText(serverData.getString("friendname"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                if (serverData.getString("friendpicture") != null) {
                    volleySingleton.getImageLoader().get(serverData.getString("friendpicture"), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                notificationBuilder.setLargeIcon(response.getBitmap());
                                notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilder.build());
                            }

                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilder.build());

                        }
                    });
                } else {
                    notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilder.build());

                }
                break;
            /*
                NEW MATCH
             */
            case GcmAction.NEW_MATCH:
                Intent intentMatch = new Intent(context, MatchActivity.class);
                intentMatch.putExtra("serverdata", serverData);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntentMatch = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentMatch,
                        PendingIntent.FLAG_ONE_SHOT);

                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                final NotificationCompat.Builder notificationBuilderMatch = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_tutinder_250dp)
                        .setContentTitle("New Match")
                        .setContentText(serverData.getString("matchedname"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntentMatch);

                if (serverData.getString("matchedpicture") != null) {
                    volleySingleton.getImageLoader().get(serverData.getString("matchedpicture"), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                notificationBuilderMatch.setLargeIcon(response.getBitmap());
                                notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderMatch.build());
                            }

                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderMatch.build());

                        }
                    });
                } else {
                    notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderMatch.build());

                }
                break;
            /*
                NEW GROUP REQUEST
             */
            case GcmAction.NEW_GROUP_REQUEST:
                Intent intentGroup = new Intent(context, GroupActivity.class);
                intentGroup.putExtra("serverdata", serverData);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntentGroup = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentGroup,
                        PendingIntent.FLAG_ONE_SHOT);

                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                final NotificationCompat.Builder notificationBuilderGroup = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_tutinder_250dp)
                        .setContentTitle("New Group Request")
                        .setContentText(serverData.getString("username"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntentGroup);

                if (serverData.getString("userpicture") != null) {
                    volleySingleton.getImageLoader().get(serverData.getString("userpicture"), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                notificationBuilderGroup.setLargeIcon(response.getBitmap());
                                notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderGroup.build());
                            }

                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderGroup.build());

                        }
                    });
                } else {
                    notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderGroup.build());

                }
                break;
            /*
                NEW GROUP MEMBER
             */
            case GcmAction.NEW_GROUPMEMBER:
                Intent intentGroupMember = new Intent(context, GroupActivity.class);
                intentGroupMember.putExtra("serverdata", serverData);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntentGroupMember = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentGroupMember,
                        PendingIntent.FLAG_ONE_SHOT);

                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                final NotificationCompat.Builder notificationBuilderGroupMember = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_tutinder_250dp)
                        .setContentTitle("New Group Member")
                        .setContentText(serverData.getString("username") + " in " + serverData.getString("coursename"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntentGroupMember);

                if (serverData.getString("userpicture") != null) {
                    volleySingleton.getImageLoader().get(serverData.getString("userpicture"), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                notificationBuilderGroupMember.setLargeIcon(response.getBitmap());
                                notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderGroupMember.build());
                            }

                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderGroupMember.build());

                        }
                    });
                } else {
                    notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilderGroupMember.build());

                }
                break;
            /*
                NEW MESSAGE
             */
            case GcmAction.NEW_MESSAGE:
                // prepare notificationID
                String groupID = serverData.getString("groupid");
                int tempID = -1;
                if(chatIds.containsKey(groupID)) {
                    tempID = chatIds.get(groupID);
                } else {
                    globalID++;
                    chatIds.put(groupID, globalID);
                    tempID = globalID;
                }
                final int notificationID = tempID;
                // prepare the intent
                Intent intentMessage = new Intent(context, GroupChatActivity.class);
                intentMessage.putExtra("groupid", serverData.getString("groupid"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent pendingIntentMessage = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intentMessage,
                        PendingIntent.FLAG_ONE_SHOT);
                // create a new Notification
                final NotificationCompat.Builder builderSingle = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_tutinder_250dp)
                        .setContentTitle("New Mesage: " + serverData.getString("username"))
                        .setContentText(serverData.getString("text"))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntentMessage)
                        .setGroup(NOTIFICATION_GROUP_KEY_MESSAGES)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);
                // update the summaryNotification
                final NotificationCompat.Builder builderSummary = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                        .setContentTitle("Multible Messages")
                        .setStyle(new NotificationCompat.InboxStyle()
                            .addLine(serverData.getString("username") + "  " + serverData.getString("text"))
                                .setBigContentTitle("X new messages")
                        )
                        .setGroup(NOTIFICATION_GROUP_KEY_MESSAGES)
                        .setGroupSummary(true);
                // finally check if user has a profilepicture
                Log.d(TAG, "picture: " + serverData.getString("userpicture"));
                if (serverData.getString("userpicture") != null) {
                    volleySingleton.getImageLoader().get(serverData.getString("userpicture"), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                builderSingle.setLargeIcon(response.getBitmap());
                                notificationManager.notify(notificationID, builderSingle.build());
                                //builderSummary.setLargeIcon(response.getBitmap());
                                //notificationManager.notify(notificationID, builderSummary.build());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notificationManager.notify(notificationID, builderSingle.build());
                            //notificationManager.notify(notificationID, builderSummary.build());
                        }
                    });
                } else {
                    notificationManager.notify(notificationID, builderSingle.build());
                    //notificationManager.notify(notificationID, builderSummary.build());
                }
        }
    }
}
