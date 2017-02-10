package tutinder.mad.uulm.de.tutinder.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.FriendListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.services.GCMNotificationService;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

public class FriendlistActivity extends AppCompatActivity {

    public final int REQUEST_CAMERA_PERMISSIONS = 0;

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private Toolbar toolbar;
    private ProgressBar loadingProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewFriends;
    private FriendListAdapter recyclerAdapterFriends;
    private FloatingActionButton btnFloating, btnFloatingCamera, btnFloatingQrcode;
    private boolean isFabOpen;

    /**
     * Initialises the Activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Application References
        mContext = getApplicationContext();
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContext);

        // Arguments
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra(GCMNotificationService.BUNDLE_NAME_SERVERDATA);
        if (args != null) {
            if(args.containsKey(GCMNotificationService.BUNDLE_NAME_SERVERDATA)) {
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

        //Views
        rootView = (CoordinatorLayout) findViewById(R.id.cl_friendlist);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestFriends();
            }
        });

        recyclerViewFriends = (RecyclerView) findViewById(R.id.recycler_myfriends);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerAdapterFriends = new FriendListAdapter(FriendlistActivity.this, new ArrayList<User>(), new OnListItemInteractListener() {
            @Override
            public void onListItemInteract(String method, Bundle args) {
                if(method.equals(getString(R.string.action_show_profile))) {
                    // Start FriendActivity
                    Intent intentProfile = new Intent(FriendlistActivity.this, FriendActivity.class);
                    intentProfile.putExtras(args);
                    startActivity(intentProfile);
                } else if(method.equals(getString(R.string.action_remove_friend))) {
                    // Send Remove Friend Request
                    showRemoveDialog(args.getString("friendid"));
                }
            }

            @Override
            public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {

            }
        });
        recyclerViewFriends.setAdapter(recyclerAdapterFriends);

        isFabOpen = false;
        btnFloating = (FloatingActionButton) findViewById(R.id.btn_floating);
        btnFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFloatingActionButtonClicked();
            }
        });
        btnFloatingCamera = (FloatingActionButton) findViewById(R.id.btn_floating_camera);
        btnFloatingCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraPermissions();
            }
        });
        btnFloatingQrcode = (FloatingActionButton) findViewById(R.id.btn_floating_qrcode);
        btnFloatingQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendlistActivity.this, QrCodeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestFriends();
    }

    /**
     * Callback for OptionMenu Item clicks.
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
     * Callback for QR-Scanner.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult != null) {
                    String result = scanResult.getContents();
                    // prevent scanning other qr codes
                    if (result.startsWith(QrCodeActivity.QR_PREFIX)) {
                        String userId = result.split(QrCodeActivity.QR_DELIMITER)[2];
                        UUID nonce = UUID.fromString(result.split(QrCodeActivity.QR_DELIMITER)[3]);
                        // prevent adding yourself
                        if (!userId.equals(mTutinder.getLoggedInUser().get_id())) {
                            requestAddFriend(userId, nonce);
                            return;
                        }
                    }
                }
                // Show error Message
                Snackbar.make(rootView, getString(R.string.error_reading_qr_code), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                scanQrCode();
                            }
                        })
                        .show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Callback for PermissionManager.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanQrCode();
                } else {
                    // Show error Message
                    Snackbar.make(rootView, getString(R.string.error_permissions_denied), Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Checks if app has permissions for taking a picture. If not, it will request not
     * granted permissions.
     */
    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean askPermissions = false;
            int pCamera = checkSelfPermission(Manifest.permission.CAMERA);
            if (pCamera != PackageManager.PERMISSION_GRANTED) {
                String[] request = {
                        Manifest.permission.CAMERA
                };
                requestPermissions(request, REQUEST_CAMERA_PERMISSIONS);
                return;
            }
        }
        scanQrCode();
    }

    /**
     * Starts Zxing Activity for scanning a qr code. After scanning it will return data in this activity
     * by callback "onActivityResult".
     */
    public void scanQrCode() {
        IntentIntegrator integrator = new IntentIntegrator(FriendlistActivity.this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    /**
     * Shows or Hides the FAB Actions.
     */
    private void onFloatingActionButtonClicked() {
        if(isFabOpen) {
            btnFloating.animate()
                    .rotation(0.0f)
                    .withLayer()
                    .setDuration(200l)
                    .setInterpolator(new OvershootInterpolator(10.0f))
                    .start();
            btnFloatingCamera.hide();
            btnFloatingQrcode.hide();
            isFabOpen = false;

        } else {
            btnFloating.animate()
                    .rotation(45.0f)
                    .withLayer()
                    .setDuration(200l)
                    .setInterpolator(new OvershootInterpolator(10.0f))
                    .start();
            btnFloatingCamera.show();
            btnFloatingQrcode.show();
            isFabOpen = true;
        }
    }

    public void onRequestResponse(List<User> response) {
        if(response != null) {
            if(response.size() > 0) {
                recyclerAdapterFriends.setItemList(response);
            } else{
                recyclerAdapterFriends.setItemList(new ArrayList<User>());
                recyclerViewFriends.setAdapter(recyclerAdapterFriends);

                // Show empty state Message
                Snackbar.make(rootView, getString(R.string.emptystate_activity_friendlist), Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        } else {
            recyclerAdapterFriends.setItemList(new ArrayList<User>());
            recyclerViewFriends.setAdapter(recyclerAdapterFriends);

            // Show error Message
            Snackbar.make(rootView, getString(R.string.error_sending_request), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestFriends();
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
     * Makes an API Call to load all friends of the logged in User.
     */
    public void requestFriends() {
        // show swiperefresh icon
        final String URL = mVolley.getAPIRoot() + "/user/friends/";
        GsonRequest<User[]> request = new GsonRequest<User[]>(Request.Method.GET, URL, null, User[].class, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<User[]>() {
            @Override
            public void onResponse(User[] response) {
                if(response != null) {
                    onRequestResponse(Arrays.asList(response));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onRequestResponse(null);
            }
        });
        showLoadingProgressBar();
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes an API Call to add a friend, which was scanned by a qr code.
     *
     * @param userId
     * @param nonce
     */
    public void requestAddFriend(String userId, UUID nonce) {
        final String URL = mVolley.getAPIRoot() + "/user/friends/" + userId + "/nonce/" + nonce.toString();
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.POST, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                requestFriends();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Show error Message
                Snackbar.make(rootView, getString(R.string.error_sending_request), Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
        mVolley.addToRequestQueue(request);
    }

    /**
     * Makes a API Call to remove the selected Friend.
     *
     * @param userid
     */
    public void requestRemoveFriend(final String userid) {
        final String URL = mVolley.getAPIRoot() + "/user/friends/" + userid;
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, URL, null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoadingProgressBar();

                requestFriends();
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
                                requestFriends();
                            }
                        })
                        .show();
            }
        });
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
     * Confirm Dialog for removing Friends.
     *
     * @param friendid
     */
    private void showRemoveDialog(final String friendid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FriendlistActivity.this, R.style.TuTinder_Dialog_Alert);
        builder.setTitle(R.string.dialog_title_removefriend)
                .setMessage(R.string.dialog_message_removefriend)
                .setPositiveButton(R.string.btn_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestRemoveFriend(friendid);
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
