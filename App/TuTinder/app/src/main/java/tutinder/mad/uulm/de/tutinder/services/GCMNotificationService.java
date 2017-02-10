package tutinder.mad.uulm.de.tutinder.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.FriendActivity;
import tutinder.mad.uulm.de.tutinder.activities.FriendlistActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupChatActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupRequestListActivity;
import tutinder.mad.uulm.de.tutinder.activities.MatchActivity;
import tutinder.mad.uulm.de.tutinder.activities.MatchListActivity;
import tutinder.mad.uulm.de.tutinder.configs.GcmAction;
import tutinder.mad.uulm.de.tutinder.utils.ImageHelper;

/**
 * Created by Lukas on 06.07.2016.
 */
public class GCMNotificationService extends Service {

    private final String TAG = GCMNotificationService.class.getSimpleName();

    public static final String BUNDLE_NAME_SERVERDATA = "serverdata";

    private final String NOTIFICATION_SPACER_MESSAGE = ":  ";

    private final String NOTIFICATION_GROUP_NEW_MATCH = "group_new_match";
    private final int NOTIFICATION_ID_NEW_MATCH = 0;
    private final String NOTIFICATION_GROUP_NEW_FRIEND = "group_new_friend";
    private final int NOTIFICATION_ID_NEW_FRIEND = 1;
    private final String NOTIFICATION_GROUP_NEW_REQUEST = "group_new_request";
    private final int NOTIFICATION_ID_NEW_REQUEST = 2;
    private final String NOTIFICATION_GROUP_NEW_MEMBER = "group_new_member";
    private final int NOTIFICATION_ID_NEW_MEMBER = 3;
    private final String NOTIFICATION_GROUP_MEMBER_LEFT = "group_member_left";
    private final int NOTIFICATION_ID_MEMBER_LEFT = 4;
    private final String NOTIFICATION_GROUP_NEW_MESSAGE = "group_new_message";
    // Ids fpr new message notifications are generated programatically


    private Context mContext;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private NotificationManagerCompat mNotificationManager;

    private class ServiceHandler extends Handler {

        private int globalId;

        private Map<String, Integer> groupChats;
        private Map<String, Integer> prevGroupChatNotifications;
        private int prevMatchNotifications;
        private int prevFriendNotifications;
        private int prevGroupRequestNotifications;

        public ServiceHandler(Looper looper) {
            super(looper);
            // initialise
            globalId = -1;

            groupChats = new HashMap<String, Integer>();
            prevGroupChatNotifications = new HashMap<String, Integer>();
            prevMatchNotifications = 0;
            prevFriendNotifications = 0;
            prevGroupRequestNotifications = 0;

        }

        @Override
        public void handleMessage(Message message) {
            // Get the delivered data
            Bundle data = message.getData();
            if (data != null) {
                // Get the action
                String action = data.getString("action");
                Log.d(TAG, "Action: " + action);

                if (action != null) {
                    switch (action) {
                        case GcmAction.REGISTRATION_COMPLETE:
                            // do nothing...
                            break;
                        case GcmAction.NEW_FRIEND_REQUEST:
                            if (data.getBoolean("delete")) {
                                Log.d(TAG, "reseted new friend notification counter to 0");
                                prevFriendNotifications = 0;
                            }
                            sendNewFriendNotification(data);
                            break;
                        case GcmAction.NEW_GROUP_REQUEST:
                            if (data.getBoolean("delete")) {
                                Log.d(TAG, "reseted new group request notification counter to 0");
                                prevFriendNotifications = 0;
                            }
                            sendNewGroupRequestNotification(data);
                            break;
                        case GcmAction.NEW_GROUPMEMBER:
                            sendNewGroupMemberNotification(data);
                            break;
                        case GcmAction.NEW_MATCH:
                            if (data.getBoolean("delete")) {
                                Log.d(TAG, "reseted match notification counter to 0");
                                prevMatchNotifications = 0;
                            }
                            sendNewMatchNotification(action, data);
                            break;
                        case GcmAction.NEW_MATCH_WITH_GROUP:
                            if (data.getBoolean("delete")) {
                                Log.d(TAG, "reseted match notification counter to 0");
                                prevMatchNotifications = 0;
                            }
                            sendNewMatchNotification(action, data);
                            break;
                        case GcmAction.MEMBER_LEFT:
                            sendMemberLeftNotification(data);
                        case GcmAction.NEW_MESSAGE:
                            if (data.getBoolean("delete")) {
                                Log.d(TAG, "delete group chat notifiaction counter of group: " + data.getString("groupid"));
                                prevGroupChatNotifications.put(data.getString("groupid"), 0);
                                if (mNotificationManager != null) {
                                    mNotificationManager.cancel(groupChats.get(data.getString("groupid")));
                                }
                            } else {
                                //Send Broadcast for GroupChatActivity
                                Intent broadCastIntent = new Intent(GcmAction.NEW_MESSAGE);
                                broadCastIntent.putExtra("serverdata", data);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadCastIntent);
                                sendNewMessageNotification(data);
                            }
                            break;
                        default:
                            Log.d(TAG, "Unspecified action, service done nothing.");
                            break;
                    }
                } else {
                    Log.d(TAG, "Action was null, service done nothing.");
                }
            }
            // Don't stop the service here. It will stop if the user logout.
        }

