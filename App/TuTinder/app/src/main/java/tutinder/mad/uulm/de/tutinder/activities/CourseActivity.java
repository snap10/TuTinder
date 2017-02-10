package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
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
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.CourseFriendListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Timeslot;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * CourseActivity: Shows more Details about a specific Course.
 *
 * @author 1uk4s
 * @author snap10
 */
public class CourseActivity extends AppCompatActivity {

    public static final String SHARED_PREFS_COURSE_TIMESLOTS = "shared_prefs_course_timeslots";

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private String bundleCourseId;
    private boolean bundleIsInitial;
    private Course course;

    private Toolbar toolbar;
    private CoordinatorLayout rootView;
    private CollapsingToolbarLayout layoutCollapsingToolbar;
    private AppBarLayout layoutAppBar;
    private FloatingActionButton btnFloating;

    private NetworkImageView ivCoursepicture;
    private CircleNetworkImageView ivCoursepictureThumb;

    private ProgressBar loadingProgressBar, friendsProgressBar;

    private TextView tvCoursename, tvTerm, tvDescription, tvInstitute, tvLecturer, tvUsers, tvGroups, tvGroupsHint, tvFriends;

    private RecyclerView recyclerViewFriends;
    private CourseFriendListAdapter recyclerAdapterFriends;

    private Timeslot[] courseTimeslots;
    private boolean isTimeslotArrayLoaded;
    private HashSet<String> isTimeslotSelected;


    /**
     * Initialises the Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        // Application References
        mContext = getApplicationContext();
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContext);

        // Arguments
        Bundle args = getIntent().getExtras();
        bundleCourseId = args.getString("courseid");
        bundleIsInitial = args.getBoolean("isinitial");

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        rootView = (CoordinatorLayout) findViewById(R.id.layout_coordinator_course);
        layoutAppBar = (AppBarLayout) findViewById(R.id.layout_appbar);
        layoutAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (layoutCollapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(layoutCollapsingToolbar)) {
                    ivCoursepictureThumb.setVisibility(View.VISIBLE);
                } else {
                    ivCoursepictureThumb.setVisibility(View.INVISIBLE);
                }
            }
        });
        layoutCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsingtoolbar);

        ivCoursepicture = (NetworkImageView) findViewById(R.id.iv_coursepicture);
        ivCoursepicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_500dp);
        ivCoursepicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_500dp);

        ivCoursepictureThumb = (CircleNetworkImageView) findViewById(R.id.iv_thumbcoursepicture);
        ivCoursepictureThumb.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
        ivCoursepictureThumb.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        friendsProgressBar = (ProgressBar) findViewById(R.id.friends_loading_progressbar);

        tvCoursename = (TextView) findViewById(R.id.tv_coursename);
        tvTerm = (TextView) findViewById(R.id.tv_term);
        tvDescription = (TextView) findViewById(R.id.tv_description);
        tvInstitute = (TextView) findViewById(R.id.tv_institute);
        tvLecturer = (TextView) findViewById(R.id.tv_lecturer);
        tvUsers = (TextView) findViewById(R.id.tv_users);
        tvGroups = (TextView) findViewById(R.id.tv_groups);
        tvGroupsHint = (TextView) findViewById(R.id.tv_hint_groups);
        tvFriends = (TextView) findViewById(R.id.tv_friends);

        recyclerViewFriends = (RecyclerView) findViewById(R.id.recycler_friend);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerAdapterFriends = new CourseFriendListAdapter(CourseActivity.this, new ArrayList<User>(), new OnListItemInteractListener() {
            @Override
            public void onListItemInteract(String method, Bundle args) {
                if (method.equals(getString(R.string.action_show_profile))) {
                    Intent intent = new Intent(CourseActivity.this, FriendActivity.class);
                    intent.putExtras(args);
                    startActivity(intent);
                }
            }

            @Override
            public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {
                if (method.equals(getString(R.string.action_send_group_request))) {
                    String userId = args.getString("userid");
                    String courseId = course.get_id();
                    requestGroup(userId, courseId, (CourseFriendListAdapter.FriendViewHolder) viewHolder);
                }
            }
        });
        recyclerViewFriends.setAdapter(recyclerAdapterFriends);

        btnFloating = (FloatingActionButton) findViewById(R.id.btn_floating);
        btnFloating.setEnabled(false);

        // API Call
        isTimeslotArrayLoaded = false;
        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                mTutinder = Tutinder.getInstance();
                requestCourse();
                requestFriends();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                mTutinder = Tutinder.getInstance();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_course_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.action_timeslot_settings) {
            if (courseTimeslots != null && courseTimeslots.length > 0) {
                showTimeslotSettingsDialog(courseTimeslots);
            } else {
                Snackbar.make(rootView, R.string.noTimeslotsInCourse, Snackbar.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if Timeslots are selected and sends selected Timeslots to Server.
     */
    public void onTimeslotsSettingsResponse() {
        String[] timeslots = isTimeslotSelected.toArray(new String[isTimeslotSelected.size()]);
        requestSaveTimeslots(timeslots);
    }

