package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.security.InvalidParameterException;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.GroupActivity;
import tutinder.mad.uulm.de.tutinder.activities.MatchActivity;
import tutinder.mad.uulm.de.tutinder.adapters.customItems.MatchParentListItem;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.CustomListitem;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.Types;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * @author 1uk4s
 * @author snap10
 */
public class ExpandableMatchListAdapter extends ExpandableRecyclerAdapter<ExpandableMatchListAdapter.CourseViewHolder, ExpandableMatchListAdapter.RatingViewHolder> {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private LayoutInflater mLayoutInfalter;

    private OnListItemInteractListener mListener;


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
    public ExpandableMatchListAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList, OnListItemInteractListener listener) {
        super(parentItemList);
        this.mContext = context;
        this.mTutinder = Tutinder.getInstance();
        this.mVolley = VolleySingleton.getInstance(this.mContext);
        this.mLayoutInfalter = LayoutInflater.from(this.mContext);
        this.mListener = listener;
    }

    /**
     *
     * @param parentViewGroup
     * @return
     */
    @Override
    public CourseViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View parentView = mLayoutInfalter.inflate(R.layout.recycler_item_parent, parentViewGroup, false);
        return new CourseViewHolder(parentView);
    }

    /**
     *
     * @param childViewGroup
     * @return
     */
    @Override
    public RatingViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View childView = mLayoutInfalter.inflate(R.layout.recycler_item_child, childViewGroup, false);
        return new RatingViewHolder(childView, this);
    }

    /**
     *
     * @param parentViewHolder
     * @param position
     * @param parentListItem
     */
    @Override
    public void onBindParentViewHolder(CourseViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        MatchParentListItem match = (MatchParentListItem) parentListItem;
        parentViewHolder.bind(match);

    }

    /**
     *
     * @param childViewHolder
     * @param position
     * @param childListItem
     */
    @Override
    public void onBindChildViewHolder(RatingViewHolder childViewHolder, int position, Object childListItem) {
        CustomListitem item = (CustomListitem) childListItem;
        childViewHolder.bind(item, position);
    }

    /**
     * Given the index relative to the entire RecyclerView, returns the nearest
     * ParentPosition without going past the given index.
     * <p/>
     * If it is the index of a parent item, will return the corresponding parent position.
     * If it is the index of a child item within the RV, will return the position of that childs parent.
     */
    int getNearestParentPosition(int fullPosition) {
        if (fullPosition == 0) {
            return 0;
        }

        int parentCount = -1;
        for (int i = 0; i <= fullPosition; i++) {
            Object listItem = getListItem(i);
            if (listItem instanceof ParentWrapper) {

                parentCount++;
            }
        }
        return parentCount;
    }

    /**
     * @author 1uk4s
     * @author snap10
     */
    public class CourseViewHolder extends ParentViewHolder {

        private RelativeLayout rootViewParent;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle, tvInfo;
        private ImageButton btnAction;
        private ImageView ivFromGroupIndicator;


        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public CourseViewHolder(View itemView) {
            super(itemView);

            rootViewParent = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.iv_itempicture);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);
            tvInfo = (TextView) itemView.findViewById(R.id.tv_iteminfo);
            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);
            ivFromGroupIndicator = (ImageView) itemView.findViewById(R.id.iv_from_group_indicator);
        }

        /**
         *
         * @param match
         */
        public void bind(final MatchParentListItem match) {
            ivPicture.setImageUrl(match.getCourse().getThumbnailpath(), mVolley.getImageLoader());
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            tvTitle.setText(match.getCourse().getName());
            tvSubtitle.setText(match.getCourse().getTerm());
            if (match.getChildItemList().size() == 1) {
                tvInfo.setText(match.getChildItemList().size() + " " + mContext.getString(R.string.match));
            } else {
                tvInfo.setText(match.getChildItemList().size() + " " + mContext.getString(R.string.matches));
            }
            if (match != null && match.getGroupid() != null) {
                //Show an Icon to indicate, that this match is based on the users group and my be from an rating of another group member
                ivFromGroupIndicator.setVisibility(View.VISIBLE);
            }

            if (match.getChildItemList().size() == 0) {
                btnAction.setVisibility(View.GONE);
            } else {
                btnAction.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
                btnAction.setVisibility(View.VISIBLE);

                rootViewParent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExpanded()) {
                            collapseView();
                        } else {
                            //Workaround for woring Index in Library...
                            collapseAllParents();
                            expandView();
                        }
                    }
                });
            }
        }
    }


    /**
     * @author 1uk4s
     * @author snap10
     */
    public class RatingViewHolder extends ChildViewHolder {

        private final ProgressBar progressBar;
        private MatchParentListItem parentItem;
        private ExpandableMatchListAdapter parentAdapter;
        private RelativeLayout rootViewChild;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle;
        private ImageButton btnAction;
        private CustomListitem item;
        private ImageView ivFromGroupIndicator;


        /**
         * Default constructor.
         *
         * @param itemView                   The {@link View} being hosted in this ViewHolder
         * @param expandableMatchListAdapter
         */
        public RatingViewHolder(final View itemView, ExpandableMatchListAdapter expandableMatchListAdapter) {
            super(itemView);
            parentAdapter = expandableMatchListAdapter;

            rootViewChild = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            progressBar = (ProgressBar) itemView.findViewById(R.id.listitem_progressbar);

            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.iv_itempicture);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);
            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenu();
                }
            });

        }

        private void startIntentsTo() {
            ParentWrapper wrapper = (ParentWrapper) getListItem(getParentAdapterPosition());
            MatchParentListItem match = (MatchParentListItem) wrapper.getParentListItem();
            if (item.getType() == Types.USER) {
                Intent intent = new Intent(mContext, MatchActivity.class);
                intent.putExtra("matchedid", item.getId());
                intent.putExtra("courseid", match.getCourse().get_id());
                mContext.startActivity(intent);
            } else if (item.getType() == Types.GROUP) {
                Intent intent = new Intent(mContext, GroupActivity.class);
                intent.putExtra("groupid", item.getId());
                mContext.startActivity(intent);

            }
        }

        public void bind(final CustomListitem item, final int position) {
            this.item = item;
            ParentWrapper wrapper = (ParentWrapper) getListItem(getParentAdapterPosition());
            parentItem = (MatchParentListItem) wrapper.getParentListItem();

            rootViewChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startIntentsTo();
                }
            });
            rootViewChild.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopupMenu();
                    return true;
                }
            });

            if (item.getType() == Types.GROUP) {
                //If Item is of Type Group we have to preload Courses and Users
                ((Group) item).getCourse(mVolley, mTutinder, new Group.OnCourseLoadedListener() {
                    @Override
                    public void onCourseLoaded(Course course) {
                        tvTitle.setText(R.string.group);
                        ivPicture.setImageUrl(item.getThumbnailpath(), mVolley.getImageLoader());

                    }
                });
                ((Group) item).getUsers(mContext, mVolley, mTutinder, new Group.OnUsersLoadedListener() {
                    @Override
                    public void onUsersComplete(List<User> users) {
                        tvSubtitle.setText(item.getSubtitle());
                    }
                });
            } else {
                tvTitle.setText(item.getTitle());
                ivPicture.setImageUrl(item.getThumbnailpath(), mVolley.getImageLoader());
                tvSubtitle.setText(item.getSubtitle());
            }
        }

        public void showPopupMenu() {
            ParentWrapper wrapper = (ParentWrapper) getListItem(getParentAdapterPosition());
            final MatchParentListItem match = (MatchParentListItem) wrapper.getParentListItem();

            PopupMenu popupMenu = new PopupMenu(mContext, btnAction);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_match_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_show_profile:
                            startIntentsTo();
                            return true;
                        case R.id.action_send_group_request:
                            Bundle bundleRequest = new Bundle();
                            bundleRequest.putString("userid", item.getId());
                            bundleRequest.putString("courseid", match.getCourse().get_id());
                            mListener.onListItemInteract(mContext.getString(R.string.action_send_group_request), bundleRequest, RatingViewHolder.this);
                            return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }

        /**
         * Returns the adapter position of the Parent associated with this ChildViewHolder
         *
         * @return The adapter position of the Parent if it still exists in the adapter.
         * RecyclerView.NO_POSITION if item has been removed from the adapter,
         * RecyclerView.Adapter.notifyDataSetChanged() has been called after the last
         * rootViewParent pass or the ViewHolder has already been recycled.
         */
        public int getParentAdapterPosition() {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return adapterPosition;
            }

            return parentAdapter.getNearestParentPosition(adapterPosition);
        }

        /**
         *
         */
        public void showProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setEnabled(true);
            }
        }

        /**
         *
         */
        public void hideProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
                progressBar.setEnabled(false);
            }
        }

    }


}

