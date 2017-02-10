package tutinder.mad.uulm.de.tutinder.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.activities.FriendlistActivity;
import tutinder.mad.uulm.de.tutinder.activities.MatchListActivity;
import tutinder.mad.uulm.de.tutinder.activities.GroupRequestListActivity;

/**
 * Fragment displayed in MainActivity, which is used for navigation.
 *
 * @author 1uk4s
 * @author snap10
 */
public class TutinderFragment extends Fragment {

    private Button btnOpenRequests, btnMatches, btnFriends;


    /**
     * Default Constructor.
     */
    public TutinderFragment() {
        // do nothing
    }

    /**
     * Returns a new instance of TutinderFragment.
     *
     * @return
     */
    public static Fragment newInstance() {
        return new TutinderFragment();
    }

    /**
     * Creates the Fragments View.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_tutinder, container, false);

        btnOpenRequests = (Button) fragmentView.findViewById(R.id.btn_mygrouprequests);
        btnOpenRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GroupRequestListActivity.class);
                startActivity(intent);
            }
        });

        btnMatches = (Button) fragmentView.findViewById(R.id.btn_myMatches);
        btnMatches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MatchListActivity.class);
                startActivity(intent);
            }
        });

        btnFriends = (Button) fragmentView.findViewById(R.id.btn_myFriends);
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendlistActivity.class);
                startActivity(intent);
            }
        });

        return fragmentView;
    }


}
