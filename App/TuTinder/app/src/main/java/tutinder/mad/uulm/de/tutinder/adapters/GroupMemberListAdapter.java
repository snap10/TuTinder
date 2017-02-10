package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.MatchActivity;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.views.CircleNetworkImageView;

/**
 * Created by Lukas on 30.06.2016.
 */
public class GroupMemberListAdapter extends RecyclerView.Adapter<GroupMemberListAdapter.GroupMemberViewHolder > {

    Context context;
    VolleySingleton volleySingleton;

    List<User> memberList;
    private String courseid;

    public GroupMemberListAdapter(Context context, List<User> memberList, String courseid) {
        this.context = context;
        this.courseid = courseid;
        volleySingleton = VolleySingleton.getInstance(this.context);
        this.memberList = memberList;
    }


    @Override
    public GroupMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_inner, parent, false);
        return new GroupMemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GroupMemberViewHolder holder, int position) {
        holder.bind(memberList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class GroupMemberViewHolder extends RecyclerView.ViewHolder {

        CircleNetworkImageView ivPicture;
        TextView tvTitle, tvSubtitle;
        ImageButton btnAction;
        View vSeparator;

        public GroupMemberViewHolder(View itemView) {
            super(itemView);

            vSeparator = (View) itemView.findViewById(R.id.listitem_separator);
            ivPicture = (CircleNetworkImageView) itemView.findViewById(R.id.listitem_picture);
            tvTitle = (TextView) itemView.findViewById(R.id.listitem_title);
            tvSubtitle = (TextView) itemView.findViewById(R.id.listitem_subtitle);
            btnAction = (ImageButton) itemView.findViewById(R.id.listitem_button);
            btnAction.setImageResource(R.drawable.ic_info_black_24dp);

        }

        public void bind(final User user, int position) {
            if(position == 0) {
                vSeparator.setVisibility(View.INVISIBLE);
            }

            ivPicture.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_50dp);
            ivPicture.setImageUrl(user.getProfileThumbnailPath(), volleySingleton.getImageLoader());

            tvTitle.setText(user.getName());
            tvSubtitle.setText(user.getStudycourse());

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MatchActivity.class);
                    intent.putExtra("userid", user.get_id());
                    intent.putExtra("courseid", courseid);
                    context.startActivity(intent);
                }
            });

        }
    }
}
