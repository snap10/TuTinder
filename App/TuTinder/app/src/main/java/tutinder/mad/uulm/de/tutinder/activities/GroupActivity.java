package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.GroupMemberListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

public class GroupActivity extends AppCompatActivity {

    private final int LOADING_ARRAY_POSITION_GROUP = 0;
    private final int LOADING_ARRAY_POSITION_GROUP_SIZE = 1;
    private final int LOADING_ARRAY_POSITION_COURSE = 2;
    private final int LOADING_ARRAY_POSITION_USERS = 3;

    private Context context;
    private Tutinder helper;
    private VolleySingleton volleySingleton;

    private String bundleGroupID;
    private boolean bundleIsMember;

    private Group group;

    private TextView tvCoursename, tvGroupMembers;
    private RecyclerView recyclerMembers;
    private FloatingActionButton btnFloating;

    private ProgressBar loadingProgressBar;
    private boolean[] loadingArray = {false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        /*
            Get the Application References
         */
        context = getApplicationContext();
        volleySingleton = VolleySingleton.getInstance(context);
         /*
            Arguments
         */
        Bundle args = getIntent().getExtras();

        Bundle serverdata = getIntent().getBundleExtra("serverdata");
        //If serverdata was set we klicked on Notification
        if (serverdata != null) args = serverdata;
        if (args != null) {
            bundleGroupID = args.getString("groupid");
            if (bundleGroupID == null) {
                Toast.makeText(getApplicationContext(), "Bundle: " + R.string.strangeError, Toast.LENGTH_LONG).show();
                finish();
            }
            bundleIsMember = args.getBoolean("ismember");
        } else {
            Toast.makeText(getApplicationContext(), "Bundle do not exits", Toast.LENGTH_LONG).show();
            finish();
        }
        /*
            Toolbar
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Get the View Elements
         */
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);

        tvCoursename = (TextView) findViewById(R.id.tv_coursename);
        tvGroupMembers = (TextView) findViewById(R.id.tv_group_members);

        recyclerMembers = (RecyclerView) findViewById(R.id.recycler_group_members);

        btnFloating = (FloatingActionButton) findViewById(R.id.fab);
        /*
            API-Call
         */
        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                helper = Tutinder.getInstance();
                requestGroup(bundleGroupID);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_group_chat_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.action_leavegroup) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TuTinder_Dialog_Alert);
            builder.setTitle(getString(R.string.dialog_title_leave_group))
                    .setMessage(getString(R.string.dialog_message_leave_group))
                    .setIcon(R.drawable.ic_friend_remove_black_24dp)
                    .setPositiveButton(getString(R.string.btn_leave), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestLeaveGroup(bundleGroupID);
                        }
                    })
                    .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRequestResponse(final Group group) {
        this.group = group;

        showLoadingProgressBar();
        group.getCourse(volleySingleton, helper, new Group.OnCourseLoadedListener() {
            @Override
            public void onCourseLoaded(Course course) {
                loadingArray[LOADING_ARRAY_POSITION_COURSE] = true;
                tvCoursename.setText(course.getName());
                hideLoadingProgressBar();
            }
        });

        recyclerMembers.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        showLoadingProgressBar();
        group.getUsers(getApplicationContext(), volleySingleton, helper, new Group.OnUsersLoadedListener() {

            @Override
            public void onUsersComplete(List<User> users) {
                loadingArray[LOADING_ARRAY_POSITION_USERS] = true;
                recyclerMembers.setAdapter(new GroupMemberListAdapter(GroupActivity.this, users, group.getCourse()));
                hideLoadingProgressBar();
                requestGroupSize(group.getCourse());
            }
        });


        if (bundleIsMember) {
            btnFloating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GroupActivity.this, GroupChatActivity.class);
                    intent.putExtra("groupid", group.get_id());
                    startActivity(intent);
                }
            });
            btnFloating.setVisibility(View.VISIBLE);
        }

        if (group.getMessages().length == 0) {

        }
    }

    public void requestGroup(String groupID) {
        final String URL = volleySingleton.getAPIRoot() + "/user/groups/" + groupID;
        GsonRequest<Group> request = new GsonRequest<Group>(Request.Method.GET, URL, null, Group.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Group>() {
            @Override
            public void onResponse(Group response) {
                loadingArray[LOADING_ARRAY_POSITION_GROUP] = true;
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

    public void requestGroupSize(String courseID) {
        final String URL = volleySingleton.getAPIRoot() + "/courses/" + courseID + "/maxgroupsize";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, URL, null, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response == null) {
                        throw new Resources.NotFoundException("No max Group Size was found on the Server");
                    } else {
                        JSONObject jsonObject = new JSONObject(response);
                        int maxGroupSize = jsonObject.getInt("maxgroupsize");
                        String text = tvGroupMembers.getText() + " (" + group.getUsers().length + "/" + maxGroupSize + ")";
                        tvGroupMembers.setText(text);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                loadingArray[LOADING_ARRAY_POSITION_GROUP_SIZE] = true;
                hideLoadingProgressBar();
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

    /**
     *
     * @param groupID
     */
    public void requestLeaveGroup(String groupID) {
        final String URL = volleySingleton.getAPIRoot() + "/user/groups/" + groupID;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, URL, null, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                hideLoadingProgressBar();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
            }
        }

        );
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
            for (int i = 0; i < loadingArray.length; i++) {
                if (!loadingArray[i]) {
                    return;
                }
            }
            loadingProgressBar.setVisibility(View.GONE);
            loadingProgressBar.setEnabled(false);
        }
    }
}
