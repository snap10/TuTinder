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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.ExpandableMatchListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.customItems.MatchParentListItem;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Matches;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

public class MatchListActivity extends AppCompatActivity {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewMatches;
    private ExpandableMatchListAdapter recyclerAdapterMatches;
    private List<MatchParentListItem> matchItemList;
    private ProgressBar loadingProgressBar;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

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
        rootView = (CoordinatorLayout) findViewById(R.id.cl_matchlist);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout_matches);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestMatches();
            }
        });

        recyclerViewMatches = (RecyclerView) findViewById(R.id.recycler_matches);
        recyclerViewMatches.setLayoutManager(new LinearLayoutManager(mContext));
        matchItemList = new ArrayList<>();

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);

        LoginChecker.logInCurrentUser(mContext, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                if (status == LoginChecker.SUCCESS) {
                    requestMatches();
                } else {
                    Intent intent = new Intent(MatchListActivity.this, MainActivity.class);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                }
            }
        });
    }

    /**
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
     * This callback will be called after the API-call responded with 200 OK.
     *
     * @param response
     */
    public void onRequestResponse(Matches[] response) {
        if (response != null) {
            if(response.length > 0) {
                try {
                    matchItemList = new ArrayList<>();
                    for (Matches matches : response) {
                        if (matches.get_groupid() != null) {
                            matchItemList.add(new MatchParentListItem(matches.get_courseid(), matches.get_groupid(), matches.getMatches(), matches.getMatchedgroups()));
                        } else if (matches.get_userid() != null) {
                            matchItemList.add(new MatchParentListItem(matches.get_courseid(), matches.getMatches(), matches.getMatchedgroups()));
                        }
                    }
                    recyclerAdapterMatches = new ExpandableMatchListAdapter(this, matchItemList, new OnListItemInteractListener() {
                        @Override
                        public void onListItemInteract(String method, Bundle args) {

                        }

                        @Override
                        public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {
                            if (method.equals(getString(R.string.action_send_group_request))) {
                                String userId = args.getString("userid");
                                String courseId = args.getString("courseid");
                                requestGroup(userId, courseId, (ExpandableMatchListAdapter.RatingViewHolder) viewHolder);
                            }
                        }
                    });
                    recyclerViewMatches.setAdapter(recyclerAdapterMatches);
                } catch (Exception e) {
                    // Show error Message
                    Snackbar.make(rootView, getString(R.string.error_loading_matches), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestMatches();
                                }
                            })
                            .show();
                }
            } else {
                // Show emptystate Message
                Snackbar.make(rootView, getString(R.string.emptystate_activity_matchlist), Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        } else {
            // Show error Message
            Snackbar.make(rootView, getString(R.string.error_loading_matches), Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestMatches();
                        }
                    })
                    .show();
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        hideLoadingProgressBar();
    }

    /**
     * Loads all matches of the logged in user.
     */
    public void requestMatches() {
        final String URL = mVolley.getAPIRoot() + "/matching/";
        GsonRequest<Matches[]> matchGsonRequest = new GsonRequest<>(Request.Method.GET, URL, null, Matches[].class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<Matches[]>() {
            @Override
            public void onResponse(Matches[] response) {
                onRequestResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);
            }
        });
        mVolley.addToRequestQueue(matchGsonRequest);
        showLoadingProgressBar();
    }

    /**
     * Makes an API call to send a group request with the given userId.
     */
    public void requestGroup(final String userId, final String courseId, final ExpandableMatchListAdapter.RatingViewHolder viewHolder) throws InvalidParameterException {
        if (userId == null) throw new InvalidParameterException("Userid was null");
        if (courseId == null) throw new InvalidParameterException("Courseid was null");

        final String URL = mVolley.getAPIRoot() + "/courses/" + courseId + "/users/" + userId + "/grouprequest";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.POST, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                viewHolder.hideProgressBar();

                // Show success Message
                Snackbar.make(rootView, mContext.getString(R.string.toast_group_request_succeded), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                viewHolder.hideProgressBar();

                if(error.networkResponse.statusCode == 409) {
                    // Show error Message
                    Snackbar.make(rootView, getString(R.string.error_user_already_in_group), Snackbar.LENGTH_LONG)
                            .show();
                } else if(error.networkResponse.statusCode == 412){
                    // Show error Message
                    Snackbar.make(rootView, mContext.getString(R.string.error_group_already_full), Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    // Show error Message
                    Snackbar.make(rootView, mContext.getString(R.string.error_sending_group_request), Snackbar.LENGTH_LONG)
                            .setAction(mContext.getString(R.string.btn_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestGroup(userId, courseId, viewHolder);
                                }
                            })
                            .show();
                }


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
