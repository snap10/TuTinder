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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.CourseActivity;
import tutinder.mad.uulm.de.tutinder.activities.TutinderActivity;
import tutinder.mad.uulm.de.tutinder.adapters.CourseListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

/**
 * Fragment displayed in MainActivity, which holds all Courses in which the logged in User is
 * enrolled.
 *
 * @author 1uk4s
 * @author snap10
 */
public class MyCourseListFragment extends Fragment {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private ProgressBar loadingProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewCourses;
    private CourseListAdapter recyclerAdapterCourses;


    /**
     * Default Constructor.
     */
    public MyCourseListFragment() {
        // do nothing
    }

    /**
     * Returns a new instance of MyCourseListFragment.
     *
     * @return
     */
    public static Fragment newInstance() {
        return new MyCourseListFragment();
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
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(getContext().getApplicationContext());

        // API Call
        requestUser();
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
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_my_course_list, container, false);

        rootView = (CoordinatorLayout) fragmentView.findViewById(R.id.cl_mycourselist);

        loadingProgressBar = (ProgressBar) fragmentView.findViewById(R.id.loading_progressbar);
        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestUser();
            }
        });

        recyclerViewCourses = (RecyclerView) fragmentView.findViewById(R.id.recycler_mycourses);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerAdapterCourses = new CourseListAdapter(mContext, new ArrayList<Course>(), new OnListItemInteractListener() {
            @Override
            public void onListItemInteract(String method, Bundle args) {
                if (method.equals(getString(R.string.action_show_course))) {
                    // Start CourseActivity
                    Intent intentCourse = new Intent(mContext, CourseActivity.class);
                    intentCourse.putExtras(args);
                    startActivity(intentCourse);
                } else if (method.equals(getString(R.string.action_search_for_partners))) {
                    // Start TutinderActivity
                    Intent intent = new Intent(mContext, TutinderActivity.class);
                    intent.putExtras(args);
                    startActivity(intent);
                } else if (method.equals(getString(R.string.action_leave_course))) {
                    showLeaveDialog(args.getString("courseid"), args.getString("coursename"));
                }
            }

            @Override
            public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {

            }
        });
        recyclerViewCourses.setAdapter(recyclerAdapterCourses);

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
                requestUser();
            }
        });
    }

    /**
     * Callback after request Response 200 OK.
     */
    public void onRequestResponse(List<Course> response) {
        if (response != null) {
            recyclerAdapterCourses.setItemList(response);
        } else {
            recyclerAdapterCourses.setItemList(new ArrayList<Course>());
            recyclerViewCourses.setAdapter(recyclerAdapterCourses);
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        hideLoadingProgressBar();
    }

    /**
     * Makes an API Call to load all Courses of the logged in User.
     */
    public void requestUser() {
        final String URL = mVolley.getAPIRoot() + "/user/";
        GsonRequest<User> request = new GsonRequest<User>(Request.Method.GET, URL, null, User.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                onRequestResponse(response.getCourses());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);
                // Show error message
                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                Snackbar.make(viewPager, getString(R.string.error_loading_course_list), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestUser();
                            }
                        })
                        .show();
            }
        });
        showLoadingProgressBar();
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes an API Call to leave the Course with the specified courseId.
     *
     * @param courseId
     */
    private void requestLeaveCourse(final String courseId) {
        final String URL = mVolley.getAPIRoot() + "/user/courses/" + courseId + "/leave/";
        GsonRequest<User> requestCourses = new GsonRequest<User>(Request.Method.POST, URL, null, User.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User user) {
                user.setPassword(mTutinder.getLoggedInUser().getPassword());
                mTutinder.setLoggedInUser(user);

                onRequestResponse(mTutinder.getLoggedInUser().getCourses());

                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                Snackbar.make(viewPager, getString(R.string.toast_leave_course_succes), Snackbar.LENGTH_LONG)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();

                ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                Snackbar.make(viewPager, getString(R.string.error_sending_request), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestLeaveCourse(courseId);
                            }
                        })
                        .show();
            }
        });

        requestCourses.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        showLoadingProgressBar();
        mVolley.addToRequestQueue(requestCourses);
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
     * Shows the Leave Group Confirm Dialog.
     */
    private void showLeaveDialog(final String courseId, String courseName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.TuTinder_Dialog_Progress);
        builder.setTitle(courseName)
                .setMessage(R.string.dialog_message_leave)
                .setPositiveButton(R.string.btn_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        requestLeaveCourse(courseId);
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
