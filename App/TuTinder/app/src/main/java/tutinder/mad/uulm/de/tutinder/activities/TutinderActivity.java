package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.wenchao.cardstack.CardStack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.CardStackAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.CardStackGroup;
import tutinder.mad.uulm.de.tutinder.models.CardStackObject;
import tutinder.mad.uulm.de.tutinder.models.CardStackUser;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

public class TutinderActivity extends AppCompatActivity {

    private final String PREFS_FILTER_ROOT = "filter_settings";
    private final String PREFS_FILTER_TIMESLOTS = "filter_timeslots";
    private final String PREFS_FILTER_TAGS = "filter_tags";
    private final String PREFS_FILTER_USERS = "filter_users";
    private final String PREFS_FILTER_GROUPS = "filter_groups";
    private final int FALSE = 0;
    private final int TRUE = 1;
    private final int DEFAULT_FILTER_TIMESLOTS = FALSE;
    private final int DEFAULT_FILTER_TAGS = 5;
    private final boolean DEFAULT_FILTER_USERS = true;
    private final boolean DEFAULT_FILTER_GROUPS = false;

    private final boolean RATING_LIKE = true;
    private final boolean RATING_DISLIKE = false;

    private Tutinder helper;
    private VolleySingleton volleySingleton;

    private Context context;
    private String courseId;
    private String coursename;
    private String coursethumbnailpath;

    private int filterTags, filterTimeslots;
    private boolean
            filterGroups,
            filterUsers;

    private List<CardStackObject> itemList;
    private Map<String, CardStackObject> ratedItemList;

