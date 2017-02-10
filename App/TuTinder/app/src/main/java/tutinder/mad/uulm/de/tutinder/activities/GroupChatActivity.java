package tutinder.mad.uulm.de.tutinder.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.GroupChatAdapter;
import tutinder.mad.uulm.de.tutinder.configs.GcmAction;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.Message;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

public class GroupChatActivity extends AppCompatActivity {

    private static final int LOG_IN_USER = 1;
    private Context context;
    private Tutinder helper;
    private VolleySingleton volleySingleton;

    private String bundleGroupID;
    private Group group;

    private RelativeLayout layoutGroupChat;
    private Toolbar toolbar;
    private ProgressBar loadingProgressBar;
    private RecyclerView recyclerMessages;
    private GroupChatAdapter recyclerMessagesAdapter;
    private List<Message> messages;
    private ImageButton btnSend;
    private EditText inMessage;
    private CircleNetworkImageView ivThumbnail;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onResume() {
        super.onResume();
        /*
        *
        * //TODO does not work at the moment...
        * */
        final IntentFilter filter = new IntentFilter();
        filter.addAction(GcmAction.NEW_MESSAGE);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() != null) {
                    Bundle extra = intent.getBundleExtra(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
                    String intentgroupid = extra.getString("groupid");
                    if (intentgroupid != null && bundleGroupID != null && intentgroupid.equals(bundleGroupID)) {
                        //TODO handle in a more performant way...
                        requestGroupMessage(bundleGroupID);
                        Intent serviceIntent = new Intent(GroupChatActivity.this, GCMNotificationService.class);
                        extra.putBoolean("delete",true);

                        serviceIntent.putExtra(GCMNotificationService.BUNDLE_NAME_SERVERDATA,extra);
                        startService(serviceIntent);
                    }

                }
            }
        };
        manager.registerReceiver(broadcastReceiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (broadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        /*
            Get the Application References

         */

        context = getApplicationContext();


        volleySingleton = VolleySingleton.getInstance(context);
         /*
            Arguments
         */
        Bundle args = getIntent().getExtras();
        if (args != null) {
            if (args.containsKey("groupid")) {
                bundleGroupID = args.getString("groupid");
            } else {
                Toast.makeText(getApplicationContext(), "Bundle: " + R.string.strangeError, Toast.LENGTH_LONG).show();
                finish();
            }
            if (args.containsKey(GCMNotificationService.BUNDLE_NAME_SERVERDATA)) {
                Bundle serverBundle = args.getBundle(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
                if (serverBundle.getBoolean("delete")) {
                    Intent service = new Intent(this, GCMNotificationService.class);
                    service.putExtra("serverdata", serverBundle);
                    startService(service);
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Bundle do not exits", Toast.LENGTH_LONG).show();
            finish();
        }
        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int error) {
                if (LoginChecker.SUCCESS == error) {
                    helper = Tutinder.getInstance();
                    initializeView();
                    requestGroup(bundleGroupID);
                }
            }
        });



        /*
            API-Call
         */

    }

    private void initializeView() {
    /*
        Toolbar
     */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Get the View Elements
         */
        layoutGroupChat = (RelativeLayout) findViewById(R.id.layout_group_chat);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);

        messages = new ArrayList<Message>();
        recyclerMessages = (RecyclerView) findViewById(R.id.recycler_messages);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        btnSend = (ImageButton) findViewById(R.id.btn_send);
        disableSendButton();
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSendMessage(inMessage.getText().toString());
            }
        });

        inMessage = (EditText) findViewById(R.id.in_message);
        inMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    enableSendButton();
                } else {
                    disableSendButton();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ivThumbnail = (CircleNetworkImageView) findViewById(R.id.iv_thumbnail);
        ivThumbnail.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        ivThumbnail.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        ivThumbnail.setImageUrl(helper.getLoggedInUser().getProfileThumbnailPath(), volleySingleton.getImageLoader());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void enableSendButton() {
        if (!btnSend.isEnabled()) {
            btnSend.setImageResource(R.drawable.ic_send_accent_24dp);
            btnSend.setClickable(true);
            btnSend.setFocusable(true);
            btnSend.setEnabled(true);
        }
    }

    public void disableSendButton() {
        if (btnSend.isEnabled()) {
            btnSend.setImageResource(R.drawable.ic_send_grey_24dp);
            btnSend.setClickable(false);
            btnSend.setFocusable(false);
            btnSend.setEnabled(false);
        }
    }

    public void onRequestResponse(Group group) {
        this.group = group;

        group.getCourse(volleySingleton, helper, new Group.OnCourseLoadedListener() {
            @Override
            public void onCourseLoaded(Course course) {
                getSupportActionBar().setSubtitle(course.getName());
            }
        });

        this.messages = Arrays.asList(group.getMessages());
        recyclerMessagesAdapter = new GroupChatAdapter(this, group);
        recyclerMessagesAdapter.setMessages(this.messages);
        recyclerMessages.setAdapter(recyclerMessagesAdapter);
        recyclerMessages.scrollToPosition(messages.size() - 1);
    }

    public void requestSendMessage(final String message) {
        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject text = new JSONObject();
            text.put("text", message);

            jsonBody.put("message", text);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        final String URL = volleySingleton.getAPIRoot() + "/user/groups/" + group.get_id() + "/messages";
        GsonRequest<Message[]> request = new GsonRequest<>(Request.Method.POST, URL, jsonBody.toString(), Message[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Message[]>() {
            @Override
            public void onResponse(Message[] response) {
                hideLoadingProgressBar();
                if (response != null) {
                    inMessage.setText("");
                    messages = Arrays.asList(response);
                    recyclerMessagesAdapter.setMessages(messages);
                    recyclerMessages.scrollToPosition(messages.size() - 1);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                Snackbar.make(layoutGroupChat, R.string.error_sending_message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestSendMessage(inMessage.getText().toString());
                            }
                        })
                        .show();
            }
        });
        showLoadingProgressBar();
        //Prevent from sending a Message two times...
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        volleySingleton.addToRequestQueue(request);
    }

    public void requestGroup(String groupID) {
        final String URL = volleySingleton.getAPIRoot() + "/user/groups/" + groupID;
        GsonRequest<Group> request = new GsonRequest<Group>(Request.Method.GET, URL, null, Group.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Group>() {
            @Override
            public void onResponse(Group response) {
                hideLoadingProgressBar();
                onRequestResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
            }
        });
        showLoadingProgressBar();
        volleySingleton.addToRequestQueue(request);
    }


    private void requestGroupMessage(String bundleGroupID) {
        final String URL = volleySingleton.getAPIRoot() + "/user/groups/" + bundleGroupID + "/messages";
        GsonRequest<Message[]> request = new GsonRequest<Message[]>(Request.Method.GET, URL, null, Message[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Message[]>() {
            @Override
            public void onResponse(Message[] response) {
                hideLoadingProgressBar();
                GroupChatActivity.this.messages = Arrays.asList(response);
                recyclerMessagesAdapter.setMessages(GroupChatActivity.this.messages);
                recyclerMessages.setAdapter(recyclerMessagesAdapter);
                recyclerMessages.scrollToPosition(messages.size() - 1);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
            }
        });
        showLoadingProgressBar();
        volleySingleton.addToRequestQueue(request);
    }

    public void showLoadingProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setEnabled(true);
        }
    }

    public void hideLoadingProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
            loadingProgressBar.setEnabled(false);
        }
    }
}
