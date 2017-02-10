package tutinder.mad.uulm.de.tutinder.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.CourseActivity;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * Created by Lukas on 13.06.2016.
 */
public class SearchListAdapter extends RecyclerView.Adapter<SearchListAdapter.CourseViewHolder> {

    private Tutinder helper;
    private Context context;
    private VolleySingleton volleySingleton;
    private List<Course> searchList;

    public SearchListAdapter(Context context, List<Course> searchList) {
        this.context = context;
        this.helper = Tutinder.getInstance();
        this.volleySingleton = VolleySingleton.getInstance(this.context);
        this.searchList = searchList;
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(context)
                .inflate(R.layout.recycler_item_course, parent, false);
        return new CourseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        holder.bind(searchList.get(position));
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {

        private TextView courseName;
        private TextView courseTerm;
        private CircleNetworkImageView coursePicture;
        private ImageButton courseButton;
        private RelativeLayout cardLayout;

        private Course courseItem;
        private Boolean isEnrolled;

        public CourseViewHolder(View itemView) {
            super(itemView);
            courseName = (TextView) itemView.findViewById(R.id.listitem_coursename);
            courseTerm = (TextView) itemView.findViewById(R.id.listitem_term);

            coursePicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            coursePicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            coursePicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);

            courseButton = (ImageButton) itemView.findViewById(R.id.listitem_button);
            courseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isEnrolled) showLeaveDialog();
                    else showEnrollDialog();
                }
            });

            cardLayout = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isEnrolled){
                        Intent intent = new Intent(context, CourseActivity.class);
                        intent.putExtra("courseid", courseItem.get_id());
                        context.startActivity(intent);
                    }else{
                        showEnrollDialog();
                    }
                }
            });
        }

        public void bind(Course course) {
            courseItem = course;
            courseName.setText(courseItem.getName());
            courseTerm.setText(courseItem.getTerm());
            coursePicture.setImageUrl(courseItem.getThumbnailpath(), volleySingleton.getImageLoader());
            isEnrolled = helper.getLoggedInUser().isEnrolledInCourse(courseItem.get_id());
            if(isEnrolled) {
                courseButton.setImageResource(R.drawable.ic_highlight_off_red_24dp);
            }else {
                courseButton.setImageResource(R.drawable.ic_add_circle_outline_accent_24dp);
            }
        }

        public void showEnrollDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.TuTinder_Dialog_Progress);
            builder
                    .setTitle(R.string.dialog_title_enroll)
                    .setMessage(R.string.dialog_message_enroll)
                    .setPositiveButton(R.string.btn_enroll, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setMessage(context.getString(R.string.dialog_message_enrolling));
                            progressDialog.show();
                            requestEnroll(progressDialog);
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

        public void showLeaveDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.TuTinder_Dialog_Progress);
            builder
                    .setTitle(R.string.dialog_title_leave)
                    .setMessage(R.string.dialog_message_leave)
                    .setPositiveButton(R.string.btn_leave, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setMessage(context.getString(R.string.dialog_message_leaving));
                            progressDialog.show();
                            requestLeave(progressDialog);

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

        private void requestEnroll(final ProgressDialog dialog) {
            final String URL = volleySingleton.getAPIRoot() + "/user/courses/" + courseItem.get_id() + "/enroll/";
            GsonRequest<User> request = new GsonRequest<User>(Request.Method.POST, URL, null, User.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
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
            volleySingleton.addToRequestQueue(request);
        }

        private void requestLeave(final ProgressDialog dialog) {
            final String URL = volleySingleton.getAPIRoot() + "/user/courses/" + courseItem.get_id() + "/leave/";
            GsonRequest<User> request = new GsonRequest<User>(Request.Method.POST, URL, null, User.class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<User>() {
                @Override
                public void onResponse(User user) {
                    user.setPassword(helper.getLoggedInUser().getPassword());
                    helper.setLoggedInUser(user);
                    notifyDataSetChanged();
                    dialog.dismiss();
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
            volleySingleton.addToRequestQueue(request);
        }
    }
}
