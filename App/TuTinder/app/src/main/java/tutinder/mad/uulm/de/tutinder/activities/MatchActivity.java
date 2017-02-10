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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.ChipListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.PicturePagerAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Timeslot;
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
public class MatchActivity extends AppCompatActivity {

    private Context mContext;
    private Tutinder mTutinder = Tutinder.getInstance();
    private VolleySingleton mVolley;

    private String userId;
    private String courseId;
    private User user;
    private boolean isMatch;

    // general
    private Toolbar toolbar;
    private CoordinatorLayout rootView;
    private CollapsingToolbarLayout layoutCollapsingToolbar;
    private AppBarLayout layoutAppBar;

    // pictures
    private HorizontalScrollView svThumbs;
    private ViewPager vpPictures;
    private CircleNetworkImageView ivProfilepictureThumb;
    private CircleNetworkImageView[] thumbs;

    // tags
    ChipListAdapter recyclerAdapter;
    RecyclerView recyclerTags;
    List<String> tagList;

    private TextView tvName, tvStudycourse, tvDescription;
    private FloatingActionButton btnFloating;
    private ProgressBar loadingProgressBar;
    private RecyclerView recyclerViewTimeslots;
    private ChipListAdapter recyclerAdapterTimeslots;
    private Timeslot[] timeslotobjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        /*
            Get the Application References
        */
        mContext = getApplicationContext();

