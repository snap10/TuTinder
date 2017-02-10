package tutinder.mad.uulm.de.tutinder.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.PicturePagerAdapter;
import tutinder.mad.uulm.de.tutinder.exceptions.NotAllTagsException;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.DataPart;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.VolleyMultipartRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.InputChecker;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

public class EditAccountActivity extends AppCompatActivity {

    public final int REQUEST_CAMERA_PERMISSIONS = 0;
    public final int REQUEST_GALLERY_PERMISSIONS = 1;

    private static final int IMAGE_SELECT_BY_CAMERA = 0;
    private static final int IMAGE_SELECT_BY_GALLERY = 1;

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;
    private AppBarLayout layoutAppBar;
    private CollapsingToolbarLayout layoutCollapsingToolbar;
    private CircleNetworkImageView ivProfilepictureThumb;
    private Toolbar toolbar;
    private ViewPager vpPictures;
    private CircleNetworkImageView[] thumbs;
    private FloatingActionButton btnFloating;

    private ProgressBar saveProgressBar;

    private TextInputEditText inName, inDescription, inPhone, inEmail, inMatriculationNr, inStudycourse, inPasswordOld, inPassword, inPassword2;
    private LinearLayout layoutTags;
    private List<RadioGroup> radioGroups;
    private ImageButton btnShowPassword, btnHidePassword;

    private InputChecker inputChecker;


    /**
     * Initialises the Activity.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        // Application References
        mContext = getApplicationContext();
        mVolley = VolleySingleton.getInstance(getApplicationContext());
        mTutinder = Tutinder.getInstance();
        inputChecker = new InputChecker(mContext);

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        radioGroups = new ArrayList<RadioGroup>();
        initialiseView();

        // API Call
        requestTags(); // TODO check tags
    }

    /**
     * Get all necessary view elements and initializes them.
     */
    private void initialiseView() {
        rootView = (CoordinatorLayout) findViewById(R.id.cl_editprofile);

        layoutCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsingtoolbar);