    private CoordinatorLayout layoutTutinder;
    private AppBarLayout layoutAppBar;
    private Toolbar toolbar;
    private RelativeLayout layoutEmptyState;
    private CircleNetworkImageView ivThumbCoursepicture;
    private CardStack cardStack;
    private CardStackAdapter cardStackAdapter;
    private ProgressBar loadingProgressBar;
    private Button btnLike, btnDislike, btnResetAllRatings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutinder);
        /*
            Get the Application References
         */
        context = getApplicationContext();
        helper = Tutinder.getInstance();
        volleySingleton = VolleySingleton.getInstance(context);
        itemList = new ArrayList<CardStackObject>();
        ratedItemList = new HashMap<String, CardStackObject>();
        /*
            Args
         */
        Bundle args = getIntent().getExtras();
        courseId = args.getString("courseid");
        coursename = args.getString("coursename");
        coursethumbnailpath = args.getString("coursethumbnailpath");
        /*
            Prefs
         */
        SharedPreferences prefs = getSharedPreferences(PREFS_FILTER_ROOT, MODE_PRIVATE);
        if (prefs != null) {
            filterTimeslots = prefs.getInt(PREFS_FILTER_TIMESLOTS, DEFAULT_FILTER_TIMESLOTS);
            filterTags = prefs.getInt(PREFS_FILTER_TAGS, DEFAULT_FILTER_TAGS);
            filterUsers = prefs.getBoolean(PREFS_FILTER_USERS, DEFAULT_FILTER_USERS);
            filterGroups = prefs.getBoolean(PREFS_FILTER_GROUPS, DEFAULT_FILTER_GROUPS);
        } else {
            filterTimeslots = DEFAULT_FILTER_TIMESLOTS;
            filterTags = DEFAULT_FILTER_TAGS;
            filterUsers = DEFAULT_FILTER_USERS;
            filterGroups = DEFAULT_FILTER_GROUPS;
        }
        /*
            Toolbar
         */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        layoutAppBar = (AppBarLayout) findViewById(R.id.layout_appbar);
        if (toolbar != null) {
            if (coursename != null) {
                toolbar.setTitle(coursename);
            }
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Get the View Elements
         */
        layoutTutinder = (CoordinatorLayout) findViewById(R.id.layout_tutinder);
        layoutEmptyState = (RelativeLayout) findViewById(R.id.layout_emptystate);

        cardStackAdapter = new CardStackAdapter(this, 0, courseId);

        cardStack = (CardStack) findViewById(R.id.cardstack_container);
        cardStack.setListener(new CardStackListener());
        cardStack.setContentResource(R.layout.activity_tutinder_card_layout);
        cardStack.setAdapter(cardStackAdapter);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);

        btnDislike = (Button) findViewById(R.id.btn_dislike);
        btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRate(RATING_DISLIKE, true);
            }
        });
        btnLike = (Button) findViewById(R.id.btn_like);
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRate(RATING_LIKE, true);
            }
        });
        btnResetAllRatings = (Button) findViewById(R.id.btn_resetdislikes);
        btnResetAllRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestResetAllRatings();
            }
        });

        itemList.clear();
        if (filterUsers == true) {
            requestUsers();
        }
        if (filterGroups == true) {
            requestGroups();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_tutinder_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        if (id == R.id.action_filter) {
            showFilterSettingsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the activities loading ProgressDialog.
     */
    private void showProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setEnabled(true);
        }

    }

    /**
     * Hides the activities loading ProgressDialog if its showing.
     */
    private void hideProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setEnabled(false);
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void showEmptyState() {
        if (layoutEmptyState != null) {
            if (layoutEmptyState.getVisibility() != View.VISIBLE) {
                layoutEmptyState.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hideEmptyState() {
        if (layoutEmptyState != null) {
            if (layoutEmptyState.getVisibility() != View.GONE) {
                layoutEmptyState.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Shows a dialog in which filter settings can be configured.
     */
    public void showFilterSettingsDialog() {
        final View customView = getLayoutInflater().inflate(R.layout.dialog_filter_settings, null);
        // create if null
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TuTinder_Dialog_Alert)
                .setTitle(R.string.dialog_title_filtersettings)
                .setIcon(R.drawable.ic_filter_list_black_24dp)
                .setView(customView)
                .setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateFilterSettings(customView);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.btn_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateFilterSettings(null);
                    }
                });
        // initialise view
        final TextView tvSeekBarValue = (TextView) customView.findViewById(R.id.tv_seekbarvalue);
        tvSeekBarValue.setText((filterTags * 10) + "%");
        SeekBar seekBar = (SeekBar) customView.findViewById(R.id.seekbar_filtertags);
        seekBar.setProgress(filterTags);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSeekBarValue.setText((progress * 10) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // do nothing
            }
        });

        CheckBox boxFilterTimeslots = (CheckBox) customView.findViewById(R.id.checkbox_filtertimeslots);
        if (filterTimeslots == TRUE) boxFilterTimeslots.setChecked(true);
        else boxFilterTimeslots.setChecked(false);
        CheckBox boxFilterUsers = (CheckBox) customView.findViewById(R.id.checkbox_filterusers);
        if (filterUsers) boxFilterUsers.setChecked(true);
        else boxFilterUsers.setChecked(false);
        CheckBox boxFilterGroups = (CheckBox) customView.findViewById(R.id.checkbox_filtergroups);
        if (filterGroups) boxFilterGroups.setChecked(true);
        else boxFilterGroups.setChecked(false);
        // show
        builder.create().show();
    }

    /**
     * Updates and saves filter settings after closing filter-dialog with positive-button.
     *
     * @param view
     */
    public void updateFilterSettings(View view) {
        if (view != null) {
            // update filter settings
            SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekbar_filtertags);
            filterTags = seekBar.getProgress();
            CheckBox boxFilterTimeslots = (CheckBox) view.findViewById(R.id.checkbox_filtertimeslots);
            if (boxFilterTimeslots.isChecked()) {
                filterTimeslots = TRUE;
            } else {
                filterTimeslots = FALSE;
            }
            CheckBox boxFilterUsers = (CheckBox) view.findViewById(R.id.checkbox_filterusers);
            filterUsers = boxFilterUsers.isChecked();
            CheckBox boxFilterGroups = (CheckBox) view.findViewById(R.id.checkbox_filtergroups);
            filterGroups = boxFilterGroups.isChecked();
        } else {
            // default values
            filterTimeslots = DEFAULT_FILTER_TIMESLOTS;
            filterTags = DEFAULT_FILTER_TAGS;
            filterUsers = DEFAULT_FILTER_USERS;
            filterGroups = DEFAULT_FILTER_GROUPS;
        }
        // save in shared prefs
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_FILTER_ROOT, MODE_PRIVATE).edit();
        editor.putInt(PREFS_FILTER_TIMESLOTS, filterTimeslots);
        editor.putInt(PREFS_FILTER_TAGS, filterTags);
        editor.putBoolean(PREFS_FILTER_USERS, filterUsers);
        editor.putBoolean(PREFS_FILTER_GROUPS, filterGroups);
        editor.commit();
        // show hint
        if (view != null) {
            Snackbar.make(layoutTutinder, R.string.toast_filtersettingsavedsucess, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(layoutTutinder, R.string.toast_filtersettingsresettedsuccess, Snackbar.LENGTH_SHORT).show();
        }
        // make api-call
        itemList.clear();
        if (filterUsers == true) {
            requestUsers();
        }
        if (filterGroups == true) {
            requestGroups();
        }
    }

    /**
     * Handles CardStack after a card was removed or added.
     *
     * @param update
     */
    public void handleCardStack(boolean update) {
        if (!itemList.isEmpty()) {
            if (update) {
                cardStackAdapter.clear();
                cardStack.reset(true);
                cardStackAdapter.addAll(itemList);
            }
            enableButtons();
            hideEmptyState();
        } else {
            disableButtons();
            showEmptyState();
            // TODO load next users and re-enable buttons
            // TODO request ratingReset via dialog if all user are rated
        }
    }

    /**
     * Disables the Like-Button and the Dislike-Button
     */
    public void disableButtons() {
        if (btnDislike.getVisibility() == View.VISIBLE && btnLike.getVisibility() == View.VISIBLE) {
            btnDislike.setVisibility(View.INVISIBLE);
            btnLike.setVisibility(View.INVISIBLE);
        }
        if (btnDislike.isEnabled() && btnLike.isEnabled()) {
            btnDislike.setEnabled(false);
            btnLike.setEnabled(false);
        }
    }

    /**
     * Enables the Like-Button and the Dislike-Button
     */
    public void enableButtons() {
        if (btnDislike.getVisibility() == View.INVISIBLE && btnLike.getVisibility() == View.INVISIBLE) {
            btnDislike.setVisibility(View.VISIBLE);
            btnLike.setVisibility(View.VISIBLE);
        }
        if (!btnDislike.isEnabled() && !btnLike.isEnabled()) {
            btnDislike.setEnabled(true);
            btnLike.setEnabled(true);
        }
    }

    /**
     * Undo a rating for a user.
     *
     * @param itemID
     * @param makeApiCall
     */
    private void undoRating(String itemID, boolean makeApiCall) {
        CardStackObject ratedItem = ratedItemList.get(itemID);
        if (ratedItem != null) {
            if (makeApiCall) {
                requestUndoRating(ratedItem);
            } else {
                Snackbar.make(layoutTutinder, getString(R.string.error_rating_error), Snackbar.LENGTH_LONG).show();
                itemList.add(0, ratedItemList.remove(itemID));
                handleCardStack(true);
            }
        }
    }

    /**
     * Requests all not yet rated users.
     */
    public void requestUsers() {
        showProgressBar();
        String filters = "?personalitythreshold=" + filterTags
                + "&timeslots=" + filterTimeslots;
        String url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings/users" + filters;
        GsonRequest<User[]> requestUsers = new GsonRequest<User[]>(Request.Method.GET, url, null, User[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<User[]>() {
            @Override
            public void onResponse(User[] response) {
                if (response != null) {
                    for (User user : response) {
                        itemList.add(new CardStackUser(user));
                    }
                }
                handleCardStack(true);
                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, getString(R.string.users_could_not_be_loaded) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
        volleySingleton.addToRequestQueue(requestUsers);
    }


    /**
     * Requests all not yet rated users.
     */
    public void requestGroups() {
        showProgressBar();

        String url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings/groups";
        GsonRequest<Group[]> requestGroups = new GsonRequest<Group[]>(Request.Method.GET, url, null, Group[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] response) {
                if (response != null) {
                    for (Group group : response) {
                        //Add Groups to front
                        itemList.add(0, new CardStackGroup(group));
                    }
                }
                handleCardStack(true);
                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressBar();
                if (error.networkResponse != null && error.networkResponse.statusCode == 423) {
                    Toast.makeText(context, getString(R.string.rating_as_a_group) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, getString(R.string.error_loading_groups) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();


                }
            }
        });
        volleySingleton.addToRequestQueue(requestGroups);
    }


    /**
     * Makes an api-call to rate a user.
     *
     * @param rating
     */
    public void requestRate(final boolean rating, boolean isButtonPressed) {
        // get Item
        String id = null;
        CardStackObject item = cardStackAdapter.getItem(cardStack.getCurrIndex());
        if (item instanceof CardStackUser) {
            id = ((CardStackUser) item).getUser().get_id();
        }
        if (item instanceof CardStackGroup) {
            id = ((CardStackGroup) item).getGroup().get_id();
        }
        final String itemID = id;

        // remove Item from List
        int position = -1;
        for (int i = 0; i < itemList.size(); i++) {
            item = itemList.get(i);
            if (item instanceof CardStackUser) {
                if (((CardStackUser) item).getUser().get_id().equals(itemID)) {
                    position = i;
                    break;
                }
            }
            if (item instanceof CardStackGroup) {

                if (((CardStackGroup) item).getGroup().get_id().equals(itemID)) {
                    position = i;
                    break;
                }

            }
        }
        item = itemList.remove(position);
        // copy Item into UNDO-List
        ratedItemList.put(itemID, item);
        // dismiss Card if isButtonPressed
        if (isButtonPressed) {
            if (rating == RATING_DISLIKE) {
                // swipe card left
                cardStack.discardTop(0);
            } else {
                // swipe card right
                cardStack.discardTop(1);
            }
        }
        // prepare Request
        String url = null;
        if (item instanceof CardStackUser) {
            url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings/users/" + ((CardStackUser) item).getUser().get_id() + "/rating";
        }
        if (item instanceof CardStackGroup) {
            url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings/groups/" + ((CardStackGroup) item).getGroup().get_id() + "/rating";
        }
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("like", rating);
        } catch (JSONException e) {
            // restore rated user
            undoRating(itemID, false);
            return;
        }
        // make request
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.POST, url, jsonBody.toString(), helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    Boolean match = responseJson.getBoolean("match");
                    if (match) {
                        //Response has a type field where we can decide if the match was based on a user or a group...
                        String type = responseJson.getString("type");
                        switch (type) {
                            case "group":
                                Intent groupIntent = new Intent(TutinderActivity.this, GroupActivity.class);
                                groupIntent.putExtra("matchedid", responseJson.getString("_ratedgroupid"));
                                groupIntent.putExtra("courseid", courseId);
                                startActivity(groupIntent);
                                break;
                            case "user":
                                Intent matchIntent = new Intent(TutinderActivity.this, MatchActivity.class);
                                matchIntent.putExtra("matchedid", responseJson.getString("_rateduserid"));
                                matchIntent.putExtra("courseid", courseId);
                                startActivity(matchIntent);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (rating == RATING_DISLIKE) {
                    Snackbar.make(layoutTutinder, getString(R.string.toast_undo_rating), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.action_undo), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    undoRating(itemID, true);
                                }
                            })
                            .show();
                }
                handleCardStack(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                undoRating(itemID, false);
            }
        });
        volleySingleton.addToRequestQueue(request);
    }

    /**
     * Makes an api-call to undo a rating for a user.
     *
     * @param ratedItem
     */
    public void requestUndoRating(final CardStackObject ratedItem) {
        showProgressBar();

        String url = null;
        String id = null;
        if (ratedItem instanceof CardStackUser) {
            id = ((CardStackUser) ratedItem).getUser().get_id();
            url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings/users/" + id;
        }
        if (ratedItem instanceof CardStackGroup) {
            id = ((CardStackGroup) ratedItem).getGroup().get_id();
            url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings/groups/" + id;
        }
        final String itemID = id;

        CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, url, null, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                itemList.add(0, ratedItemList.remove(itemID));
                handleCardStack(true);
                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
        volleySingleton.addToRequestQueue(request);
    }

    public void requestResetAllRatings() {
        showProgressBar();
        String url = volleySingleton.getAPIRoot() + "/matching/courses/" + courseId + "/ratings";
        final CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, url, null, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (filterUsers) {
                    requestUsers();
                }
                if (filterGroups) {
                    requestGroups();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
        volleySingleton.addToRequestQueue(request);
    }


    /**
     * @author 1uk4s
     */
    private class CardStackListener implements CardStack.CardEventListener {

        private final int TRESHOLD = 300;

        private final int TOP_LEFT = 0;
        private final int TOP_RIGHT = 1;
        private final int BOTTOM_LEFT = 2;
        private final int BOTTOM_RIGHT = 3;

        // use to dismiss the card or not after swipe
        private final Boolean DISMISS = true;
        private final Boolean BACK = false;

        private boolean isHighlightedLike = false;
        private boolean isHighlightedDislike = false;

        @Override
        public boolean swipeEnd(int section, float distance) {
            if (distance > TRESHOLD) {
                if (section == TOP_LEFT || section == BOTTOM_LEFT) {
                    requestRate(RATING_DISLIKE, false);
                }
                if (section == TOP_RIGHT || section == BOTTOM_RIGHT) {
                    requestRate(RATING_LIKE, false);
                }
                clearHighlighting();
                return DISMISS;
            }
            clearHighlighting();
            return BACK;
        }

        @Override
        public boolean swipeStart(int section, float distance) {
            return false;
        }

        @Override
        public boolean swipeContinue(int section, float distanceX, float distanceY) {
            if (distanceX > TRESHOLD) {
                if (section == TOP_LEFT || section == BOTTOM_LEFT) {
                    addHighlightingDislike();
                }
                if (section == TOP_RIGHT || section == BOTTOM_RIGHT) {
                    addHighlightingLike();
                }
                return DISMISS;
            } else {
                clearHighlighting();
            }
            return BACK;
        }

        @Override
        public void discarded(int mIndex, int direction) {
            //this callback invoked when dismiss animation is finished.
        }

        @Override
        public void topCardTapped() {
            //this callback invoked when a top card is tapped by user.
        }

        public void addHighlightingDislike() {
            if (!isHighlightedDislike) {
                btnDislike.getBackground().setColorFilter(ContextCompat.getColor(TutinderActivity.this, R.color.red), PorterDuff.Mode.MULTIPLY);
                isHighlightedDislike = true;
            }
        }

        public void addHighlightingLike() {
            if (!isHighlightedLike) {
                btnLike.getBackground().setColorFilter(ContextCompat.getColor(TutinderActivity.this, R.color.green), PorterDuff.Mode.MULTIPLY);
                isHighlightedLike = true;
            }
        }

        public void clearHighlighting() {
            if (isHighlightedDislike) {
                btnDislike.getBackground().clearColorFilter();
                isHighlightedDislike = false;
            }
            if (isHighlightedLike) {
                btnLike.getBackground().clearColorFilter();
                isHighlightedLike = false;
            }
        }

    }
}
