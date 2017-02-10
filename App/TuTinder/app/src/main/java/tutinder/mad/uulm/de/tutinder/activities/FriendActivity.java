package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.ChipListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.FriendCourseListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.PicturePagerAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;


/**
 * @author Snap10
 * @author 1uk4s
 */
public class FriendActivity extends AppCompatActivity {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private AppBarLayout layoutAppBar;
    private CollapsingToolbarLayout layoutCollapsingToolbar;
    private CircleNetworkImageView ivProfilepictureThumb;
    private Toolbar toolbar;
    private ProgressBar loadingProgressBar;
    private ViewPager vpPictures;
    private CircleNetworkImageView[] thumbs;
    private TextView tvName, tvStudycourse, tvDescription, tvEmail, tvPhone, tvCourseInfo;

    private ChipListAdapter recyclerAdapterTags;
    private RecyclerView recyclerViewTags;

    private FriendCourseListAdapter recyclerAdapterCourses;
    private RecyclerView recyclerViewCourses;


    private String bundleUserId;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // Application Context
        mContext = this;
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContext);

        // Arguments
        Bundle args = getIntent().getExtras();
        Bundle serverdata = getIntent().getBundleExtra(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
        //If serverdata was set we klicked on Notification
        if (serverdata != null) args = serverdata;
        //If Serverdata was null we came from Matchlist
        if (args != null) {
            if (args.containsKey("friendid")) {
                bundleUserId = args.getString("friendid");
            } else {
                Toast.makeText(this, getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                finish();
            }
        }

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        rootView = (CoordinatorLayout) findViewById(R.id.cl_friend);

        layoutAppBar = (AppBarLayout) findViewById(R.id.layout_appbar);
        layoutAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (layoutCollapsingToolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(layoutCollapsingToolbar)) {
                    ivProfilepictureThumb.setVisibility(View.VISIBLE);
                } else {
                    ivProfilepictureThumb.setVisibility(View.INVISIBLE);
                }
            }
        });
        layoutCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsingtoolbar);
        ivProfilepictureThumb = (CircleNetworkImageView) findViewById(R.id.iv_thumbprofilepicture);
        ivProfilepictureThumb.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        ivProfilepictureThumb.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        vpPictures = (ViewPager) findViewById(R.id.vp_pictures);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);


        CircleNetworkImageView ivPicture1 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture1);
        CircleNetworkImageView ivPicture2 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture2);
        CircleNetworkImageView ivPicture3 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture3);
        CircleNetworkImageView ivPicture4 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture4);
        CircleNetworkImageView ivPicture5 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture5);
        thumbs = new CircleNetworkImageView[]{ivPicture1, ivPicture2, ivPicture3, ivPicture4, ivPicture5};
        for (CircleNetworkImageView thumb : thumbs) {
            thumb.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            thumb.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        }

        tvName = (TextView) findViewById(R.id.tv_username);
        tvStudycourse = (TextView) findViewById(R.id.tv_studycourse);
        tvDescription = (TextView) findViewById(R.id.tv_description);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvCourseInfo = (TextView) findViewById(R.id.tv_courseinfo);

        recyclerAdapterTags = new ChipListAdapter(this, new ArrayList<String>());
        recyclerViewTags = (RecyclerView) findViewById(R.id.recycler_tags);
        recyclerViewTags.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewTags.setAdapter(recyclerAdapterTags);

        recyclerAdapterCourses = new FriendCourseListAdapter(FriendActivity.this, new ArrayList<Course>(), new OnListItemInteractListener() {
            @Override
            public void onListItemInteract(String method, Bundle args) {
                if (method.equals(getString(R.string.action_show_course))) {
                    Intent intentCourse = new Intent(FriendActivity.this, CourseActivity.class);
                    intentCourse.putExtras(args);
                    startActivity(intentCourse);
                } else if (method.equals(getString(R.string.action_send_group_request))) {
                    requestGroup(bundleUserId, args.getString("courseid"), args.getString("coursename"));
                } else if (method.equals(getString(R.string.action_enroll_in_course))) {
                    showEnrollDialog(args.getString("courseid"));
                }
            }

            @Override
            public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {

            }
        });
        recyclerViewCourses = (RecyclerView) findViewById(R.id.recycler_course);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerViewCourses.setAdapter(recyclerAdapterCourses);

        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                mTutinder = Tutinder.getInstance();
                // API Call
                requestUser(bundleUserId);

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

    /**
     * Creates a OptionMenu for the Activities Toolbar.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_friend_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Callback gor OptionMenu Item clicks.
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
        if (id == R.id.action_removefriend) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TuTinder_Dialog_Alert);
            builder.setTitle(R.string.dialog_title_removefriend)
                    .setMessage(R.string.dialog_message_removefriend)
                    .setPositiveButton(R.string.btn_remove, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestRemoveFriend();
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
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback after request Response 200 OK.
     *
     * @param response
     */
    public void onRequestResponse(User response) {
        user = response;

        // set toolbar title
        layoutCollapsingToolbar.setTitle(user.getName());

        // set viewpager pictures
        String[] pathPictures = user.getCloudinarypicturepaths(false);
        //Workaround to have defaultImage shown
        if (pathPictures.length == 0) pathPictures = new String[]{"default"};
        vpPictures.setAdapter(new PicturePagerAdapter(mContext, pathPictures, R.drawable.ic_placeholder_profilepicture_grey_500dp));
        vpPictures.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                for (CircleNetworkImageView thumb : thumbs) {
                    thumb.setBorderWidth(0);
                }
                thumbs[position].setBorderWidth(4);
                thumbs[position].setBorderColorResource(R.color.accent);
                ivProfilepictureThumb.setImageUrl((String) thumbs[position].getTag(), mVolley.getImageLoader());
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // Set Startpicture
        for (int i = 0; i < pathPictures.length; i++) {
            if (pathPictures[i].equals(user.getProfilepicturepath())) {
                vpPictures.setCurrentItem(i);
            }
        }
        // Set Thumbnails
        String[] pathThumbs = user.getCloudinarypicturepaths(true);
        for (int i = 0; i < pathThumbs.length; i++) {
            final int position = i;
            // set thumbnail urls
            thumbs[i].setImageUrl(pathThumbs[i], mVolley.getImageLoader());
            thumbs[i].setTag(pathThumbs[i]);
            // set thumbnail click listeners
            thumbs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    CircleNetworkImageView thumb = (CircleNetworkImageView) v;
                    vpPictures.setCurrentItem(position);
                }
            });
        }

        // Set textviews
        String name = user.getName();
        if (name != null) tvName.setText(name);
        String studycourse = user.getStudycourse();
        if (studycourse != null) tvStudycourse.setText(studycourse);
        String description = user.getDescription();
        if (description != null) tvDescription.setText(description);
        String email = user.getEmail();
        if (email != null) tvEmail.setText(email);
        String phone = user.getPhone();
        if (phone != null) tvPhone.setText(phone);

        // Set Courses
        List<Course> courseList = user.getCourses();
        if (!courseList.isEmpty()) {
            recyclerAdapterCourses.setItemList(courseList);
            tvCourseInfo.setVisibility(View.GONE);
            recyclerViewCourses.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCourses.setVisibility(View.GONE);
            tvCourseInfo.setVisibility(View.VISIBLE);
        }

        // Set Tags
        for (String tagId : user.getPersonalitytags()) {
            requestTags(tagId);
        }
    }

    /**
     * Makes a api call to delete the selected friend.
     */
    public void requestRemoveFriend() {
        final String URL = mVolley.getAPIRoot() + "/user/friends/" + bundleUserId;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoadingProgressBar();

                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                // Show error Message
                Snackbar.make(rootView, getString(R.string.error_sending_request), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestRemoveFriend();
                            }
                        })
                        .show();
            }
        });
        showLoadingProgressBar();
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes an API call to load a User with the given bundleUserId.
     */
    private void requestUser(final String userid) {
        final String URL = mVolley.getAPIRoot() + "/user/friends/" + userid;
        GsonRequest<User> requestCourse = new GsonRequest<User>(Request.Method.GET, URL, null, User.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                hideLoadingProgressBar();

                onRequestResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();

                if (error.networkResponse.statusCode == 404) {
                    Toast.makeText(mContext, getString(R.string.error_loading_user), Toast.LENGTH_SHORT);
                    finish();
                } else {
                    Snackbar.make(rootView, getString(R.string.error_loading_user), Snackbar.LENGTH_INDEFINITE)
                            .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestUser(userid);
                                }
                            })
                            .show();
                }

            }
        });
        mVolley.addToRequestQueue(requestCourse);
    }

    /**
     * Makes an API call to load a PersonalityTag with the given tagId.
     *
     * @param tagId
     */
    public void requestTags(final String tagId) {
        final String URL = mVolley.getAPIRoot() + "/variables/tags/personality/" + tagId;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, URL, null, null, new Response.Listener<String>() {
            @Override
            public void onResponse(String tags) {
                try {
                    if (tags == null) {
                        throw new Resources.NotFoundException("No Tags were found on the Server");
                    } else {
                        String tagName = null;
                        JSONArray tagsJson = new JSONArray(tags);
                        JSONArray tag = tagsJson.getJSONObject(0).getJSONArray("tags").getJSONObject(0).getJSONArray("tag");

                        for (int i = 0; i < tag.length(); i++) {
                            JSONObject tagObject = tag.getJSONObject(i);
                            if (tagObject.getString("lang").equals(Locale.getDefault().getLanguage().toUpperCase())) {
                                tagName = tagObject.getString("value");
                            }
                        }

                        if (tagName != null) {
                            recyclerAdapterTags.updateItemList(tagName);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    finish();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();

                // Show error Message
                Snackbar.make(rootView, getString(R.string.error_tags_could_not_be_loaded), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                for (String tagId : user.getPersonalitytags()) {
                                    requestTags(tagId);
                                }
                            }
                        })
                        .show();
            }
        });
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes an API call to send a Group Request with the given userId and courseId.
     */
    public void requestGroup(final String userId, final String courseId, final String courseName) {
        final String URL = mVolley.getAPIRoot() + "/courses/" + courseId + "/users/" + userId + "/grouprequest";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.POST, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoadingProgressBar();

                Snackbar.make(rootView, getString(R.string.toast_group_request_succeded), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();

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
                                    requestGroup(userId, courseId, courseName);
                                }
                            })
                            .show();
                }

            }
        });
        showLoadingProgressBar();
        mVolley.addToRequestQueue(request);
    }

    /**
     * @param courseId
     */
    public void requestEnroll(final String courseId) {
        final String URL = mVolley.getAPIRoot() + "/user/courses/" + courseId + "/enroll/";
        GsonRequest<User> request = new GsonRequest<User>(Request.Method.POST, URL, null, User.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User user) {
                hideLoadingProgressBar();

                user.setPassword(mTutinder.getLoggedInUser().getPassword());
                mTutinder.setLoggedInUser(user);

                Intent intent = new Intent(FriendActivity.this, CourseActivity.class);
                intent.putExtra("courseid", courseId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgressBar();
                if (error.networkResponse != null) {
                    Toast.makeText(mContext, "" + error.networkResponse.statusCode + " " + mContext.getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, R.string.strangeError, Toast.LENGTH_LONG).show();
                }
            }
        });
        showLoadingProgressBar();
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
     * Confirm Dialog for enrolling Courses.
     *
     * @param courseId
     */
    private void showEnrollDialog(final String courseId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.TuTinder_Dialog_Progress);
        builder
                .setTitle(R.string.dialog_title_enroll)
                .setMessage(R.string.dialog_message_enroll)
                // .setIcon(R.drawable.ic_logout_accent_24dp)
                .setPositiveButton(R.string.btn_enroll, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestEnroll(courseId);
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