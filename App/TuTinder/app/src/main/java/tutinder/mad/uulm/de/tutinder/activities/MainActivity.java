package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.fragments.MyCourseListFragment;
import tutinder.mad.uulm.de.tutinder.activities.fragments.MyGroupListFragment;
import tutinder.mad.uulm.de.tutinder.activities.fragments.TutinderFragment;
import tutinder.mad.uulm.de.tutinder.admin.AdminActivity;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;
import tutinder.mad.uulm.de.tutinder.services.GCMRegisterService;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * MainActivity of Tutinder.
 *
 * @author 1uk4s
 * @author snap10
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final int TUTINDERPOSITION = 0;
    private static final int COURSESPOSITION = 1;
    private static final int GROUPSPOSITION = 2;

    private static final int[] TAB_ICONS_PASSIVE = new int[]{
            R.drawable.ic_tutinder_grey_24dp,
            R.drawable.ic_school_grey_24dp,
            R.drawable.ic_people_grey_24dp
    };
    private static final int[] TAB_ICONS_ACTIVE = new int[]{
            R.drawable.ic_tutinder_white_24dp,
            R.drawable.ic_school_white_24dp,
            R.drawable.ic_people_white_24dp
    };
    private static final int[] TAB_TITLE = new int[]{
            R.string.tab_tutinder,
            R.string.tab_my_courses,
            R.string.tab_my_groups
    };

    private Context mContex;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private Toolbar toolbar;
    private CircleNetworkImageView ivProfilepicture;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton btnFloating;

    public static boolean isFABVisible = false;


    /**
     * Initialises the Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Application References
        mContex = getApplicationContext();
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContex);

        // Arguments
        Bundle args = getIntent().getExtras();
        if(args != null) {
            if (args.getBoolean("logout")) {
                onLogout();
                return;
            }
            if (args.getBoolean("editaccount")) {
                Intent intent = new Intent(MainActivity.this, EditAccountActivity.class);
                startActivity(intent);
            }
        }

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Start GCM necessary Services if Google Play Services are installed on the device.
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            startService(new Intent(this, GCMRegisterService.class));
            // Start Service to display notifications for incoming GCM push messages.
            startService(new Intent(this, GCMNotificationService.class));
        }

        // Views
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(ViewPagerAdapter.NUM_ITEMS);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case TUTINDERPOSITION:
                        isFABVisible = false;
                        btnFloating.hide();
                        break;
                    case COURSESPOSITION:
                        isFABVisible = true;
                        btnFloating.show();
                        break;
                    case GROUPSPOSITION:
                        isFABVisible = false;
                        btnFloating.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < TAB_ICONS_ACTIVE.length; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab.isSelected()) {
                tab.setIcon(TAB_ICONS_ACTIVE[i]);
                getSupportActionBar().setTitle(TAB_TITLE[i]);
            } else {
                tab.setIcon(TAB_ICONS_PASSIVE[i]);
            }
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                tab.setIcon(TAB_ICONS_ACTIVE[position]);
                getSupportActionBar().setTitle(TAB_TITLE[position]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewPager.setCurrentItem(position);
                tab.setIcon(TAB_ICONS_PASSIVE[position]);
                getSupportActionBar().setTitle(TAB_TITLE[position]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });


        btnFloating = (FloatingActionButton) findViewById(R.id.btn_floating);
        isFABVisible = false;
        btnFloating.hide();
        btnFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContex, CourseListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LoginChecker.logInCurrentUser(getApplicationContext(), new LoginChecker.LoginCheckerListener() {
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
        if (mTutinder != null && mTutinder.getLoggedInUser() != null && mTutinder.getLoggedInUser().isAdmin())
            getMenuInflater().inflate(R.menu.menu_activity_main_toolbar_admin, menu);
        else getMenuInflater().inflate(R.menu.menu_activity_main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Callback for OptionMenu Item clicks.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                showLogoutDialog();
                break;
            case R.id.action_edit_profile:
                Intent intent = new Intent(mContex, EditAccountActivity.class);
                startActivity(intent);
                break;
            case R.id.action_gotoadmin:
                Intent adminIntent = new Intent(getApplicationContext(), AdminActivity.class);
                startActivity(adminIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Go back to first ViewPager Item, if already there show Logout Dialog.
     */
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != TUTINDERPOSITION) {
            // back to overview
            viewPager.setCurrentItem(TUTINDERPOSITION);
        } else {
            // confirm logout
            showLogoutDialog();
        }
    }

    /**
     * Shows a AlertDialog in which the user can decide to onLogout or not.
     */
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TuTinder_Dialog_Alert);
        builder
                .setTitle(R.string.dialog_title_logout)
                .setMessage(R.string.dialog_message_logout)
                .setIcon(R.drawable.ic_logout_accent_24dp)
                .setPositiveButton(R.string.btn_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onLogout();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog logoutDialog = builder.create();
        logoutDialog.show();
    }

    /**
     * Removes saved user from shared prefs and starts LoginActivity with cleared activity stack.
     */
    private void onLogout() {
        // Delete User specific SharedPref entries
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean("sent_token_to_server", false).apply();
        SharedPreferences userPref = getSharedPreferences("logged_in_user", MODE_PRIVATE);
        userPref.edit().clear().apply();

        // Delete GCM Token from server
        VolleySingleton volley = VolleySingleton.getInstance(getApplicationContext());
        CustomJsonRequest updateRequest = new CustomJsonRequest(Request.Method.DELETE, volley.getAPIRoot() + "/user/gcmtoken", null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Successfully deleted token on server ");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Failed to delete token on server");
            }
        });
        volley.addToRequestQueue(updateRequest);

        // Stop services
        if (checkPlayServices()) {
            stopService(new Intent(this, GCMRegisterService.class));
            stopService(new Intent(this, GCMNotificationService.class));
        }

        // Start LoginActivity and clear ActivityStack
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * @param groupID
     */
    public void requestLeaveGroup(String groupID) {
        final String URL = mVolley.getAPIRoot() + "/user/groups/" + groupID;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
            }
        }

        );
        mVolley.addToRequestQueue(request);
    }

    /**
     * FragmentPagerAdapter for the MainActivity ViewPager.
     *
     * @author 1uk4s
     * @author snap10
     */
    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private static final int NUM_ITEMS = 3;

        /**
         * Default Constructor.
         *
         * @param manager
         */
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        /**
         * Returns the Fragment at the specified position.
         *
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TUTINDERPOSITION:
                    return new TutinderFragment().newInstance();
                case COURSESPOSITION:
                    return new MyCourseListFragment().newInstance();
                case GROUPSPOSITION:
                    return new MyGroupListFragment().newInstance();
                default:
                    throw new IllegalArgumentException("Wrong Fragment ID chosen");
            }
        }

        /**
         * Returns the amount of Fragments stored in the ViewPagerAdapter.
         *
         * @return
         */
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }
    }

}
