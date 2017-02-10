package tutinder.mad.uulm.de.tutinder.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.Arrays;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.CourseActivity;
import tutinder.mad.uulm.de.tutinder.adapters.customItems.FacultyListItem;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

public class ExpandableCourseListAdapter extends ExpandableRecyclerAdapter<ExpandableCourseListAdapter.FacultyViewHolder, ExpandableCourseListAdapter.CourseViewHolder> {

    Tutinder helper = Tutinder.getInstance();
    VolleySingleton volleySingleton;
    private final Context context;
    private LayoutInflater mInflator;

    /**
     * Primary constructor. Sets up {@link #mParentItemList} and {@link #mItemList}.
     * <p/>
     * Changes to {@link #mParentItemList} should be made through add/remove methods in
     * {@link ExpandableRecyclerAdapter}
     *
     * @param parentItemList List of all {@link ParentListItem} objects to be
     *                       displayed in the RecyclerView that this
     *                       adapter is linked to
     */
    public ExpandableCourseListAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        this.context = context;
        volleySingleton = VolleySingleton.getInstance(context);
        mInflator = LayoutInflater.from(context);
    }


    // onCreate ...
    @Override
    public FacultyViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View facultyView = mInflator.inflate(R.layout.recycler_item_faculty, parentViewGroup, false);
        return new FacultyViewHolder(facultyView);
    }

    @Override
    public CourseViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View courseView = mInflator.inflate(R.layout.recycler_subitem_course, childViewGroup, false);
        return new CourseViewHolder(courseView);
    }

    // onBind ...
    @Override
    public void onBindParentViewHolder(FacultyViewHolder facultyViewHolder, int position, ParentListItem parentListItem) {
        FacultyListItem faculty = (FacultyListItem) parentListItem;
        facultyViewHolder.bind(faculty);
    }

    @Override
    public void onBindChildViewHolder(CourseViewHolder courseViewHolder, int position, Object childListItem) {
        Course course = (Course) childListItem;
        courseViewHolder.bind(course);
    }


    public class FacultyViewHolder extends ParentViewHolder {

        private LinearLayout cardLayout;
        private TextView tvCourseCount;
        private TextView facultyName;
        private AppCompatImageView arrowView;
        private FacultyListItem facultyListItem;
        private ProgressBar progressBar;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public FacultyViewHolder(View itemView) {
            super(itemView);
            cardLayout = (LinearLayout) itemView.findViewById(R.id.listitem_layout);
            facultyName = (TextView) itemView.findViewById(R.id.listitem_facultyname);
            progressBar = (ProgressBar) itemView.findViewById(R.id.courses_loading_progressbar);
            tvCourseCount = (TextView) itemView.findViewById(R.id.textView_coursecount);
            arrowView = (AppCompatImageView) itemView.findViewById(R.id.list_arrow_down);
        }

        public void bind(FacultyListItem faculty) {
            facultyName.setText(faculty.getFaculty().getName());
            this.facultyListItem = faculty;
            int courseCount = this.facultyListItem.getFaculty().getCourseCount();
            if(courseCount==1){
                tvCourseCount.setText(courseCount + " " + context.getString(R.string.course));

            }else{

                tvCourseCount.setText(courseCount + " " + context.getString(R.string.courses));
            }
            if (courseCount == 0) {
                arrowView.setVisibility(View.GONE);
            } else {
                arrowView.setVisibility(View.VISIBLE);
                cardLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExpanded()) {
                            collapseView();
                        } else {
                            //Workaround for woring Index in Library...
                            collapseAllParents();
                            if (facultyListItem.getCourseList() == null) {
                                progressBar.setIndeterminate(true);
                                progressBar.setEnabled(true);
                                progressBar.setVisibility(View.VISIBLE);
                                makeCourseRequest(facultyListItem, getAdapterPosition(), progressBar);
                            }
                            expandView();
                        }
                    }
                });
            }
        }
    }

    private void makeCourseRequest(final FacultyListItem facultyListItem, final int position, final ProgressBar progressBar) {

        VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
        GsonRequest<Course[]> requestCourses = new GsonRequest<Course[]>(Request.Method.GET, volleySingleton.getAPIRoot() + "/courses/faculties/" + facultyListItem.getFaculty().get_id(), null, Course[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course[]>() {
            @Override
            public void onResponse(Course[] courseList) {
                progressBar.setEnabled(false);
                progressBar.setVisibility(View.GONE);
                facultyListItem.setCourseList(Arrays.asList(courseList));
                notifyChildItemRangeInserted(position, 0, courseList.length);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setEnabled(false);
                progressBar.setVisibility(View.GONE);
                if (error.networkResponse != null) {
                    Toast.makeText(context, "" + error.networkResponse.statusCode + " " + context.getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.strangeError, Toast.LENGTH_LONG).show();
                }
            }
        });

        volleySingleton.addToRequestQueue(requestCourses);
    }


    public class CourseViewHolder extends ChildViewHolder {

        private TextView courseName;
        private TextView courseTerm;
        private CircleNetworkImageView coursePicture;
        private ImageButton courseButton;
        private Course courseItem;
        private RelativeLayout cardLayout;

        private Boolean isEnrolled;

        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public CourseViewHolder(final View itemView) {
            super(itemView);
            cardLayout = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            courseName = (TextView) itemView.findViewById(R.id.listitem_coursename);
            courseTerm = (TextView) itemView.findViewById(R.id.listitem_term);
            coursePicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            coursePicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            coursePicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            courseButton = (ImageButton) itemView.findViewById(R.id.listitem_button);

            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEnrolled == true) {
                        Intent intent = new Intent(context, CourseActivity.class);
                        intent.putExtra("courseid", courseItem.get_id());
                        context.startActivity(intent);
                    } else {
                        showEnrollDialog();
                    }
                }
            });
            courseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (isEnrolled == false) {
                        showEnrollDialog();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.TuTinder_Dialog_Progress);
                        builder
                                .setTitle(R.string.dialog_title_leave)
                                .setMessage(R.string.dialog_message_leave)
                                // .setIcon(R.drawable.ic_logout_accent_24dp)
                                .setPositiveButton(R.string.btn_leave, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setMessage(context.getString(R.string.dialog_message_leaving));
                                        progressDialog.show();
                                        makeLeaveRequest(courseItem, progressDialog);

                                    }
                                })
                                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog leaveDialog = builder.create();
                        leaveDialog.show();
                    }
                }


            });
        }

        private void showEnrollDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.TuTinder_Dialog_Progress);
            builder
                    .setTitle(R.string.dialog_title_enroll)
                    .setMessage(R.string.dialog_message_enroll)
                    // .setIcon(R.drawable.ic_logout_accent_24dp)
                    .setPositiveButton(R.string.btn_enroll, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setMessage(context.getString(R.string.dialog_message_enrolling));
                            progressDialog.show();
                            makeEnrollRequest(courseItem, progressDialog);
                        }
                    })
                    .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog enrollDialog = builder.create();
            enrollDialog.show();
        }

        public void bind(Course course) {
            courseItem = course;
            courseName.setText(courseItem.getName());
            courseTerm.setText(courseItem.getTerm());
            coursePicture.setImageUrl(course.getThumbnailpath(), volleySingleton.getImageLoader());

            if (helper.getLoggedInUser().isEnrolledInCourse(courseItem.get_id())) {
                isEnrolled = true;
                courseButton.setImageResource(R.drawable.ic_highlight_off_red_24dp);
            } else {
                isEnrolled = false;
                courseButton.setImageResource(R.drawable.ic_add_circle_outline_accent_24dp);
            }
        }
    }

    private void makeEnrollRequest(final Course courseItem, final DialogInterface dialog) {
        VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
        GsonRequest<User> requestCourses = new GsonRequest<User>(Request.Method.POST, volleySingleton.getAPIRoot() + "/user/courses/" + courseItem.get_id() + "/enroll/", null, User.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User user) {
                user.setPassword(helper.getLoggedInUser().getPassword());
                helper.setLoggedInUser(user);
                notifyDataSetChanged();
                dialog.dismiss();

                Intent intent = new Intent(context, CourseActivity.class);
                intent.putExtra("courseid", courseItem.get_id());
                intent.putExtra("isinitial", true);
                context.startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Toast.makeText(context, "" + error.networkResponse.statusCode + " " + context.getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.strangeError, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });
        requestCourses.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        volleySingleton.addToRequestQueue(requestCourses);
    }

    private void makeLeaveRequest(Course courseItem, final DialogInterface dialog) {


        VolleySingleton volleySingleton = VolleySingleton.getInstance(context);
        GsonRequest<User> requestCourses = new GsonRequest<User>(Request.Method.POST, volleySingleton.getAPIRoot() + "/user/courses/" + courseItem.get_id() + "/leave/", null, User.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
            @Override
            public void onResponse(User user) {

                user.setPassword(helper.getLoggedInUser().getPassword());
                helper.setLoggedInUser(user);
                dialog.dismiss();
                notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Toast.makeText(context, "" + error.networkResponse.statusCode + " " + context.getString(R.string.strangeError), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.strangeError, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        });
        requestCourses.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        volleySingleton.addToRequestQueue(requestCourses);
    }


}