    /**
     * This callback will be called after the API-call responded with 200 OK.
     *
     * @param response
     */
    public void onRequestResponse(Course response) {
        if (response != null) {
            course = response;

            requestGroupQuantity(course.getId());

            layoutCollapsingToolbar.setTitle(course.getName());

            ivCoursepicture.setImageUrl(course.getPicturepath(mVolley.getAPIRoot()), mVolley.getImageLoader());
            ivCoursepictureThumb.setImageUrl(course.getThumbnailpath(), mVolley.getImageLoader());

            String coursename = course.getName();
            if (coursename != null) tvCoursename.setText(coursename);
            String term = course.getTerm();
            if (term != null) tvTerm.setText(term);
            String description = course.getDescription();
            if (description != null) tvDescription.setText(description);
            String institute = course.getFaculty().getInstitute().getName();
            if (institute != null) tvInstitute.setText(institute);
            String lecturer = course.getLecturer();
            if (lecturer != null) tvLecturer.setText(lecturer);
            String users = "" + course.getEnrolledusers().length;
            if (users != null) tvUsers.setText(users);

            String groupsHint = tvGroupsHint.getText() + " " + getString(R.string.hint_max_group_size) + " " + course.getMaxgroupsize();
            if (groupsHint != null) tvGroupsHint.setText(groupsHint);

            btnFloating.setEnabled(true);
            btnFloating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CourseActivity.this, TutinderActivity.class);
                    intent.putExtra("courseid", bundleCourseId);
                    intent.putExtra("coursename", course.getName());
                    intent.putExtra("coursethumbnailpath", course.getThumbnailpath());
                    startActivity(intent);
                }
            });

            course.getTimeslotObjects(mVolley, mTutinder, mContext, new Course.CourseTimeslotListener() {
                @Override
                public void onTimeslotObjectsLoaded(Timeslot[] timelostobjects) {
                    if (timelostobjects != null) {
                        courseTimeslots = timelostobjects;
                        isTimeslotArrayLoaded = true;
                        if (bundleIsInitial) {
                            showTimeslotSettingsDialog(courseTimeslots);
                        }
                    }
                }
            });
        } else {
            // Show error Message
            Toast.makeText(mContext, getString(R.string.error_loading_course), Toast.LENGTH_SHORT).show();
            finish();
        }

        hideProgressBar(loadingProgressBar);

    }

    /**
     * Makes an API call to load a course with the given bundleCourseId.
     */
    private void requestCourse() {
        final String URL = mVolley.getAPIRoot() + "/courses/" + bundleCourseId;
        GsonRequest<Course> requestCourse = new GsonRequest<Course>(Request.Method.GET, URL, null, Course.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course>() {
            @Override
            public void onResponse(Course response) {
                onRequestResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);
            }
        });

        showProgressBar(loadingProgressBar);
        mVolley.addToRequestQueue(requestCourse);
    }

    /**
     * @param courseID
     */
    public void requestGroupQuantity(String courseID) {
        final String URL = mVolley.getAPIRoot() + "/courses/" + courseID + "/groupquantity";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response == null) {
                        throw new Resources.NotFoundException("No GroupQuantity could be found");
                    } else {
                        JSONObject jsonObject = new JSONObject(response);
                        int groupQuantity = jsonObject.getInt("groupquantity");

                        String groups = "" + groupQuantity;
                        if (groups != null) tvGroups.setText(groups);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                hideProgressBar(loadingProgressBar);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);
            }
        });
        showProgressBar(loadingProgressBar);
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes an API call to load all Friends, which are enrolled in this Course.
     */
    private void requestFriends() {
        final String URL = mVolley.getAPIRoot() + "/user/friends";

        final GsonRequest<User[]> requestFriends = new GsonRequest<User[]>(Request.Method.GET, URL, null, User[].class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User[]>() {
            @Override
            public void onResponse(User[] friends) {
                if (friends != null && friends.length > 0) {
                    ArrayList<User> friendList = new ArrayList<>();
                    //Filter Friends for courseid
                    for (User friend : friends) {
                        if (friend.isEnrolledInCourse(bundleCourseId)) {
                            friendList.add(friend);
                        }
                    }
                    if (friendList.size() > 0) {
                        recyclerAdapterFriends.setItemList(friendList);
                        recyclerViewFriends.setVisibility(View.VISIBLE);
                        tvFriends.setVisibility(View.GONE);
                    } else {
                        recyclerViewFriends.setVisibility(View.GONE);
                        tvFriends.setVisibility(View.VISIBLE);
                    }
                } else {
                    recyclerViewFriends.setVisibility(View.GONE);
                    tvFriends.setVisibility(View.VISIBLE);
                }

                hideProgressBar(friendsProgressBar);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show error Message
                Snackbar.make(rootView, getString(R.string.error_loading_users), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestFriends();
                            }
                        })
                        .show();
            }
        });

        showProgressBar(friendsProgressBar);
        mVolley.addToRequestQueue(requestFriends);
    }

    /**
     * Makes an API call to send a Group Request to the User with the given userId.
     *
     * @param userId
     * @param courseId
     * @param viewHolder
     */
    private void requestGroup(final String userId, final String courseId, final CourseFriendListAdapter.FriendViewHolder viewHolder) {
        final String URL = mVolley.getAPIRoot() + "/courses/" + courseId + "/users/" + userId + "/grouprequest";

        CustomJsonRequest request = new CustomJsonRequest(Request.Method.POST, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Snackbar.make(rootView, getString(R.string.toast_group_request_succeded), Snackbar.LENGTH_SHORT)
                        .show();

                viewHolder.hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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

                viewHolder.hideProgressBar();
            }
        });

        viewHolder.showProgressBar();
        mVolley.addToRequestQueue(request);
    }

    public void requestSaveTimeslots(final String[] timeslots) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray(timeslots);
            jsonObject.put("timeslots", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        final String URL = mVolley.getAPIRoot() + "/user/courses/" + course.get_id() + "/timeslots";

        CustomJsonRequest request = new CustomJsonRequest(Request.Method.PUT, URL, jsonObject.toString(), mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgressBar(loadingProgressBar);

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    String[] chosenTimeslots = new String[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        chosenTimeslots[i] = jsonArray.getString(i);
                    }
                    mTutinder.getLoggedInUser().getCourse(course.getId()).setChosentimeslots(chosenTimeslots);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Snackbar.make(rootView, getString(R.string.toast_timeslots_save_succeded), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressBar(loadingProgressBar);

                // Show error Message
                Snackbar.make(rootView, getString(R.string.error_sending_request), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestSaveTimeslots(timeslots);
                            }
                        })
                        .show();
            }
        });
        showProgressBar(loadingProgressBar);
        mVolley.addToRequestQueue(request);
    }

    /**
     * Shows the activities loading ProgressDialog.
     */
    private void showProgressBar(ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setEnabled(true);
        }
    }

    /**
     * Hides the activities loading ProgressDialog if its showing.
     */
    private void hideProgressBar(ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setEnabled(false);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Shows a dialog, in which the Timeslots can be selected.
     *
     * @param allTimeslots
     */
    private void showTimeslotSettingsDialog(final Timeslot[] allTimeslots) {
        if (isTimeslotArrayLoaded) {
            String[] chosenTimeslots = mTutinder.getLoggedInUser().getCourse(course.getId()).getChosentimeslots();

            isTimeslotSelected = new HashSet<String>();
            isTimeslotSelected.addAll(Arrays.asList(chosenTimeslots));

            CharSequence[] timeslots = new CharSequence[allTimeslots.length];
            for (int i = 0; i < allTimeslots.length; i++) {
                timeslots[i] = allTimeslots[i].getFormattedTimeslot(mContext);
            }

            boolean[] isChecked = new boolean[allTimeslots.length];
            for (int j = 0; j < allTimeslots.length; j++) {
                for (int k = 0; k < chosenTimeslots.length; k++) {
                    if (allTimeslots[j].get_id().equals(chosenTimeslots[k])) {
                        isChecked[j] = true;
                        break;
                    } else {
                        isChecked[j] = false;
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this, R.style.TuTinder_Dialog_Alert)
                    .setTitle(getString(R.string.dialog_title_timeslot_settings))
                    .setMultiChoiceItems(timeslots, isChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (!isChecked && isTimeslotSelected.contains(allTimeslots[which].get_id())) {
                                isTimeslotSelected.remove(allTimeslots[which].get_id());
                            } else {
                                isTimeslotSelected.add(allTimeslots[which].get_id());
                            }

                        }
                    })
                    .setPositiveButton(getString(R.string.btn_save), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onTimeslotsSettingsResponse();
                        }
                    })
                    .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}

