package tutinder.mad.uulm.de.tutinder.admin;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.FriendListAdapter;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.DataPart;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.VolleyMultipartRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Faculty;
import tutinder.mad.uulm.de.tutinder.models.Institute;
import tutinder.mad.uulm.de.tutinder.models.Timeslot;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

public class AdminCourseActivity extends AppCompatActivity implements TimeslotAdapter.OnRecyclerInteractionListener {


    public final int REQUEST_CAMERA_PERMISSIONS = 0;
    public final int REQUEST_GALLERY_PERMISSIONS = 1;

    private static final int IMAGE_SELECT_BY_CAMERA = 0;
    private static final int IMAGE_SELECT_BY_GALLERY = 1;

    private Context context;
    private Tutinder helper;
    private VolleySingleton volleySingleton;
    private ProgressBar loadingProgressBar;
    private CollapsingToolbarLayout layoutCollapsingToolbar;
    private AppBarLayout layoutAppBar;
    private NetworkImageView ivCoursepicture;
    private RecyclerView rvEnrolledUsers;
    private TextInputEditText inDescription;
    private LinearLayout llTimeslots;
    private TextInputEditText inLecturer;
    List<String> facultiesList;
    List<Faculty> facultiesListObj;
    List<Institute> institutesListObj;
    List<String> instituteslist;
    private String courseid;
    private Course course;
    private TextInputEditText inName;
    private TextInputEditText inTerm;
    private TextInputEditText inMaxGroupSize;
    private Spinner spInstitute;
    private Spinner spFaculty;
    private List<View> timeslotviews;
    private FloatingActionButton fabCoursePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_course);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();
        helper = Tutinder.getInstance();
        volleySingleton = VolleySingleton.getInstance(context);

        layoutCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsingtoolbar);
        layoutAppBar = (AppBarLayout) findViewById(R.id.layout_appbar);
        llTimeslots = (LinearLayout) findViewById(R.id.ll_timeslots);
        ivCoursepicture = (NetworkImageView) findViewById(R.id.iv_coursepicture);
        ivCoursepicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_500dp);
        ivCoursepicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_500dp);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        fabCoursePicture = (FloatingActionButton) findViewById(R.id.fab_coursepicture);
        rvEnrolledUsers = (RecyclerView) findViewById(R.id.recycler_enrolledusers);
        inDescription = (TextInputEditText) findViewById(R.id.in_description);

        inLecturer = (TextInputEditText) findViewById(R.id.in_lecturer);
        inName = (TextInputEditText) findViewById(R.id.in_name);
        inTerm = (TextInputEditText) findViewById(R.id.in_term);
        inMaxGroupSize = (TextInputEditText) findViewById(R.id.in_maxgroupsize);
        spFaculty = (Spinner) findViewById(R.id.sp_faculty);
        spInstitute = (Spinner) findViewById(R.id.sp_institute);
        fabCoursePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminCourseActivity.this, R.style.TuTinder_Dialog_Alert);
                builder.setTitle(R.string.add_Picture);
                final CharSequence[] items = {getString(R.string.action_select_photo), getString(R.string.action_take_photo)};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals(getString(R.string.action_take_photo))) {
                            requestCameraPermissions();
                        } else if (items[item].equals(getString(R.string.action_select_photo))) {
                            requestGalleryPermissions();
                        }
                    }
                });
                builder.setPositiveButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            courseid = extras.getString("courseid");

            if (courseid != null) {
                makeCourseRequest(courseid);
            }
        } else {
            course = new Course();
        }
        makeFacultyRequest();
        initializeNewTimeSlotView(getSpinneradapter());

    }

    private void makeFacultyRequest() {
        final String URL = volleySingleton.getAPIRoot() + "/faculties";
        GsonRequest<Faculty[]> requestCourse = new GsonRequest<Faculty[]>(Request.Method.GET, URL, null, Faculty[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Faculty[]>() {
            @Override
            public void onResponse(Faculty[] faculties) {
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getApplicationContext(), R.layout.custom_spinner_item);

// Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(R.layout.custom_spinner_item);
// Apply the adapter to the spinner
                facultiesList = new ArrayList<>();
                facultiesListObj = new ArrayList<>();

                for (Faculty faculty :
                        faculties) {
                    facultiesListObj.add(faculty);
                    facultiesList.add(faculty.getName());

                }
                adapter.addAll(facultiesList);
                spFaculty.setAdapter(adapter);
                if (course != null) {
                    for (int i = 0; i < facultiesListObj.size(); i++) {
                        if (course.getFaculty() != null) {
                            if (facultiesListObj.get(i).get_id().equals(course.getFaculty().get_id())) {
                                if (spFaculty != null) {
                                    spFaculty.setSelection(i);
                                }
                            }

                        }

                    }
                }
                spFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        instituteslist = new ArrayList<>();
                        institutesListObj = new ArrayList<Institute>();
                        for (Institute institute :
                                facultiesListObj.get(position).getInstitutes()) {
                            institutesListObj.add(institute);
                            instituteslist.add(institute.getName());
                        }
                        ArrayAdapter<CharSequence> adapteri = new ArrayAdapter<CharSequence>(getApplicationContext(), R.layout.custom_spinner_item);
                        adapteri.setDropDownViewResource(R.layout.custom_spinner_item);
                        adapteri.addAll(instituteslist);
                        spInstitute.setAdapter(adapteri);
                        if (course != null) {
                            for (int i = 0; i < institutesListObj.size(); i++) {
                                if (course.getFaculty() != null && course.getFaculty().getInstitute() != null) {
                                    if (institutesListObj.get(i).get_id().equals(course.getFaculty().getInstitute().get_id())) {
                                        if (spInstitute != null) {
                                            spInstitute.setSelection(i);
                                        }
                                    }

                                }

                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        spInstitute.setAdapter(null);
                    }
                });

                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Toast.makeText(context, error.networkResponse.statusCode + " " + getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    //TODO Remove error.getMessage() in Production
                    Toast.makeText(context, getString(R.string.strangeError) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideProgressBar();
                finish();
            }
        });
        volleySingleton.addToRequestQueue(requestCourse);
        showProgressBar(getString(R.string.dialog_message_loading));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_editaccout_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (courseid != null) {
                saveCourseOnline();
            } else {
                createCourseOnline();
            }
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Shows the activities loading ProgressDialog.
     */
    private void showProgressBar(String msg) {
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
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void makeCourseRequest(String courseId) {
        final String URL = volleySingleton.getAPIRoot() + "/courses/" + courseId;
        GsonRequest<Course> requestCourse = new GsonRequest<Course>(Request.Method.GET, URL, null, Course.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course>() {
            @Override
            public void onResponse(Course response) {
                onRequestResponseCourse(response);
                hideProgressBar();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Toast.makeText(context, error.networkResponse.statusCode + " " + getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    //TODO Remove error.getMessage() in Production
                    Toast.makeText(context, getString(R.string.strangeError) + ":" + error.getMessage(), Toast.LENGTH_LONG).show();
                }
                hideProgressBar();

            }
        });
        volleySingleton.addToRequestQueue(requestCourse);
        showProgressBar(getString(R.string.dialog_message_loading));
    }

    private void onRequestResponseCourse(Course response) {
        // set course
        course = response;
        course.getTimeslotObjects(volleySingleton, helper, getApplicationContext(), new Course.CourseTimeslotListener() {
            @Override
            public void onTimeslotObjectsLoaded(Timeslot[] timelostobjects) {
                if (timelostobjects != null) {
                    initializeTimeSlots(Arrays.asList(timelostobjects), getSpinneradapter());
                }
            }
        });
        makeFacultyRequest();
        // set toolbar title
        layoutCollapsingToolbar.setTitle("Update: " + course.getName());
        // set pictures
        ivCoursepicture.setImageUrl(course.getPicturepath(volleySingleton.getAPIRoot()), volleySingleton.getImageLoader());

        String name = course.getName();
        if (name != null) inName.setText(name);
        String term = course.getTerm();
        if (term != null) inTerm.setText(term);
        // set textviews
        int maxgroupsize = course.getMaxgroupsize();
        if (maxgroupsize != 0) {
            inMaxGroupSize.setText(maxgroupsize + "");
        }
        String description = course.getDescription();
        if (description != null) inDescription.setText(description);
        String lecturer = course.getLecturer();
        if (lecturer != null) inLecturer.setText(lecturer);
        // set courses
        User[] users = course.getEnrolledusers(true);
        if (users != null) {
            rvEnrolledUsers.setAdapter(new FriendListAdapter(AdminCourseActivity.this, Arrays.asList(users), new OnListItemInteractListener() {
                @Override
                public void onListItemInteract(String method, Bundle args) {
                    // TODO @snap10
                }

                @Override
                public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder) {

                }
            }));
        }

    }

    private void initializeNewTimeSlotView(SpinnerAdapter daysAdapter) {
        View timeslotviewNew = getLayoutInflater().inflate(R.layout.form_course_timeslot, llTimeslots, true);
        final Spinner daySpinnerNew = (Spinner) timeslotviewNew.findViewById(R.id.sp_day);
        daySpinnerNew.setAdapter(daysAdapter);
        final TextView toClockNew = (TextView) timeslotviewNew.findViewById(R.id.in_to);
        toClockNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showTimePicker((TextView) v);
            }
        });
        final TextView fromClockNew = (TextView) timeslotviewNew.findViewById(R.id.in_from);
        fromClockNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showTimePicker((TextView) v);
            }
        });
        final ImageButton addTimeSlotButtonNew = (ImageButton) findViewById(R.id.listitem_button);
        addTimeSlotButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timeslot tsnew = new Timeslot();
                tsnew.setDay(daySpinnerNew.getSelectedItemPosition() - 1);
                tsnew.setFrom((String) fromClockNew.getTag());
                tsnew.setTo((String) toClockNew.getTag());
                addTimeSlotToCourse(tsnew);
            }
        });
    }

    private void addTimeSlotToCourse(Timeslot tsnew) {
        Gson gson = new Gson();
        String body = gson.toJson(tsnew);
        GsonRequest<Timeslot> tsRequest = new GsonRequest<>(Request.Method.POST, volleySingleton.getAPIRoot() + "/variables/timeslots", body, Timeslot.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Timeslot>() {
            @Override
            public void onResponse(Timeslot response) {
                course.addTimeSlot(response);
                List<Timeslot> timeslots;
                course.getTimeslotObjects(volleySingleton, helper, getApplicationContext(), new Course.CourseTimeslotListener() {
                    @Override
                    public void onTimeslotObjectsLoaded(Timeslot[] timelostobjects) {
                        if (timelostobjects != null) {
                            initializeTimeSlots(Arrays.asList(timelostobjects), getSpinneradapter());
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AdminCourseActivity.this, "TODO Error adding a Timeslot", Toast.LENGTH_LONG).show();
            }
        });
        volleySingleton.addToRequestQueue(tsRequest);
        //TODO Progressbar
    }

    private SpinnerAdapter getSpinneradapter() {
        String[] daysArr = new String[]{getString(R.string.choose), getString(R.string.monday), getString(R.string.tuesday), getString(R.string.wednesday), getString(R.string.thursday), getString(R.string.friday), getString(R.string.saturday), getString(R.string.sunday)};
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_item);
        daysAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        daysAdapter.addAll(daysArr);
        return daysAdapter;
    }


    private void initializeTimeSlots(List<Timeslot> timeslotlist, SpinnerAdapter spinneradapter) {

        timeslotviews = new ArrayList<>();
        RecyclerView recyclerViewTimeSlots = (RecyclerView) findViewById(R.id.recycler_timeslots);
        recyclerViewTimeSlots.setLayoutManager(new LinearLayoutManager(AdminCourseActivity.this));
        recyclerViewTimeSlots.setAdapter(new TimeslotAdapter(spinneradapter, timeslotlist, this, volleySingleton));

    }

    private void showTimePicker(final TextView v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(AdminCourseActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                TextView clock = v;
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String time = sdf.format(c.getTime());
                clock.setTag(time);
                clock.setText(time);
            }
        }, 0, 0, true);
        timePickerDialog.show();
    }


    private void saveCourseOnline() {
        Gson gson = new Gson();
        collectInputs();
        String coursejson = gson.toJson(course, Course.class);
        GsonRequest<Course> updateCourse = new GsonRequest<>(Request.Method.PUT, volleySingleton.getAPIRoot() + "/courses/" + course.get_id(), coursejson, Course.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course>() {
            @Override
            public void onResponse(Course response) {
                hideProgressBar();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressBar();
            }
        });
        volleySingleton.addToRequestQueue(updateCourse);
        showProgressBar("");
    }

    private void createCourseOnline() {
        Gson gson = new Gson();
        collectInputs();
        String coursejson = gson.toJson(course, Course.class);
        GsonRequest<Course> updateCourse = new GsonRequest<>(Request.Method.POST, volleySingleton.getAPIRoot() + "/courses/", coursejson, Course.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course>() {
            @Override
            public void onResponse(Course response) {
                course = response;
                onRequestResponseCourse(course);
                hideProgressBar();
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressBar();
            }
        });
        volleySingleton.addToRequestQueue(updateCourse);
        showProgressBar("");
    }


    private void collectInputs() {
        Faculty fac = facultiesListObj.get(spFaculty.getSelectedItemPosition());
        Institute inst = institutesListObj.get(spInstitute.getSelectedItemPosition());
        course.setFacultyid(fac.get_id());
        course.setInstituteid(inst.get_id());
        course.setName(inName.getText().toString());
        course.setDescription(inDescription.getText().toString());
        course.setTerm(inTerm.getText().toString());
        course.setLecturer(inLecturer.getText().toString());
        try {
            course.setMaxgroupsize(Integer.parseInt(inMaxGroupSize.getText().toString()));
        } catch (NumberFormatException e) {
            course.setMaxgroupsize(-1);
        }
    }

    @Override
    public void onListFragmentInteraction(Timeslot item, View v, String action) {
        if (action.equals("delete")) {
            makeRemoveRequest(item.get_id(), courseid);
        }

    }

    private void makeRemoveRequest(final String id, String courseid) {

        GsonRequest<Timeslot> tsRequest = new GsonRequest<>(Request.Method.DELETE, volleySingleton.getAPIRoot() + "/courses/" + courseid + "/timeslots/" + id, null, Timeslot.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Timeslot>() {
            @Override
            public void onResponse(Timeslot response) {
                course.removeTimeslot(id);
                course.getTimeslotObjects(volleySingleton, helper, getApplicationContext(), new Course.CourseTimeslotListener() {
                    @Override
                    public void onTimeslotObjectsLoaded(Timeslot[] timelostobjects) {
                        if (timelostobjects != null) {
                            initializeTimeSlots(Arrays.asList(timelostobjects), getSpinneradapter());
                        }
                    }
                });
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AdminCourseActivity.this, "TODO Error deleting a Timeslot", Toast.LENGTH_LONG).show();
            }
        });
        volleySingleton.addToRequestQueue(tsRequest);
    }


    /**
     * PICTURE SECTION
     */
    /**
     * Uploads all changes.
     *
     * @param filename
     * @param bytes
     * @param contentType
     */
    private void makeUploadRequest(String filename, byte[] bytes, String contentType) {
        //TODO upload
        Map<String, DataPart> imageMap = new HashMap<>();
        // file name could found file base or direct access from real path
        // for now just get bitmap data from ImageView
        imageMap.put("profilePicture", new DataPart(filename, bytes, contentType));

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(volleySingleton.getAPIRoot() + "/courses/" + courseid + "/uploadpicturetocloud", helper.getLoggedInUser().getLoginCredentials(), imageMap, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                showProgressBar("");

                try {
                    String json = new String(
                            response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    Gson gson = new Gson();
                    Course coursetmp = gson.fromJson(json, Course.class);
                    course = coursetmp;
                    ivCoursepicture.setImageUrl(course.getPicturepath(volleySingleton.getAPIRoot()), volleySingleton.getImageLoader());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgressBar();
                if (error.networkResponse != null) {
                    Toast.makeText(AdminCourseActivity.this, "" + error.networkResponse.statusCode + " " + AdminCourseActivity.this.getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AdminCourseActivity.this, R.string.strangeError, Toast.LENGTH_LONG).show();
                }
            }
        });
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 2, 1));
        volleySingleton.addToRequestQueue(multipartRequest);
        showProgressBar("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_SELECT_BY_CAMERA) {
                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bm;
                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

                    bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            btmapOptions);
                    //For now the image just gets scaeld by Cloudinary...
                    //bm = Bitmap.createScaledBitmap(bm, 1500, 1500, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    byte[] byteArray = stream.toByteArray();
                    String filename = String.valueOf(System
                            .currentTimeMillis()) + ".jpg";
                    makeUploadRequest(filename, byteArray, "image/jpg");
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + getString(R.string.app_name) + File.separator + "default";
                    f.delete();

                } catch (Exception e) {
                    //Todo
                    e.printStackTrace();
                }
            } else if (requestCode == IMAGE_SELECT_BY_GALLERY) {
                Uri selectedImageUri = data.getData();

                String tempPath = getPath(selectedImageUri, AdminCourseActivity.this);
                Bitmap bm;
                BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
                //For now the image just gets scaeld by Cloudinary...
                //bm = Bitmap.createScaledBitmap(bm, 1500, 1500, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                byte[] byteArray = stream.toByteArray();
                String filename = tempPath.substring(tempPath.lastIndexOf('/') + 1, tempPath.length());
                makeUploadRequest(filename, byteArray, "image/jpg");

            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSIONS:
                if (allPermissionsGranted(grantResults)) selectImageByCamera();
                else
                    Toast.makeText(getApplicationContext(), R.string.error_permissions_denied, Toast.LENGTH_LONG).show();
                break;
            case REQUEST_GALLERY_PERMISSIONS:
                if (allPermissionsGranted(grantResults)) selectImageByGallery();
                else
                    Toast.makeText(getApplicationContext(), R.string.error_permissions_denied, Toast.LENGTH_LONG).show();
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
            boolean askPermissions = false;
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


}