        /**
         * Sends a Notification if a Message got delivered.
         *
         * @param data
         */
        public void sendNewMessageNotification(Bundle data) {
            // Get the groupId
            String groupId = data.getString("groupid");

            // Prepare notificaitonId
            int tempId = -1;
            if (groupChats.containsKey(groupId)) {
                tempId = groupChats.get(groupId);
            } else {
                globalId++;
                groupChats.put(groupId, globalId);
                tempId = globalId;
            }
            final int notificationId = tempId;

            // Check if other Notifiactions exist
            int prevMessages = 0;
            if (prevGroupChatNotifications.containsKey(groupId)) {
                prevMessages = prevGroupChatNotifications.get(groupId);
            }
            // Set data delete after delete or click notification
            data.putBoolean("delete", true);

            // Prepare PendingIntent for notification delete
            Intent deleteIntent = new Intent(mContext, GCMNotificationService.class);
            deleteIntent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            PendingIntent pendingIntentDelete = PendingIntent.getService(mContext, (int) System.currentTimeMillis(), deleteIntent, PendingIntent.FLAG_ONE_SHOT);
            // Prepare PendingIntent for Notification click
            Intent clickIntent = new Intent(mContext, GroupChatActivity.class);
            clickIntent.putExtra("groupid", groupId);
            clickIntent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntentClick = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), clickIntent, PendingIntent.FLAG_ONE_SHOT);

            // Prepare NotificationSound
            Uri uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                    .setContentTitle(data.getString("coursename"))
                    .setContentIntent(pendingIntentClick)
                    .setDeleteIntent(pendingIntentDelete)
                    .setAutoCancel(true)
                    .setSound(uriNotificationSound)
                    .setGroup(NOTIFICATION_GROUP_NEW_MESSAGE);

            if (prevMessages > 0) {
                mBuilder.setStyle(new NotificationCompat.InboxStyle()
                        .addLine(data.getString("username") + NOTIFICATION_SPACER_MESSAGE + data.getString("text"))
                        .setSummaryText("+" + prevMessages + " " + getString(R.string.notification_message_summary))
                );
            } else {
                mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentText(data.getString("username") + NOTIFICATION_SPACER_MESSAGE + data.getString("text"));
            }

            // Save Notification
            prevMessages++;
            prevGroupChatNotifications.put(groupId, prevMessages);

            // Check Picture and send Notification
            if (data.getString("userpicture") != null) {
                Log.d(TAG, "Userpicture: " + data.getString("userpicture"));
                try {
                    URL url = new URL(ImageHelper.getThumbnailFrom(data.getString("userpicture"), 200, 200));
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mBuilder.setLargeIcon(ImageHelper.getCircleBitmap(bitmap));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mNotificationManager.notify(notificationId, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(notificationId, mBuilder.build());
            }
        }

        /**
         * Sends a Notification if a Match got delivered.
         *
         * @param data
         */
        public void sendNewMatchNotification(String action, Bundle data) {
            // Set data delete after delete or click notification
            data.putBoolean("delete", true);

            // Prepare PendingIntent for notification delete
            Intent deleteIntent = new Intent(mContext, GCMNotificationService.class);
            deleteIntent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            PendingIntent pendingIntentDelete = PendingIntent.getService(mContext, (int) System.currentTimeMillis(), deleteIntent, PendingIntent.FLAG_ONE_SHOT);
            // Prepare PendingIntent for notification click
            Intent intent;
            if (prevMatchNotifications > 0) {
                intent = new Intent(mContext, MatchListActivity.class);
            } else {
                if (action.equals(GcmAction.NEW_MATCH)) {
                    intent = new Intent(mContext, MatchActivity.class);
                } else {
                    intent = new Intent(mContext, GroupActivity.class);
                }
            }
            intent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            // Prepare NotificationSound
            Uri uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                    .setContentText(getString(R.string.notification_match_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(uriNotificationSound)
                    .setGroup(NOTIFICATION_GROUP_NEW_MATCH);

            if (prevMatchNotifications > 0) {
                mBuilder.setContentTitle(prevMatchNotifications + " " + getString(R.string.notification_match_title_multible));
            } else {
                mBuilder.setContentTitle(getString(R.string.notification_match_title_single));
            }

            // Save Notification
            prevMatchNotifications++;

            // Check Picture and send Notification
            if (data.getString("matchedpicture") != null) {
                Log.d(TAG, "Matchedpicture: " + data.getString("matchedpicture"));
                try {
                    URL url = new URL(ImageHelper.getThumbnailFrom(data.getString("matchedpicture"), 200, 200));
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mBuilder.setLargeIcon(ImageHelper.getCircleBitmap(bitmap));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mNotificationManager.notify(NOTIFICATION_ID_NEW_MATCH, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(NOTIFICATION_ID_NEW_MATCH, mBuilder.build());
            }
        }

        /**
         * Sends a Notification if a new Friend got delivered.
         *
         * @param data
         */
        public void sendNewFriendNotification(Bundle data) {
            // Set data delete after delete or click notification
            data.putBoolean("delete", true);

            // Prepare PendingIntent for notification delete
            Intent deleteIntent = new Intent(mContext, GCMNotificationService.class);
            deleteIntent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            PendingIntent pendingIntentDelete = PendingIntent.getService(mContext, (int) System.currentTimeMillis(), deleteIntent, PendingIntent.FLAG_ONE_SHOT);
            // Prepare PendingIntent for notification click
            Intent intent;
            if (prevFriendNotifications > 0) {
                intent = new Intent(mContext, FriendlistActivity.class);
            } else {
                intent = new Intent(mContext, FriendActivity.class);
            }
            intent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            // Prepare NotificationSound
            Uri uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                    .setContentText(getString(R.string.notification_friend_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(uriNotificationSound)
                    .setGroup(NOTIFICATION_GROUP_NEW_FRIEND);

            if (prevMatchNotifications > 0) {
                mBuilder.setContentTitle(prevFriendNotifications + " " + getString(R.string.notification_friend_title_multible));
            } else {
                mBuilder.setContentTitle(getString(R.string.notification_friend_title_single));
            }

            // Save Notification
            prevFriendNotifications++;

            // Check Picture and send Notification
            if (data.getString("friendpicture") != null) {
                Log.d(TAG, "Friendpicture: " + data.getString("friendpicture"));
                try {
                    URL url = new URL(ImageHelper.getThumbnailFrom(data.getString("friendpicture"), 200, 200));
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mBuilder.setLargeIcon(ImageHelper.getCircleBitmap(bitmap));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mNotificationManager.notify(NOTIFICATION_ID_NEW_FRIEND, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(NOTIFICATION_ID_NEW_FRIEND, mBuilder.build());
            }
        }

        /**
         * Sends a Notification if a new Group Reqeust got delivered.
         *
         * @param data
         */
        public void sendNewGroupRequestNotification(Bundle data) {
            // Set data delete after delete or click notification
            data.putBoolean("delete", true);

            // Prepare PendingIntent for notification delete
            Intent deleteIntent = new Intent(mContext, GCMNotificationService.class);
            deleteIntent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            PendingIntent pendingIntentDelete = PendingIntent.getService(mContext, (int) System.currentTimeMillis(), deleteIntent, PendingIntent.FLAG_ONE_SHOT);
            // Prepare PendingIntent for notification click
            Intent intent = new Intent(mContext, GroupRequestListActivity.class);
            intent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            // Prepare NotificationSound
            Uri uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                    .setContentText(getString(R.string.notification_group_request_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(uriNotificationSound)
                    .setGroup(NOTIFICATION_GROUP_NEW_REQUEST);

            if (prevGroupRequestNotifications > 0) {
                mBuilder.setContentTitle(prevGroupRequestNotifications + " " + getString(R.string.notification_group_request_title_multible));
            } else {
                mBuilder.setContentTitle(getString(R.string.notification_group_request_title_single));
            }

            // Save Notification
            prevGroupRequestNotifications++;

            // Check Picture and send Notification
            if (data.getString("userpicture") != null) {
                Log.i(TAG, "Userpicture: " + data.getString("userpicture"));
                try {
                    String urlString = ImageHelper.getThumbnailFrom(data.getString("userpicture"), 200, 200);
                    URL url = new URL(urlString);
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mBuilder.setLargeIcon(ImageHelper.getCircleBitmap(bitmap));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mNotificationManager.notify(NOTIFICATION_ID_NEW_REQUEST, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(NOTIFICATION_ID_NEW_REQUEST, mBuilder.build());
            }
        }

        /**
         * Sends a Notification if a new Group Member got delivered.
         *
         * @param data
         */
        public void sendNewGroupMemberNotification(Bundle data) {
            // Prepare PendingIntent for notification click
            Intent intent = new Intent(mContext, GroupActivity.class);
            data.putBoolean("ismember", true);
            intent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            // Prepare NotificationSound
            Uri uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                    .setContentTitle(getString(R.string.notification_new_group_member_title))
                    .setContentText(data.getString("username") + " - in " + data.getString("coursename"))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(uriNotificationSound)
                    .setGroup(NOTIFICATION_GROUP_NEW_MEMBER);

            // Check Picture and send Notification
            if (data.getString("userpicture") != null) {
                Log.d(TAG, "Userpicture: " + data.getString("userpicture"));
                try {
                    URL url = new URL(ImageHelper.getThumbnailFrom(data.getString("userpicture"), 200, 200));
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mBuilder.setLargeIcon(ImageHelper.getCircleBitmap(bitmap));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mNotificationManager.notify(NOTIFICATION_ID_NEW_MEMBER, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(NOTIFICATION_ID_NEW_MEMBER, mBuilder.build());
            }
        }

        /**
         * Sends a Notification if a Group Member Left got delivered.
         *
         * @param data
         */
        public void sendMemberLeftNotification(Bundle data) {
            // Prepare PendingIntent for notification click
            Intent intent = new Intent(mContext, GroupActivity.class);
            intent.putExtra(BUNDLE_NAME_SERVERDATA, data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            // Prepare NotificationSound
            Uri uriNotificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // Build Notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.drawable.ic_tutinder_white_24dp)
                    .setContentTitle(getString(R.string.notification_member_left))
                    .setContentText(data.getString("username") + " - in " + data.getString("coursename"))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(uriNotificationSound)
                    .setGroup(NOTIFICATION_GROUP_MEMBER_LEFT);

            // Check Picture and send Notification
            if (data.getString("userpicture") != null) {
                Log.d(TAG, "Userpicture: " + data.getString("userpicture"));
                try {
                    URL url = new URL(ImageHelper.getThumbnailFrom(data.getString("userpicture"), 200, 200));
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mBuilder.setLargeIcon(ImageHelper.getCircleBitmap(bitmap));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mNotificationManager.notify(NOTIFICATION_ID_MEMBER_LEFT, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(NOTIFICATION_ID_MEMBER_LEFT, mBuilder.build());
            }
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "service starting");

        mContext = getApplicationContext();
        mNotificationManager = NotificationManagerCompat.from(mContext);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
            // For each start request, send a message to start a job and deliver the
            // start ID so we know which request we're stopping when we finish the job
            Message message = mServiceHandler.obtainMessage();
            message.arg1 = startId;
            if (intent.hasExtra(BUNDLE_NAME_SERVERDATA)) {
                message.setData(intent.getBundleExtra(BUNDLE_NAME_SERVERDATA));
            }
            mServiceHandler.sendMessage(message);
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "service done");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1, restartService, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePI);

        super.onTaskRemoved(rootIntent);
    }
}
