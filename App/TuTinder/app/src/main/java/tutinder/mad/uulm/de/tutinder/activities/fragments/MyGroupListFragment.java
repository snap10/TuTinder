package tutinder.mad.uulm.de.tutinder.activities.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.GroupActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupChatActivity;
import tutinder.mad.uulm.de.tutinder.adapters.GroupListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

/**
 * Fragment displayed in MainActivity, which holds all Groups in which the logged in User is Member.
 *
 * @author 1uk4s
 * @author snap10
 */
public class MyGroupListFragment extends Fragment {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private ProgressBar loadingProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewGroups;
    private GroupListAdapter recyclerAdapterGroups;


    /**
     * Default Constructor.
     */
    public MyGroupListFragment() {
        // do nothing
    }

    /**
     * Returns a new instance of MyGroupListFragment.
     *
     * @return
     */
    public static Fragment newInstance() {
        return new MyGroupListFragment();
    }

    /**
     * Initialises the Fragment.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();

        mVolley = VolleySingleton.getInstance(mContext);

        LoginChecker.logInCurrentUser(getContext(), new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                mTutinder = Tutinder.getInstance();
                // API Call
                requestGroups();
            }
        });
    }

    /**
     * Creates the Fragments View.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_my_group_list, container, false);

        rootView = (CoordinatorLayout) fragmentView.findViewById(R.id.cl_mygrouplist);

        loadingProgressBar = (ProgressBar) fragmentView.findViewById(R.id.loading_progressbar);
        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestGroups();
            }
        });

        recyclerViewGroups = (RecyclerView) fragmentView.findViewById(R.id.recycler_mygrouplist);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerAdapterGroups = new GroupListAdapter(mContext, new ArrayList<Group>(), new OnListItemInteractListener() {
            @Override
            public void onListItemInteract(String method, Bundle args) {
                if (method.equals(getString(R.string.action_show_group))) {
                    // Start GroupActivity
                    Intent intentGroup = new Intent(mContext, GroupActivity.class);
                    intentGroup.putExtras(args);
                    startActivity(intentGroup);
                } else if (method.equals(getString(R.string.action_send_messages))) {
                    // Start GroupChatActivity
                    Intent intentMessages = new Intent(mContext, GroupChatActivity.class);
                    intentMessages.putExtras(args);
                    startActivity(intentMessages);
                } else if (method.equals(getString(R.string.action_leave_group))) {
                    // Send Group Leave Request
                    showLeaveDialog(args.getString("groupid"));
                }
            }

            @Override
            public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {

            }
        });
        recyclerViewGroups.setAdapter(recyclerAdapterGroups);

        return fragmentView;
    }

    /**
     * Reloads courses from Server.
     */
    @Override
    public void onResume() {
        super.onResume();

        mVolley = VolleySingleton.getInstance(getContext().getApplicationContext());
        LoginChecker.logInCurrentUser(getContext(), new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                mTutinder = Tutinder.getInstance();
                mVolley = VolleySingleton.getInstance(getContext().getApplicationContext());

            }
        });
    }

    /**
     * Callback after request Response 200 OK.
     */
    public void onRequestResponse(List<Group> response) {
        if (response != null) {
            recyclerAdapterGroups.setItemList(response);
        } else {
            recyclerAdapterGroups.setItemList(new ArrayList<Group>());
            recyclerViewGroups.setAdapter(recyclerAdapterGroups);
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        hideLoadingProgressBar();
    }

    /**
     * Makes an API Call to load all Groups of the logged in User.
     */
    public void requestGroups() {
        GsonRequest<Group[]> groupRequest = new GsonRequest<Group[]>(Request.Method.GET, mVolley.getAPIRoot() + "/user/groups", null, Group[].class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] response) {
                if (response != null) {
                    onRequestResponse(Arrays.asList(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);
                // Show error message
                Snackbar.make(rootView, getString(R.string.error_loading_groups), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestGroups();
                            }
                        })
                        .show();
            }
        });
        showLoadingProgressBar();
        mVolley.addToRequestQueue(groupRequest);
    }

    /**
     * Makes an API Call to leave the Group with the specified groupId.
     *
     * @param groupId
     */
    public void requestLeaveGroup(final String groupId) {
        final String URL = mVolley.getAPIRoot() + "/user/groups/" + groupId;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoadingProgressBar();

                requestGroups();

                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                Snackbar.make(viewPager, getString(R.string.toast_leave_group_succes), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();

                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                Snackbar.make(viewPager, getString(R.string.error_sending_leave_group_request), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestLeaveGroup(groupId);
                            }
                        })
                        .show();
            }
        }

        );
        showLoadingProgressBar();
        mVolley.addToRequestQueue(request);
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

    /**
     * Confirm Dialog for leaving Groups.
     *
     * @param groupId
     */
    private void showLeaveDialog(final String groupId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.TuTinder_Dialog_Alert);
        builder.setTitle(R.string.dialog_title_leave_group)
                .setMessage(R.string.dialog_message_leave_group)
                .setPositiveButton(R.string.btn_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestLeaveGroup(groupId);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
