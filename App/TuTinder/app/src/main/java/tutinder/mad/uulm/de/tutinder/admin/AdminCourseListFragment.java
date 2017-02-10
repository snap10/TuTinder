package tutinder.mad.uulm.de.tutinder.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AdminCourseListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Course> courseList;
    private RecyclerView recyclerView;
    private ProgressBar loadingProgressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AdminCourseListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AdminCourseListFragment newInstance(int columnCount) {
        AdminCourseListFragment fragment = new AdminCourseListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            courseList = new ArrayList<Course>();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_admin_course, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_course) {
            Intent intent = new Intent(getContext(), AdminCourseActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admincourselistfragment_list, container, false);
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.loading_progressbar);
        // Set the adapter

        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        makeCourseListRequest();

        return view;
    }


    /**
     * Shows the activities loading ProgressDialog.
     */
    private void showProgressDialog() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            loadingProgressBar.setEnabled(true);
        }

    }

    /**
     * Hides the activities loading ProgressDialog if its showing.
     */
    private void hideProgressDialog() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setEnabled(false);
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void makeCourseListRequest() {
        VolleySingleton volleySingleton = VolleySingleton.getInstance(getContext());
        Tutinder helper = Tutinder.getInstance();
        GsonRequest<Course[]> courseListReq = new GsonRequest<>(Request.Method.GET, volleySingleton.getAPIRoot() + "/courses", null, Course[].class, helper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course[]>() {
            @Override
            public void onResponse(Course[] response) {
                if (recyclerView != null) {
                    courseList = Arrays.asList(response);
                    recyclerView.setAdapter(new MyAdminCourseListFragmentRecyclerViewAdapter(courseList, mListener, VolleySingleton.getInstance(getContext())));
                }
                hideProgressDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO
                Toast.makeText(getContext(), R.string.strangeError + "ServerError", Toast.LENGTH_LONG).show();
                hideProgressDialog();
                getActivity().finish();
            }
        });
        volleySingleton.addToRequestQueue(courseListReq);
        showProgressDialog();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Course item, View v, String action);
    }
}
