package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.GroupMemberListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.GroupRequest;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

public class GroupRequestActivity extends AppCompatActivity {


    private final int LOADING_ARRAY_POSITION_GROUP = 0;
    private final int LOADING_ARRAY_POSITION_GROUP_SIZE = 1;
    private final int LOADING_ARRAY_POSITION_COURSE = 2;
    private final int LOADING_ARRAY_POSITION_USERS = 3;

    private Context context;
    private Tutinder helper;
    private VolleySingleton volleySingleton;

    private String bundleGroupRequestID;
    private boolean bundleIsMember;


    private TextView tvCoursename, tvGroupMembers;
    private RecyclerView recyclerMembers;
    private FloatingActionButton btnFloating;

    private ProgressBar loadingProgressBar;
    private boolean[] loadingArray = {false, false, false, false};
    private GroupRequest groupRequest;
    private CoordinatorLayout wrapper;

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
            bundleGroupRequestID = args.getString("grouprequestid");
            if (bundleGroupRequestID == null) {
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
            toolbar.setTitle(R.string.grouprequest_title);
        }
            /*
                Get the View Elements
             */
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        wrapper = (CoordinatorLayout) findViewById(R.id.cl_group_activity);
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
                requestGroupRequest(bundleGroupRequestID);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRequestResponse(final GroupRequest groupRequest) {
        this.groupRequest = groupRequest;
        tvCoursename.setText(groupRequest.get_courseid().getName());
        showLoadingProgressBar();
        recyclerMembers.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerMembers.setAdapter(new GroupMemberListAdapter(GroupRequestActivity.this, groupRequest.getUsers(), groupRequest.get_courseid().get_id()));
        requestGroupSize(groupRequest.get_courseid().getId());
        if (bundleIsMember) {
            btnFloating.setImageResource(R.drawable.ic_done_white_24dp);
        } else {
            btnFloating.setImageResource(R.drawable.ic_thumbs_up_down_white_24dp);
            btnFloating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupDialog();
                }
            });

        }

        btnFloating.setVisibility(View.VISIBLE);
    }

    public void requestGroupRequest(String groupRequestID) {
        final String URL = volleySingleton.getAPIRoot() + "/courses/grouprequests/" + groupRequestID;
        GsonRequest<GroupRequest> request = new GsonRequest<GroupRequest>(Request.Method.GET, URL, null, GroupRequest.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<GroupRequest>() {
            @Override
            public void onResponse(GroupRequest response) {
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
                        String text = tvGroupMembers.getText() + " (" + groupRequest.getAcceptedNumber() + "/" + maxGroupSize + "), " + getString(R.string.requested) + "(" + groupRequest.getMembers().size() + ")";
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

    public void showPopupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupRequestActivity.this, R.style.TuTinder_Dialog_Alert);
        builder.setTitle(context.getString(R.string.dialog_title_group_request_action))
                .setMessage(context.getString(R.string.dialog_message_group_request_action))
                .setIcon(R.drawable.ic_thumbs_up_down_black_24dp)
                .setPositiveButton(context.getString(R.string.btn_accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestAccept(bundleGroupRequestID);
                    }
                })
                .setNegativeButton(context.getString(R.string.btn_deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestDeny(bundleGroupRequestID);
                    }
                })
                .setNeutralButton(context.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void requestAccept(final String requestId) {
        JsonObject object = new JsonObject();
        object.addProperty("accept", true);
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.PUT, volleySingleton.getAPIRoot() + "/courses/grouprequests/" + requestId, object.toString(), helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoadingProgressBar();

                String groupid;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    groupid = jsonObject.getString("groupid");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                // show accepted group
                Intent intent = new Intent(context, GroupActivity.class);
                intent.putExtra("groupid", groupid);
                intent.putExtra("ismember", true);
                context.startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                Snackbar.make(wrapper
                        , R.string.error_sending_group_accept_request, Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestAccept(requestId);
                            }
                        })
                        .show();
            }
        });
        volleySingleton.addToRequestQueue(request);
        showLoadingProgressBar();
    }

    public void requestDeny(final String requestId) {
        JsonObject object = new JsonObject();
        object.addProperty("accept", false);
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.PUT, volleySingleton.getAPIRoot() + "/courses/grouprequests/" + requestId, object.toString(), helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoadingProgressBar();
                Snackbar.make(wrapper, R.string.toast_group_request_denied, Snackbar.LENGTH_SHORT)
                        .show();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                Snackbar.make(wrapper, R.string.error_sending_group_deny_request, Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestDeny(requestId);
                            }
                        })
                        .show();
            }
        });
        volleySingleton.addToRequestQueue(request);
        showLoadingProgressBar();
    }
}