        mVolley = VolleySingleton.getInstance(mContext);
        /*
            Arguments
         */
        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        Bundle serverdata = intent.getBundleExtra("serverdata");
        //If serverdata was set we klicked on Notification
        if (serverdata != null) args = serverdata;
        //If Serverdata was null we came from Matchlist
        if (args != null) {
            if (args.containsKey("matchedid")) {
                userId = args.getString("matchedid");
                isMatch = true;
            } else {
                isMatch = false;
            }
            if (args.containsKey("userid")) {
                userId = args.getString("userid");
            }
            if (args.containsKey("courseid")) {
                courseId = args.getString("courseid");
            }
            if (args.containsKey(GCMNotificationService.BUNDLE_NAME_SERVERDATA)) {
                Bundle serverBundle = args.getBundle(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
                if (serverBundle.getBoolean("delete")) {
                    Intent service = new Intent(this, GCMNotificationService.class);
                    service.putExtra("serverdata", serverBundle);
                    startService(service);
                }
            }
        }
        /*
            Toolbar
         */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Get the view elements
         */
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        rootView = (CoordinatorLayout) findViewById(R.id.cl_match);
        layoutCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsingtoolbar);
        layoutAppBar = (AppBarLayout) findViewById(R.id.layout_appbar);
        ivProfilepictureThumb = (CircleNetworkImageView) findViewById(R.id.iv_thumbprofilepicture);
        svThumbs = (HorizontalScrollView) findViewById(R.id.sv_profilepictures);
        vpPictures = (ViewPager) findViewById(R.id.vp_pictures);
        CircleNetworkImageView ivPicture1 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture1);
        CircleNetworkImageView ivPicture2 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture2);
        CircleNetworkImageView ivPicture3 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture3);
        CircleNetworkImageView ivPicture4 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture4);
        CircleNetworkImageView ivPicture5 = (CircleNetworkImageView) findViewById(R.id.banner_profilepicture5);
        thumbs = new CircleNetworkImageView[]{ivPicture1, ivPicture2, ivPicture3, ivPicture4, ivPicture5};
        tvName = (TextView) findViewById(R.id.tv_username);
        tvStudycourse = (TextView) findViewById(R.id.tv_studycourse);
        tvDescription = (TextView) findViewById(R.id.tv_description);
        btnFloating = (FloatingActionButton) findViewById(R.id.btn_floating);

        tagList = new ArrayList<String>();
        recyclerAdapter = new ChipListAdapter(this, tagList);
        recyclerTags = (RecyclerView) findViewById(R.id.recycler_tags);
        recyclerTags.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerTags.setAdapter(recyclerAdapter);

        recyclerAdapterTimeslots = new ChipListAdapter(this, new ArrayList<String>());
        recyclerViewTimeslots = (RecyclerView) findViewById(R.id.recycler_timeslots);
        recyclerViewTimeslots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewTimeslots.setAdapter(recyclerAdapterTimeslots);

        /*
            Initialise the view elements
         */
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
        ivProfilepictureThumb.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        ivProfilepictureThumb.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        for (CircleNetworkImageView thumb : thumbs) {
            thumb.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            thumb.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        }

        if (isMatch) {
            btnFloating.setEnabled(true);
            btnFloating.setVisibility(View.VISIBLE);
            btnFloating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRequestConfirmDialog();
                }
            });
        } else {
            btnFloating.setEnabled(false);
            btnFloating.setVisibility(View.GONE);
        }

        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                if (LoginChecker.SUCCESS == status) {
                    mTutinder = Tutinder.getInstance();
                    /*
                        API Call
                    */
                    try {
                        if (isMatch) {
                            requestMatchedUser(userId, courseId);
                        } else {
                            requestUser(userId);
                        }

                    } catch (InvalidParameterException e) {
                        e.printStackTrace();
                        Toast.makeText(MatchActivity.this, getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRequestResponse(User response) {
        // set user
        user = response;
        // set toolbar title
        layoutCollapsingToolbar.setTitle(user.getName());
        // set viewpager pictures
        String[] pathPictures = user.getCloudinarypicturepaths(false);

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
        // set startpicture
        for (int i = 0; i < pathPictures.length; i++) {
            if (pathPictures[i].equals(user.getProfilepicturepath())) {
                vpPictures.setCurrentItem(i);
            }
        }
        // set thumbnails
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
        // set TextViews
        String name = user.getName();
        if (name != null) tvName.setText(name);
        String studycourse = user.getStudycourse();
        if (studycourse != null) tvStudycourse.setText(studycourse);
        String description = user.getDescription();
        if (description != null) tvDescription.setText(description);
        // set Tags
        for (String tagId : user.getPersonalitytags()) {
            requestTags(tagId);
        }
        // chipsView...
        requestTimeslotsForUser(user.getCourse(courseId).getChosentimeslots());
    }

    /**
     * Shows the activities loading ProgressDialog.
     */
    private void showProgressDialog() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setEnabled(true);
        }

    }

    /**
     * Hides the activities loading ProgressDialog if its showing.
     */
    private void hideProgressDialog() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setEnabled(false);
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void showRequestConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TuTinder_Dialog_Alert);
        builder.setTitle(getString(R.string.dialog_title_group_request))
                .setMessage(getString(R.string.dialog_message_group_request))
                .setIcon(R.drawable.ic_group_add_black_24dp)
                .setPositiveButton(getString(R.string.btn_send), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestGroup(userId, courseId);
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * Makes an API call to load a user with the given userId.
     */
    private void requestMatchedUser(String userId, String courseId) throws InvalidParameterException {
        if (userId == null || courseId == null)
            throw new InvalidParameterException("One of the parameters userid or courseid was null");
        showProgressDialog();
        final String URL = mVolley.getAPIRoot() + "/matching/courses/" + courseId + "/users/" + userId;
        GsonRequest<User> requestCourse = new GsonRequest<User>(Request.Method.GET, URL, null, User.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                onRequestResponse(response);
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Toast.makeText(mContext, error.networkResponse.statusCode + ":" + getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    //TODO Remove error.getMessage() in Production
                    Toast.makeText(mContext, getString(R.string.strangeError) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideProgressDialog();
                finish();
            }
        });
        mVolley.addToRequestQueue(requestCourse);
    }

    /**
     * Makes an API call to load a user with the given userId.
     */
    private void requestUser(String userId) throws InvalidParameterException {
        if (userId == null) throw new InvalidParameterException("Userid was null");
        final String URL = mVolley.getAPIRoot() + "/users/" + userId;
        GsonRequest<User> requestCourse = new GsonRequest<User>(Request.Method.GET, URL, null, User.class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                onRequestResponse(response);
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Toast.makeText(mContext, error.networkResponse.statusCode + " " + getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, getString(R.string.strangeError) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideProgressDialog();
                finish();
            }
        });
        showProgressDialog();
        mVolley.addToRequestQueue(requestCourse);
    }

    /**
     * Makes an API call to send a group request with the given userId.
     */
    public void requestGroup(final String userId, final String courseId) throws InvalidParameterException {
        if (userId == null) throw new InvalidParameterException("Userid was null");
        if (courseId == null) throw new InvalidParameterException("Courseid was null");
        final String URL = mVolley.getAPIRoot() + "/courses/" + courseId + "/users/" + userId + "/grouprequest";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.POST, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Snackbar.make(rootView, getString(R.string.toast_group_request_succeded), Snackbar.LENGTH_SHORT)
                        .show();
                hideProgressDialog();
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
                                    requestGroup(userId, courseId);
                                }
                            })
                            .show();
                }
            }
        });
        showProgressDialog();
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes an API call to load a personalitytag with the given tagId.
     *
     * @param tagId
     */
    public void requestTags(String tagId) {
        String url = mVolley.getAPIRoot() + "/variables/tags/personality/" + tagId;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, url, null, null, new Response.Listener<String>() {
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

                        tagList.add(tagName);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO
                Toast.makeText(mContext, getString(R.string.strangeError) + ": PersonalityTagSet request error", Toast.LENGTH_LONG).show();
            }
        });
        mVolley.addToRequestQueue(request);
    }

    private void requestTimeslotsForUser(String[] timeslots) {
        //Only load if there are timeslots provided...
        if (timeslots.length > 0) {

            //Build querystring to have the timeslots loaded...
            String querystring = "?tsid=";
            for (int i = 0; i < timeslots.length; i++) {
                if (i < timeslots.length - 1)
                    querystring += timeslots[i] + ",";
                else {
                    querystring += timeslots[i];
                }
            }
            GsonRequest<Timeslot[]> tsRequest = new GsonRequest<>(Request.Method.GET, mVolley.getAPIRoot() + "/variables/timeslots" + querystring, null, Timeslot[].class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<Timeslot[]>() {
                @Override
                public void onResponse(Timeslot[] response) {
                    if (response != null) {
                        timeslotobjects = response;
                        List<String> timeslotsstrings = new ArrayList<>();
                        for (int i = 0; i < timeslotobjects.length; i++) {
                            timeslotsstrings.add(timeslotobjects[i].getFormattedTimeslot(mContext));
                        }
                        TextView tvTimeSlots = (TextView) findViewById(R.id.tv_timeslots);
                        if (timeslotsstrings.isEmpty()) {
                            tvTimeSlots.setVisibility(View.VISIBLE);
                        } else {
                            tvTimeSlots.setVisibility(View.GONE);
                        }
                        recyclerAdapterTimeslots = new ChipListAdapter(MatchActivity.this, timeslotsstrings);
                        recyclerViewTimeslots.setAdapter(recyclerAdapterTimeslots);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext, "TODO Error getting Timeslots", Toast.LENGTH_LONG).show();
                }
            });

            mVolley.addToRequestQueue(tsRequest);
        } else {
            TextView tvTimeSlots = (TextView) findViewById(R.id.tv_timeslots);
            tvTimeSlots.setVisibility(View.VISIBLE);
        }
    }

}
