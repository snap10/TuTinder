package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.CourseActivity;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * RecyclerAdapter for Course Items.
 *
 * @author 1uk4s
 * @author snap10
 */
public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.CourseViewHolder> {

    private Context mContext;
    private VolleySingleton mVolley;

    private List<Course> itemList;

    private OnListItemInteractListener listener;


    /**
     * Default Constructor.
     *
     * @param context
     * @param courseList
     * @param listener
     */
    public CourseListAdapter(Context context, List<Course> courseList, OnListItemInteractListener listener) {
        this.mContext = context;
        this.itemList = courseList;
        this.mVolley = VolleySingleton.getInstance(context);
        this.listener = listener;
    }

    /**
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_default, parent, false);
        return new CourseViewHolder(itemView);
    }

    /**
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        holder.bind(itemList.get(position));
    }

    /**
     * @return
     */
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Updates the itemList of the Adapter.
     *
     * @param courseList
     */
    public void setItemList(List<Course> courseList) {
        this.itemList = courseList;
        notifyDataSetChanged();
    }


    /**
     * ViewHolder of CourseListAdapter.
     *
     * @author 1uk4s
     * @author snap10
     */
    public class CourseViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rootView;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle;
        private ImageButton btnAction;

        private Course courseItem;

        /**
         * Default Constructor.
         *
         * @param itemView
         */
        public CourseViewHolder(final View itemView) {
            super(itemView);

            rootView = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_500dp);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);
            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);

        }

        /**
         * Binds a Course Object to this ViewHolder Instance.
         *
         * @param course
         */
        public void bind(Course course) {
            courseItem = course;

            tvTitle.setText(courseItem.getName());
            tvSubtitle.setText(courseItem.getTerm());
            ivPicture.setImageUrl(courseItem.getThumbnailpath(), mVolley.getImageLoader());

            rootView = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, CourseActivity.class);
                    intent.putExtra("courseid", courseItem.get_id());
                    mContext.startActivity(intent);
                }
            });
            rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupMenu();
                    return true;
                }
            });

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu();
                }
            });
        }

        /**
         * Shows a PopupMenu at this ViewHolder Instances Position.
         */
        private void showPopupMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, btnAction);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_course_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_show_course:
                            Bundle argsCourse = new Bundle();
                            argsCourse.putString("courseid", courseItem.get_id());
                            listener.onListItemInteract(mContext.getString(R.string.action_show_course), argsCourse);
                            return true;
                        case R.id.action_search_for_partners:
                            Bundle argsSearch = new Bundle();
                            argsSearch.putString("courseid", courseItem.get_id());
                            argsSearch.putString("coursename", courseItem.getName());
                            argsSearch.putString("coursethumbnailpath", courseItem.getThumbnailpath());
                            listener.onListItemInteract(mContext.getString(R.string.action_search_for_partners), argsSearch);
                            return true;
                        case R.id.action_leave_course:
                            Bundle argsLeave = new Bundle();
                            argsLeave.putString("courseid", courseItem.get_id());
                            argsLeave.putString("coursename", courseItem.getName());
                            listener.onListItemInteract(mContext.getString(R.string.action_leave_course), argsLeave);
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        }
    }
}
