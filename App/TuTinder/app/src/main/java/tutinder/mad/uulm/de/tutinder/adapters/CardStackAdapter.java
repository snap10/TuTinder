package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.GroupActivity;
import tutinder.mad.uulm.de.tutinder.activities.MatchActivity;
import tutinder.mad.uulm.de.tutinder.models.CardStackGroup;
import tutinder.mad.uulm.de.tutinder.models.CardStackObject;
import tutinder.mad.uulm.de.tutinder.models.CardStackUser;
import tutinder.mad.uulm.de.tutinder.models.Group;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

/**
 * Created by Lukas on 10.05.2016.
 */
public class CardStackAdapter extends ArrayAdapter<CardStackObject> {

    private Context context;
    private Tutinder helper;
    private VolleySingleton volleySingleton;
    private String courseid;

    public CardStackAdapter(Context context, int resource, String courseid) {
        super(context, resource);
        this.context = context;
        this.courseid = courseid;
        this.helper = Tutinder.getInstance();
        this.volleySingleton = VolleySingleton.getInstance(this.context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        final CardStackObject item = getItem(position);

        if (item instanceof CardStackUser) {
            CardStackUser wrapper = (CardStackUser) item;
            User user = wrapper.getUser();

            view = buildUserView(convertView, user);
            Log.i("INSTANCE", "USER");
        }
        if (item instanceof CardStackGroup) {
            CardStackGroup wrapper = (CardStackGroup) item;
            Group group = wrapper.getGroup();

            view = buildGroupView(convertView, group);
            Log.i("INSTANCE", "GROUP");
        }

        return view;
    }

    public View buildUserView(View view, final User user) {
        CardView layout = (CardView) view.findViewById(R.id.card_view);
        CardView.LayoutParams params = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        final NetworkImageView ivProfilepicture = (NetworkImageView) view.findViewById(R.id.iv_profilepicture);
        ivProfilepicture.setDefaultImageResId(R.drawable.ic_placeholder_profilepicture_grey_500dp);
        ivProfilepicture.setErrorImageResId(R.drawable.ic_placeholder_profilepicture_grey_500dp);
        ivProfilepicture.setImageUrl(user.getProfilepicturepath(), volleySingleton.getImageLoader());

        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        tvName.setText(user.getName());
        tvName.setVisibility(View.VISIBLE);

        TextView tvStudycourse = (TextView) view.findViewById(R.id.tv_studycourse);
        tvStudycourse.setText(user.getStudycourse());
        tvStudycourse.setVisibility(View.VISIBLE);

        ImageView ivCoursecount = (ImageView) view.findViewById(R.id.iv_counter);
        ivCoursecount.setImageResource(R.drawable.ic_school_white_24dp);
        ivCoursecount.setVisibility(View.VISIBLE);
        TextView tvCoursecount = (TextView) view.findViewById(R.id.tv_counter);
        tvCoursecount.setText("" + user.getCourses().size());
        tvCoursecount.setVisibility(View.VISIBLE);

        Button btnInfo = (Button) view.findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MatchActivity.class);
                intent.putExtra("userid", user.get_id());
                intent.putExtra("courseid", courseid);
                getContext().startActivity(intent);
            }
        });
        btnInfo.setVisibility(View.VISIBLE);

        return view;
    }

    public View buildGroupView(View view, final Group group) {
        CardView layout = (CardView) view.findViewById(R.id.card_view);
        CardView.LayoutParams params = new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        final NetworkImageView ivProfilepicture = (NetworkImageView) view.findViewById(R.id.iv_profilepicture);
        ivProfilepicture.setPadding(100, 100, 100, 100);
        ivProfilepicture.setDefaultImageResId(R.drawable.ic_group_white_150dp);
        ivProfilepicture.setErrorImageResId(R.drawable.ic_group_white_150dp);
        ivProfilepicture.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_group_white_150dp));

        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        tvName.setVisibility(View.GONE);

        TextView tvStudycourse = (TextView) view.findViewById(R.id.tv_studycourse);
        tvStudycourse.setVisibility(View.GONE);

        ImageView ivUserCount = (ImageView) view.findViewById(R.id.iv_counter);
        ivUserCount.setImageResource(R.drawable.ic_person_white_24dp);
        ivUserCount.setVisibility(View.VISIBLE);
        TextView tvUserCount = (TextView) view.findViewById(R.id.tv_counter);
        tvUserCount.setText("" + group.getUsers().length);
        tvUserCount.setVisibility(View.VISIBLE);

        final RecyclerView recyclerViewThumbnails = (RecyclerView) view.findViewById(R.id.recycler_thumbnails);

        recyclerViewThumbnails.setVisibility(View.VISIBLE);
        recyclerViewThumbnails.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        group.getUsers(getContext(), volleySingleton, helper, new Group.OnUsersLoadedListener() {
            @Override
            public void onUsersComplete(List<User> users) {
                List<String> thumbnailList = new ArrayList<String>();
                for (User user : users) {
                    String thumbnailPath = user.getProfileThumbnailPath();
                    if (thumbnailPath == null) {
                        thumbnailPath = "default";
                    }
                    thumbnailList.add(thumbnailPath);
                }
                recyclerViewThumbnails.setAdapter(new ThumbnailListAdapter(context, thumbnailList));

            }
        });

        Button btnInfo = (Button) view.findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupActivity.class);
                intent.putExtra("groupid", group.get_id());
                context.startActivity(intent);
            }
        });

        return view;
    }
}
