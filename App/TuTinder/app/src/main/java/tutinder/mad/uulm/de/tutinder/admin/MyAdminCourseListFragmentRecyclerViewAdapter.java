package tutinder.mad.uulm.de.tutinder.admin;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tutinder.mad.uulm.de.tutinder.R;

import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

import java.util.List;

import static android.support.v7.widget.RecyclerView.*;

/**
 * {@link Adapter} that can display a {@link Course} and makes a call to the
 * specified {@link AdminCourseListFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyAdminCourseListFragmentRecyclerViewAdapter extends RecyclerView.Adapter<MyAdminCourseListFragmentRecyclerViewAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private AdminCourseListFragment.OnListFragmentInteractionListener mListener;
    private VolleySingleton volleySingleton;

    public MyAdminCourseListFragmentRecyclerViewAdapter(List<Course> courseList, AdminCourseListFragment.OnListFragmentInteractionListener listener, VolleySingleton volleySingleton) {
        this.courseList = courseList;
        this.mListener = listener;
        this.volleySingleton = volleySingleton;

    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        holder.bind(courseList.get(position));
    }


    @Override
    public int getItemCount() {
        return courseList.size();
    }


    public class CourseViewHolder extends ViewHolder {

        private TextView courseName;
        private TextView courseTerm;
        private CircleNetworkImageView coursePicture;
        private ImageButton courseButton;
        private RelativeLayout cardLayout;
        private Course courseItem;

        public CourseViewHolder(final View v) {
            super(v);
            courseName = (TextView) v.findViewById(R.id.listitem_coursename);
            courseTerm = (TextView) v.findViewById(R.id.listitem_term);

            coursePicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            coursePicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            coursePicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);

            courseButton = (ImageButton) v.findViewById(R.id.listitem_button);
            courseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO
                }
            });

            cardLayout = (RelativeLayout) v.findViewById(R.id.listitem_layout);
            cardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListFragmentInteraction(courseItem, v, "click");
                }
            });
            cardLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    return true;
                }
            });
        }

        public void bind(Course course) {
            courseItem = course;
            courseName.setText(courseItem.getName());
            courseTerm.setText(courseItem.getTerm());
            coursePicture.setImageUrl(courseItem.getThumbnailpath(), volleySingleton.getImageLoader());
        }


    }
}