        ivProfilepictureThumb = (CircleNetworkImageView) findViewById(R.id.iv_thumbprofilepicture);
        ivProfilepictureThumb.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
        ivProfilepictureThumb.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);

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

        // Initialise Thumbnails
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

        initialisePictures();

        saveProgressBar = (ProgressBar) findViewById(R.id.save_progressbar);

        inMatriculationNr = (TextInputEditText) findViewById(R.id.in_matriculationnr);
        inMatriculationNr.setText("" + mTutinder.getLoggedInUser().getMatrikelnr());
        inName = (TextInputEditText) findViewById(R.id.in_name);
        if (mTutinder.getLoggedInUser().getName() != null)
            inName.setText("" + mTutinder.getLoggedInUser().getName());
        inEmail = (TextInputEditText) findViewById(R.id.in_email);
        if (mTutinder.getLoggedInUser().getEmail() != null)
            inEmail.setText("" + mTutinder.getLoggedInUser().getEmail());
        inPhone = (TextInputEditText) findViewById(R.id.in_phone);
        if (mTutinder.getLoggedInUser().getPhone() != null)
            inPhone.setText("" + mTutinder.getLoggedInUser().getPhone());
        inStudycourse = (TextInputEditText) findViewById(R.id.in_studycourse);
        if (mTutinder.getLoggedInUser().getStudycourse() != null)
            inStudycourse.setText("" + mTutinder.getLoggedInUser().getStudycourse());
        inDescription = (TextInputEditText) findViewById(R.id.in_description);
        if (mTutinder.getLoggedInUser().getDescription() != null)
            inDescription.setText("" + mTutinder.getLoggedInUser().getDescription());
        inPassword = (TextInputEditText) findViewById(R.id.in_newpassword);
        inPassword2 = (TextInputEditText) findViewById(R.id.in_newpassword2);
        inPasswordOld = (TextInputEditText) findViewById(R.id.in_oldpassword);

        layoutTags = (LinearLayout) findViewById(R.id.layout_tags);

        btnFloating = (FloatingActionButton) findViewById(R.id.btn_floating);
        btnFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTutinder.getLoggedInUser().getCloudinarypicturepaths(true).length < 5) {
                    PopupMenu popupMenu = new PopupMenu(EditAccountActivity.this, btnFloating);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_popup_edit_account_fab, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_take_photo:
                                    requestCameraPermissions();
                                    return true;
                                case R.id.action_select_photo:
                                    requestGalleryPermissions();
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                } else {
                    final Snackbar snackBar = Snackbar.make(rootView, getString(R.string.error_to_much_profilepictures), Snackbar.LENGTH_INDEFINITE);
                    snackBar.setAction(getString(R.string.btn_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackBar.dismiss();
                        }
                    })
                    .show();
                }
            }
        });

        btnShowPassword = (ImageButton) findViewById(R.id.btn_showPassword);
        btnShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show password and switch buttons
                btnShowPassword.setVisibility(View.INVISIBLE);
                btnHidePassword.setVisibility(View.VISIBLE);
                inPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                inPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                inPasswordOld.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });
        btnHidePassword = (ImageButton) findViewById(R.id.btn_hidePassword);
        btnHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide password and switch buttons
                btnHidePassword.setVisibility(View.INVISIBLE);
                btnShowPassword.setVisibility(View.VISIBLE);
                inPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inPassword2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inPasswordOld.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    /**
     * Handles Pictures, which are delivered by Camera or Gallery Application.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_SELECT_BY_CAMERA) {
                File f = new File(Environment.getExternalStorageDirectory().toString());

                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }

                try {
                    Bitmap bm;
                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                    bm = BitmapFactory.decodeFile(f.getAbsolutePath(),btmapOptions);
                    // Image gets scaled by Cloudinary
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    byte[] byteArray = stream.toByteArray();
                    String filename = String.valueOf(System.currentTimeMillis()) + ".jpg";
                    makeUploadRequest(filename, byteArray, "image/jpg");
                    String path = android.os.Environment.getExternalStorageDirectory() + File.separator + getString(R.string.app_name) + File.separator + "default";
                    f.delete();
                } catch (Exception e) {
                    Snackbar.make(rootView, getString(R.string.error_processing_picture),Snackbar.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                }
            } else if (requestCode == IMAGE_SELECT_BY_GALLERY) {
                Uri selectedImageUri = data.getData();
                String tempPath = getPath(selectedImageUri, EditAccountActivity.this);
                Bitmap bm;
                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
                // Image gets scaled by Cloudinary
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byteArray = stream.toByteArray();
                String filename = tempPath.substring(tempPath.lastIndexOf('/') + 1, tempPath.length());
                makeUploadRequest(filename, byteArray, "image/jpg");
            }
        }
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_editaccout_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            makeUserUpdateRequest();
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Do further processing, if permission(s) got granted.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                if (allPermissionsGranted(grantResults)) {
                    selectImageByCamera();
                }
                else {
                    Snackbar.make(rootView, getString(R.string.error_permissions_denied), Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
            case REQUEST_GALLERY_PERMISSIONS:
                if (allPermissionsGranted(grantResults)) {
                    selectImageByGallery();
                }
                else {
                    Snackbar.make(rootView, getString(R.string.error_permissions_denied), Snackbar.LENGTH_LONG)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Returns true, if all requested permissions are granted.
     *
     * @param grantResults
     * @return
     */
    private boolean allPermissionsGranted(int[] grantResults) {
        boolean granted = true;
        for (int p : grantResults) {
            if (p != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    /**
     * Checks if app has permissions for taking a picture. If not, it will request not
     * granted permissions.
     */
    private void requestCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int pCamera = checkSelfPermission(Manifest.permission.CAMERA);
            int pWriteExternalStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int pReadExternalStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (pCamera != PackageManager.PERMISSION_GRANTED || pWriteExternalStorage != PackageManager.PERMISSION_GRANTED || pReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
                String[] request = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
                requestPermissions(request, REQUEST_CAMERA_PERMISSIONS);
                return;
            }
        }
        selectImageByCamera();
    }

    /**
     * Checks if app has permissions for selecting a picture by gallery. If not,
     * it will request not granted permissions.
     */
    private void requestGalleryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int pReadExternalStorage = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (pReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
                String[] request = {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
                requestPermissions(request, REQUEST_GALLERY_PERMISSIONS);
                return;
            }
        }
        selectImageByGallery();
    }

    /**
     * Starts the default camera application to take a profilepicture.
     */
    private void selectImageByCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(intent, IMAGE_SELECT_BY_CAMERA);
    }

    /**
     * Starts the default gallery application to pick a profilepicture.
     */
    private void selectImageByGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), IMAGE_SELECT_BY_GALLERY);
    }

    /**
     * Clears all Thumbnail Images.
     */
    public void clearThumbnails() {
        for (CircleNetworkImageView thumb : thumbs) {
            thumb.setImageUrl("", mVolley.getImageLoader());
            thumb.setBorderWidth(0);
        }
    }

    /**
     * Initialises all Pictures.
     */
    private void initialisePictures() {
        // Initialises ViewPager
        String[] pathPictures = mTutinder.getLoggedInUser().getCloudinarypicturepaths(false);
        if (pathPictures.length == 0) pathPictures = new String[]{"default"};
        vpPictures = (ViewPager) findViewById(R.id.vp_pictures);
        vpPictures.setAdapter(new PicturePagerAdapter(EditAccountActivity.this, pathPictures, R.drawable.ic_placeholder_coursepicture_accent_500dp));
        vpPictures.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ivProfilepictureThumb.setImageUrl((String) thumbs[position].getTag(), mVolley.getImageLoader());
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Set Thumbnails
        String[] pathTumbs = mTutinder.getLoggedInUser().getCloudinarypicturepaths(true);
        for (int i = 0; i < pathTumbs.length; i++) {
            final int position = i;
            // Set Thumbnail URLs
            thumbs[i].setImageUrl(pathTumbs[i], mVolley.getImageLoader());
            thumbs[i].setTag(pathPictures[i]);
            thumbs[i].setBorderWidth(0);
            // Set Thumbnail OnClickListeners
            thumbs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(EditAccountActivity.this, thumbs[position]);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_popup_edit_account_thumb, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_set_profilepicture:
                                    CircleNetworkImageView picture = (CircleNetworkImageView) v;
                                    mTutinder.getLoggedInUser().setProfilepicturepath((String) picture.getTag());

                                    vpPictures.setCurrentItem(position);
                                    for (CircleNetworkImageView thumb : thumbs) {
                                        thumb.setBorderWidth(0);
                                    }
                                    thumbs[position].setBorderColorResource(R.color.accent);
                                    thumbs[position].setBorderWidth(4);
                                    return true;
                                case R.id.action_remove_picture:
                                    showRemoveDialog(v);
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        // Set Startpicture
        for (int i = 0; i < pathPictures.length; i++) {
            if (pathPictures[i].equals(mTutinder.getLoggedInUser().getProfilepicturepath())) {
                vpPictures.setCurrentItem(i);

                thumbs[i].setBorderWidth(4);
                thumbs[i].setBorderColorResource(R.color.accent);
            }
        }
    }

    /**
     * Show confirm Dialog, to remove a Picture.
     * @param v
     */
    private void showRemoveDialog(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditAccountActivity.this, R.style.TuTinder_Dialog_Progress);
        builder
                .setTitle(R.string.dialog_title_remove_picture)
                .setMessage(R.string.dialog_message_remove_picture)
                .setPositiveButton(R.string.btn_remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CircleNetworkImageView picture = (CircleNetworkImageView) v;
                        String path = (String) picture.getTag();
                        if (path != null) {
                            makeImageDeleteRequest(path);
                        }
                        dialog.dismiss();
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

    /**
     * Magic Operation.
     *
     * @param uri
     * @param activity
     * @return
     */
    private String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = new CursorLoader(getApplicationContext(), uri, null, null, null, null).loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    /**
     * Makes an API Call to removes a Picture.
     * @param path
     */
    private void makeImageDeleteRequest(final String path) {
        try {
            CustomJsonRequest request = new CustomJsonRequest(Request.Method.DELETE, mVolley.getAPIRoot() + "/user/userpicture/" + URLEncoder.encode(path, "utf-8"), null, mTutinder.getLoggedInUser().getLoginCredentials(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Remove the deleted Path
                    String[] paths = mTutinder.getLoggedInUser().getCloudinarypicturepaths(false);
                    ArrayList<String> newpathsList = new ArrayList<>();
                    for (int i = 0; i < paths.length; i++) {
                        if (!path.equals(paths[i])) newpathsList.add(paths[i]);
                    }
                    mTutinder.getLoggedInUser().setCloudinarypicturepaths(newpathsList.toArray(new String[newpathsList.size()]));
                    // Check if was set as Profilepicture
                    if (path.equals(mTutinder.getLoggedInUser().getProfilepicturepath())) {
                        if (mTutinder.getLoggedInUser().getCloudinarypicturepaths(false).length > 0) {
                            mTutinder.getLoggedInUser().setProfilepicturepath(mTutinder.getLoggedInUser().getCloudinarypicturepaths(false)[0]);
                        } else {
                            mTutinder.getLoggedInUser().setProfilepicturepath("");
                        }
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.success_deleting_picture), Toast.LENGTH_LONG).show();

                    clearThumbnails();
                    initialisePictures();

                    saveProgressBar.setEnabled(false);
                    saveProgressBar.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_deleting_picture), Toast.LENGTH_LONG).show();
                    saveProgressBar.setEnabled(false);
                    saveProgressBar.setVisibility(View.GONE);
                }
            });

            mVolley.addToRequestQueue(request);
            saveProgressBar.setEnabled(true);
            saveProgressBar.setVisibility(View.VISIBLE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the logged in user.
     */
    private void makeUserUpdateRequest() {
        try {
            final User loggedInUser = mTutinder.getLoggedInUser();

            // Check Name
            String name = inName.getText().toString();
            if (!inputChecker.checkName(name, inName)) {
                return;
            }
            // Check Email
            String email = inEmail.getText().toString();
            if (!inputChecker.checkEmail(email, inEmail)) {
                return;
            }
            // Check Studycourse
            String studycourse = inStudycourse.getText().toString();
            if (!inputChecker.checkStudycourse(studycourse, inStudycourse)) {
                return;
            }
            // Check Password(s)
            String passwordOld = inPasswordOld.getText().toString();
            String passwordNew1 = inPassword.getText().toString();
            String passwordNew2 = inPassword2.getText().toString();
            if (passwordNew1.length() > 0 && passwordNew2.length() > 0) {
                if (inputChecker.checkPasswords(passwordNew1, passwordNew2, inPassword, inPassword2)) {
                    if (inputChecker.checkOldPassword(passwordOld, inPasswordOld)) {
                        // Write new Password into logged in User
                        loggedInUser.setNewPassword(passwordNew1);
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }

            // Write changes into logged in User
            loggedInUser.setName(name);
            loggedInUser.setEmail(email);
            loggedInUser.setStudycourse(studycourse);
            loggedInUser.setPhone(inPhone.getText().toString());
            loggedInUser.setStudycourse(inStudycourse.getText().toString());
            loggedInUser.setDescription(inDescription.getText().toString());

            // Set Personality Tags
            String[] persTags = resolvePersonalityTags();
            if (persTags == null) {
                throw new Exception("Not all Tags provided");
            }
            loggedInUser.setPersonalitytags(persTags);

            // Send Request
            Gson tmpGson = new Gson();
            String userJson = tmpGson.toJson(mTutinder.getLoggedInUser());
            System.out.print(userJson);
            GsonRequest req = new GsonRequest<User>(Request.Method.PUT, mVolley.getAPIRoot() + "/user", userJson, User.class, loggedInUser.getLoginCredentials(), new Response.Listener<User>() {

                @Override
                public void onResponse(User response) {
                    saveProgressBar.setEnabled(false);
                    saveProgressBar.setVisibility(View.GONE);

                    response.setPassword(loggedInUser.getPassword());
                    mTutinder.setLoggedInUser(response);

                    Toast.makeText(getApplicationContext(), "Save Successful", Toast.LENGTH_LONG).show();
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mTutinder.setLoggedInUser(null);
                    LoginChecker.logInCurrentUser(mContext, new LoginChecker.LoginCheckerListener() {
                        @Override
                        public void onLoginComplete(int status) {
                            if (status == LoginChecker.SUCCESS) {
                                finish();
                            }
                        }
                    });
                    saveProgressBar.setEnabled(false);
                    saveProgressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                        Toast.makeText(getApplicationContext(), R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
                    }
                }
            });
            mVolley.addToRequestQueue(req);
            saveProgressBar.setEnabled(true);
            saveProgressBar.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error_invalid_inputs, Toast.LENGTH_LONG).show();
        } catch (NotAllTagsException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error_notalltagsselected, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Uploads all changes.
     *
     * @param filename
     * @param bytes
     * @param contentType
     */
    private void makeUploadRequest(String filename, byte[] bytes, String contentType) {
        Map<String, DataPart> imageMap = new HashMap<>();
        // file name could found file base or direct access from real path
        // for now just get bitmap data from ImageView
        imageMap.put("profilePicture", new DataPart(filename, bytes, contentType));

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(mVolley.getAPIRoot() + "/user/userpicture", mTutinder.getLoggedInUser().getLoginCredentials(), imageMap, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                saveProgressBar.setEnabled(false);
                saveProgressBar.setVisibility(View.GONE);
                try {
                    String json = new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    Gson gson = new Gson();
                    User user = gson.fromJson(json, User.class);
                    user.setPassword(mTutinder.getLoggedInUser().getPassword());
                    mTutinder.setLoggedInUser(user);


                    clearThumbnails();
                    initialisePictures();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                saveProgressBar.setEnabled(false);
                saveProgressBar.setVisibility(View.GONE);
                if (error.networkResponse != null) {
                    Toast.makeText(EditAccountActivity.this, "" + error.networkResponse.statusCode + " " + EditAccountActivity.this.getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EditAccountActivity.this, R.string.strangeError, Toast.LENGTH_LONG).show();
                }
            }
        });
        mVolley.addToRequestQueue(multipartRequest);
        saveProgressBar.setEnabled(true);
        saveProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Makes an api call and loads all tags. After download it will display all tags.
     */
    private void requestTags() {
        String url = mVolley.getAPIRoot() + "/variables/tags/personality";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, url, null, null, new Response.Listener<String>() {
            @Override
            public void onResponse(String tags) {
                try {
                    if (tags == null) {
                        throw new Resources.NotFoundException("No Tags were found on the Server");
                    } else {
                        JSONArray jsonTags = new JSONArray(tags);
                        for (int i = 0; i < jsonTags.length(); i++) {
                            radioGroups.add(generateRadioGroup(jsonTags.getJSONObject(i)));
                        }
                        displayAllRadioGroups();
                        //addInterestsSearchView(tags.getJSONArray("interests"));
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
                Toast.makeText(getApplicationContext(), getString(R.string.error_tags_could_not_be_loaded) + ": PersonalityTagSet request error", Toast.LENGTH_LONG).show();
            }
        });
        mVolley.addToRequestQueue(request);
    }

    /**
     * Generates a RadioGroup from a given TagJsonObject and returns it.
     *
     * @return
     */
    public RadioGroup generateRadioGroup(JSONObject tag) throws JSONException {
        String value1 = null, value2 = null;
        String tagId, tagId1, tagId2;
        try {
            JSONArray tagsWrapper = tag.getJSONArray("tags");
            JSONArray languageTagsLeft = tagsWrapper.getJSONObject(0).getJSONArray("tag");
            JSONArray languageTagsRight = tagsWrapper.getJSONObject(1).getJSONArray("tag");
            tagId1 = tagsWrapper.getJSONObject(0).getString("_id");
            tagId2 = tagsWrapper.getJSONObject(1).getString("_id");
            for (int i = 0; i < languageTagsLeft.length(); i++) {
                JSONObject tagObject = languageTagsLeft.getJSONObject(i);
                if (tagObject.getString("lang").equals(Locale.getDefault().getLanguage().toUpperCase())) {
                    value1 = tagObject.getString("value");
                }
            }
            for (int i = 0; i < languageTagsRight.length(); i++) {
                JSONObject tagObject = languageTagsRight.getJSONObject(i);
                if (tagObject.getString("lang").equals(Locale.getDefault().getLanguage().toUpperCase())) {
                    value2 = tagObject.getString("value");
                }
            }
            tagId = tag.getString("_id");
            if (value1 == null || value2 == null)
                throw new JSONException("values were not provided correctly");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // generate RadioGroup
        RadioGroup radioGroup = new RadioGroup(this);
        //Set ID of Tag to RadioGroup
        radioGroup.setTag(tagId);
        RadioGroup.LayoutParams groupParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(0, 4, 0, 4);
        radioGroup.setLayoutParams(groupParams);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        // generate RadioButtons
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        for (int i = 0; i < 2; i++) {
            AppCompatRadioButton radioButton = new AppCompatRadioButton(this);
            radioButton.setLayoutParams(params);
            radioButton.generateViewId();
            radioButton.setTextSize(11);
            radioGroup.addView(radioButton);

            if (i == 0) {
                radioButton.setText(value1);
                radioButton.setTag(tagId1);
                if (Arrays.asList(mTutinder.getLoggedInUser().getPersonalitytags()).contains(tagId1)) {
                    radioButton.setChecked(true);
                    radioGroup.check(radioButton.getId());
                }
            } else {
                radioButton.setText(value2);
                radioButton.setTag(tagId2);
                if (Arrays.asList(mTutinder.getLoggedInUser().getPersonalitytags()).contains(tagId2)) {
                    radioButton.setChecked(true);
                    radioGroup.check(radioButton.getId());
                }
            }
        }
        return radioGroup;
    }


    /**
     * Displays all RadioGroups.
     */
    public void displayAllRadioGroups() {
        for (RadioGroup rg : radioGroups) {
            layoutTags.addView(rg);
        }
    }

    /**
     * Finds all checked RadioButtons and returns their Values as a String[]
     *
     * @return
     */
    private String[] resolvePersonalityTags() throws NotAllTagsException {
        ArrayList<String> persTags = new ArrayList<>();
        //tags
        if (!inputChecker.checkAllTagsSelected(radioGroups)) {
            throw new NotAllTagsException("Not all Tags selected");
        }
        //Find the checked Radiobutton and its Text for each radioGroup
        for (RadioGroup radioGroup : radioGroups) {
            int id = radioGroup.getCheckedRadioButtonId();
            radioGroup.getCheckedRadioButtonId();
            RadioButton rb = (RadioButton) findViewById(id);
            if (rb != null) {
                persTags.add(rb.getTag().toString());
            }
        }
        //Cast to String[] and return
        String[] persTagsArr = persTags.toArray(new String[persTags.size()]);
        return persTagsArr;
    }


}

