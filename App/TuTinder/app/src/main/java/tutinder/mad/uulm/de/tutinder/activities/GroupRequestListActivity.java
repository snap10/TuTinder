package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.ExpandableRequestListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.customItems.RequestParentListItem;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.CustomListitem;
import tutinder.mad.uulm.de.tutinder.models.GroupRequest;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

/**
 * @author 1uk4s
 * @author snap10
 */
public class GroupRequestListActivity extends AppCompatActivity {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewRequest;
    private ArrayList<RequestParentListItem> requestItemList;
    private ExpandableRequestListAdapter recyclerAdapterRequest;
    private ProgressBar loadingProgressBar;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_request_list);

        // Application References
        mContext = getApplicationContext();
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContext);

        // Arguments
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
        if (args != null) {
            if (args.containsKey(GCMNotificationService.BUNDLE_NAME_SERVERDATA)) {
                Bundle serverBundle = args.getBundle(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
                if (serverBundle.getBoolean("delete")) {
                    Intent service = new Intent(this, GCMNotificationService.class);
                    service.putExtra("serverdata", serverBundle);
                    startService(service);
                }
            }
        }

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Views
        rootView = (CoordinatorLayout) findViewById(R.id.layout_group_requests);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout_group_requests);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mTutinder != null && mVolley != null) {
                    requestOpenGroupRequests();
                }
            }
        });

        recyclerViewRequest = (RecyclerView) findViewById(R.id.recycler_group_requests);
        recyclerViewRequest.setLayoutManager(new LinearLayoutManager(GroupRequestListActivity.this));
        requestItemList = new ArrayList<>();

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);

        // API Call
        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                mTutinder = Tutinder.getInstance();
                mVolley = VolleySingleton.getInstance(mContext);

                requestOpenGroupRequests();
            }
        });
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @param groupRequests
     */
    public void onRequestResponse(GroupRequest[] groupRequests) {
        if(groupRequests != null) {
            if(groupRequests.length > 0) {
                requestItemList = new ArrayList<>();
                try {
                    Set<Course> courseset = new HashSet<Course>();
                    for (GroupRequest groupRequest : groupRequests) {
                        if (!courseset.contains(groupRequest.get_courseid())) {
                            courseset.add(groupRequest.get_courseid());
                        }

                    }
                    for (Course c : courseset) {
                        requestItemList.add(new RequestParentListItem(c, new ArrayList<GroupRequest>()));
                    }

                    for (GroupRequest groupRequest : groupRequests) {
                        if (requestItemList.size() > 0) {
                            for (RequestParentListItem listitem : requestItemList) {
                                if (listitem.getCourse().get_id().equals(groupRequest.get_courseid().get_id())) {
                                    listitem.addRequest(groupRequest);
                                }
                            }
                        }
                    }

                    recyclerAdapterRequest = new ExpandableRequestListAdapter(GroupRequestListActivity.this, requestItemList, new OnListItemInteractListener() {
                        @Override
                        public void onListItemInteract(String method, Bundle args) {

                        }

                        @Override
                        public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {
                            if (method.equals(getString(R.string.action_accept))) {
                                String grouprequestId = args.getString("grouprequestid");
                                requestAccept(grouprequestId, (ExpandableRequestListAdapter.RequestViewHolder) viewHolder);
                            } else if (method.equals(getString(R.string.action_deny))) {
                                String grouprequestId = args.getString("grouprequestid");
                                requestDeny(grouprequestId, (ExpandableRequestListAdapter.RequestViewHolder) viewHolder);
                            }
                        }
                    });
                    recyclerViewRequest.setAdapter(recyclerAdapterRequest);
                } catch (Exception e) {
                    // Show error Message
                    Snackbar.make(rootView, R.string.error_loading_group_requests, Snackbar.LENGTH_LONG)
                            .setAction(R.string.btn_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestOpenGroupRequests();
                                }
                            })
                            .show();
                }
            } else {
                // Show emptystate
                Snackbar.make(rootView, mContext.getString(R.string.emptystate_activity_group_request_list), Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        hideLoadingProgressBar();
    }

    /**
     *
     */
    public void requestOpenGroupRequests() {
        final String URL = mVolley.getAPIRoot() + "/courses/grouprequests";
        GsonRequest<GroupRequest[]> groupRequestRequst = new GsonRequest<>(Request.Method.GET, URL, null, GroupRequest[].class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<GroupRequest[]>() {
            @Override
            public void onResponse(GroupRequest[] response) {
                if (response != null) {
                    onRequestResponse(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);

                // Show error Message
                Snackbar.make(rootView, R.string.error_loading_group_requests, Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestOpenGroupRequests();
                            }
                        })
                        .show();
            }
        });
        mVolley.addToRequestQueue(groupRequestRequst);
        showLoadingProgressBar();
    }

    /**
     *
     * @param grouprequestId
     * @param viewHolder
     */
    public void requestAccept(final String grouprequestId, final ExpandableRequestListAdapter.RequestViewHolder viewHolder) {
        JsonObject object = new JsonObject();
        object.addProperty("accept", true);

        final String URL = mVolley.getAPIRoot() + "/courses/grouprequests/" + grouprequestId;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.PUT, URL, object.toString(), mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                viewHolder.hideProgressBar();

                String groupid;
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    groupid = jsonObject.getString("groupid");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                // show accepted group
                Intent intent = new Intent(mContext, GroupActivity.class);
                intent.putExtra("groupid", groupid);
                intent.putExtra("ismember", true);
                mContext.startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                viewHolder.hideProgressBar();

                // Show error Message
                Snackbar.make(rootView, R.string.error_sending_group_accept_request, Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestAccept(grouprequestId, viewHolder);
                            }
                        })
                        .show();
            }
        });
        mVolley.addToRequestQueue(request);
        viewHolder.showProgressBar();
    }

    /**
     *
     * @param grouprequestId
     * @param viewHolder
     */
    public void requestDeny(final String grouprequestId, final ExpandableRequestListAdapter.RequestViewHolder viewHolder) {
        JsonObject object = new JsonObject();
        object.addProperty("accept", false);

        final String URL = mVolley.getAPIRoot() + "/courses/grouprequests/" + grouprequestId;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.PUT, URL, object.toString(), mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                viewHolder.hideProgressBar();

                // Show success Message
                Snackbar.make(rootView, R.string.toast_group_request_denied, Snackbar.LENGTH_SHORT)
                        .show();

                viewHolder.removeItem();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                viewHolder.hideProgressBar();

                // Show error Message
                Snackbar.make(rootView, R.string.error_sending_group_deny_request, Snackbar.LENGTH_LONG)
                        .setAction(R.string.btn_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestDeny(grouprequestId, viewHolder);
                            }
                        })
                        .show();
            }
        });
        mVolley.addToRequestQueue(request);
        viewHolder.showProgressBar();
    }

    /**
     * Shows the loadingProgressBar.
     */
    public void showLoadingProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setEnabled(true);
        }
    }

    /**
     * Hides the loadingProgressBar.
     */
    public void hideLoadingProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
            loadingProgressBar.setEnabled(false);
        }
    }
}
