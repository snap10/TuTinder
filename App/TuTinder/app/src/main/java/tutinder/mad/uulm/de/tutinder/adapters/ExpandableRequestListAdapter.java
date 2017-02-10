package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.Interfaces.OnListItemInteractListener;
import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.GroupRequestActivity;
import tutinder.mad.uulm.de.tutinder.adapters.customItems.RequestParentListItem;
import tutinder.mad.uulm.de.tutinder.models.GroupRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * @author 1uk4s
 * @author snap10
 */
public class ExpandableRequestListAdapter extends ExpandableRecyclerAdapter<ExpandableRequestListAdapter.CourseViewHolder, ExpandableRequestListAdapter.RequestViewHolder> {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private LayoutInflater mLayoutInflater;

    private OnListItemInteractListener mListener;

    /**
     * Primary constructor. Sets up {@link #mParentItemList} and {@link #mItemList}.
     * <p/>
     * Changes to {@link #mParentItemList} should be made through add/remove methods in
     * {@link ExpandableRecyclerAdapter}
     *
     * @param context
     * @param parentItemList List of all {@link ParentListItem} objects to be
     *                       displayed in the RecyclerView that this
     */
    public ExpandableRequestListAdapter(Context context, @NonNull List<? extends ParentListItem> parentItemList, OnListItemInteractListener listener) {
        super(parentItemList);

        this.mContext = context;
        this.mTutinder = Tutinder.getInstance();
        this.mVolley = VolleySingleton.getInstance(this.mContext);
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mListener = listener;
    }

    /**
     * @param parentViewGroup
     * @return
     */
    @Override
    public CourseViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View parentView = mLayoutInflater.inflate(R.layout.recycler_item_parent, parentViewGroup, false);
        return new CourseViewHolder(parentView);
    }

    /**
     * @param childViewGroup
     * @return
     */
    @Override
    public RequestViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View childView = mLayoutInflater.inflate(R.layout.recycler_item_child, childViewGroup, false);
        return new RequestViewHolder(childView, this);
    }

    /**
     * @param parentViewHolder
     * @param position
     * @param parentListItem
     */
    @Override
    public void onBindParentViewHolder(CourseViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        RequestParentListItem item = (RequestParentListItem) parentListItem;
        parentViewHolder.bind(item);

    }

    /**
     * @param childViewHolder
     * @param position
     * @param childListItem
     */
    @Override
    public void onBindChildViewHolder(RequestViewHolder childViewHolder, int position, Object childListItem) {
        GroupRequest item = (GroupRequest) childListItem;
        childViewHolder.bind(item, position);
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
        }

        /**
         * @param item
         */
        public void bind(RequestParentListItem item) {
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_coursepicture_accent_50dp);
            ivPicture.setImageUrl(item.getCourse().getThumbnailpath(), mVolley.getImageLoader());


            tvTitle.setText(item.getCourse().getTitle());
            tvSubtitle.setText(item.getCourse().getTitle());

            if (item.getChildItemList().size() == 1) {
                tvInfo.setText(item.getChildItemList().size() + " " + mContext.getString(R.string.request));
            } else {
                tvInfo.setText(item.getChildItemList().size() + " " + mContext.getString(R.string.requests));
            }

            if (item.getChildItemList().size() == 0) {
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
    public class RequestViewHolder extends ChildViewHolder {

        private ExpandableRequestListAdapter parentAdapter;

        private RelativeLayout rootViewChild;
        private CircleNetworkImageView ivPicture;
        private TextView tvTitle, tvSubtitle;
        private ImageButton btnAction;
        private ProgressBar progressBar;

        private GroupRequest item;


        /**
         * Default constructor.
         *
         * @param itemView The {@link View} being hosted in this ViewHolder
         */
        public RequestViewHolder(View itemView, ExpandableRequestListAdapter expandableRequestListAdapter) {
            super(itemView);
            parentAdapter = expandableRequestListAdapter;
            progressBar = (ProgressBar) itemView.findViewById(R.id.listitem_progressbar);
            rootViewChild = (RelativeLayout) itemView.findViewById(R.id.listitem_layout);
            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.iv_itempicture);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);
            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);
        }

        public void bind(final GroupRequest item, final int position) {
            this.item = item;

            rootViewChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, GroupRequestActivity.class);
                    intent.putExtra("grouprequestid", item.getId());
                    if (item.isAccepted(mTutinder.getLoggedInUser().get_id())) {
                        intent.putExtra("ismember", true);
                    }
                    mContext.startActivity(intent);
                }
            });
            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setImageUrl(item.getThumbnailpath(), mVolley.getImageLoader());
            try {
                if (item.isInitiator(mTutinder.getLoggedInUser().get_id())) {
                    tvTitle.setText(mContext.getString(R.string.my_group_request));
                    ivPicture.setImageUrl(mTutinder.getLoggedInUser().getThumbnailpath(), mVolley.getImageLoader());
                } else {
                    User requestor = item.getRequestor(mTutinder.getLoggedInUser().get_id());
                    tvTitle.setText(mContext.getString(R.string.grouprequest_title) + " " + mContext.getString(R.string.from) + " " + requestor.getName());
                    ivPicture.setImageUrl(requestor.getThumbnailpath(), mVolley.getImageLoader());
                }
            } catch (Resources.NotFoundException e) {
                tvTitle.setText(R.string.grouprequest_title);
            }
            try {
                if (item.isAccepted(mTutinder.getLoggedInUser().get_id())) {
                    btnAction.setImageResource(R.drawable.ic_check_circle_black_24dp);
                    btnAction.setEnabled(false);
                    //do nothing with the button...
                } else {
                    btnAction.setEnabled(true);
                    btnAction.setImageResource(R.drawable.ic_thumbs_up_down_black_24dp);
                    btnAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showPopupMenu();
                        }
                    });
                }

            } catch (Resources.NotFoundException e) {

            }
            tvSubtitle.setText(item.getSubtitle());
        }

        /**
         *
         */
        public void showPopupMenu() {
            PopupMenu popupMenu = new PopupMenu(mContext, btnAction);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup_group_request_list, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.action_accept:
                            Bundle bundleAccept = new Bundle();
                            bundleAccept.putString("grouprequestid", item.get_id());
                            mListener.onListItemInteract(mContext.getString(R.string.action_accept), bundleAccept, RequestViewHolder.this);
                            return true;
                        case R.id.action_deny:
                            Bundle bundleDeny = new Bundle();
                            bundleDeny.putString("grouprequestid", item.get_id());
                            mListener.onListItemInteract(mContext.getString(R.string.action_deny), bundleDeny, RequestViewHolder.this);
                            return true;
                    }
                    return false;
                }
            });
            popupMenu.show();
        }

        /**
         * Shows ProgressBar.
         */
        public void showProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setEnabled(true);
            }
        }

        /**
         * Hides ProgressBar
         */
        public void hideProgressBar() {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
                progressBar.setEnabled(false);
            }
        }

        /**
         *
         */
        public void removeItem() {
            mItemList.remove(item);
            notifyDataSetChanged();
        }
    }

}
