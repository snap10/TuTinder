package tutinder.mad.uulm.de.tutinder.activities;

import android.app.SearchManager;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.adapters.ExpandableCourseListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.SearchListAdapter;
import tutinder.mad.uulm.de.tutinder.adapters.customItems.FacultyListItem;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Faculty;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

/**
 * In this activity, all available courses are provided. The user also has the possibility to
 * search for courses, by typing keywords into a search input field.
 * @author 1uk4s
 * @author snap10
 */
public class CourseListActivity extends AppCompatActivity {

    private final int REQUEST_COURSES = 0;
    private final int REQUEST_SEARCH = 1;

    private Context mContext;
    private Tutinder mHelper;
    private VolleySingleton mVolley;

    List<FacultyListItem> facultyItemList;
    List<Course> searchItemList;

    private Toolbar toolbar;
    private CoordinatorLayout rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewCourses;
    private RecyclerView recyclerViewSearch;
    private SearchListAdapter searchListAdapter;
    private SearchView searchView;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        /*
            Get the Application References
        */
        mContext = getApplicationContext();
        mHelper = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContext);
        facultyItemList = new ArrayList<FacultyListItem>();
        searchItemList = new ArrayList<Course>();
        /*
            Toolbar
         */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Views
         */
        rootView = (CoordinatorLayout) findViewById(R.id.cl_courselist);

        loadingProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout_courses);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestCourses();
            }
        });

        recyclerViewCourses = (RecyclerView) findViewById(R.id.recycler_courses);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerViewCourses.setAdapter(new ExpandableCourseListAdapter(this, facultyItemList));

        recyclerViewSearch = (RecyclerView) findViewById(R.id.recycler_search);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(mContext));
        searchListAdapter = new SearchListAdapter(this, searchItemList);
        recyclerViewSearch.setAdapter(searchListAdapter);
        /*
            API-Call
         */
        requestCourses();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_course_list_toolbar, menu);
        // initialise search
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) CourseListActivity.this.getSystemService(Context.SEARCH_SERVICE);
        searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(CourseListActivity.this.getComponentName()));
            searchView.setQueryHint(getString(R.string.hint_search));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    if (query.length() >= 1) {
                        requestSearch(query);
                        return true;
                    }else if(query.length()==0){
                        //Make Adapter empty if user deletes query
                        if (searchItemList != null) {
                            searchItemList.clear();
                            searchListAdapter.notifyDataSetChanged();}
                    }
                    return false;
                }
            });
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    searchView.onActionViewCollapsed();
                    recyclerViewSearch.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                    return true;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Hide swipeRefreshLayout to have Progressbar shown immediately
                    //moved here from ReqREsponse
                    swipeRefreshLayout.setVisibility(View.GONE);
                    recyclerViewSearch.setVisibility(View.VISIBLE);
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // first close searchView, if it's visible
        if (searchView != null && !searchView.isIconified()) {
            searchView.onActionViewCollapsed();
            recyclerViewSearch.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        // if serachView isn't visible, perform onBackPressed()
        else {
            super.onBackPressed();
        }
    }

    /**
     * This callback will be called after the API-call responded with 200 OK.
     * @param requestType
     */
    public void onRequestResponse(int requestType) {
        switch (requestType) {
            case REQUEST_COURSES: // handle recyclerViewCourses content
                if (facultyItemList != null && !facultyItemList.isEmpty()) {
                    recyclerViewCourses.setAdapter(new ExpandableCourseListAdapter(this, facultyItemList));
                } else {
                    Snackbar.make(rootView, getString(R.string.error_loading_course_list), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestCourses();
                                }
                            })
                            .show();
                }
                break;
            case REQUEST_SEARCH: // handle recyclerViewSerach content
                searchListAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * Makes an API call to load all courses.
     *
     * @param keyword
     */
    private void requestSearch(final String keyword) {
        final String URL = mVolley.getAPIRoot() + "/courses/find?q=" + keyword;
        GsonRequest<Course[]> request = new GsonRequest<Course[]>(Request.Method.GET, URL, null, Course[].class, mHelper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Course[]>() {
            @Override
            public void onResponse(Course[] response) {
                // update list
                searchItemList.clear();
                for (Course course : response) {
                    searchItemList.add(course);
                }
                // stop refreshing
                hideProgressBar();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                // callback
                onRequestResponse(REQUEST_SEARCH);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // stop refreshing
                hideProgressBar();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                // show error message
                Snackbar.make(rootView, getString(R.string.error_sending_search_request), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestSearch(keyword);
                            }
                        })
                        .show();
            }
        });
        //Fix for overlapping requests...
        mVolley.cancelRequests();
        mVolley.addToRequestQueue(request);
        showProgressBar();
    }

    /**
     * Makes an API call to load all courses.
     */
    private void requestCourses() {
        final String URL = mVolley.getAPIRoot() + "/faculties/";
        GsonRequest<Faculty[]> request = new GsonRequest<Faculty[]>(Request.Method.GET, URL, null, Faculty[].class, mHelper.getLoggedInUser().getLoginCredentials(), new Response.Listener<Faculty[]>() {
            @Override
            public void onResponse(Faculty[] facultylist) {
                // update list
                facultyItemList.clear();
                for (Faculty faculty : facultylist) {
                    facultyItemList.add(new FacultyListItem(faculty, null));
                }
                // stop refreshing
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                // callback
                onRequestResponse(REQUEST_COURSES);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // stop refreshing
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                // show error message
                Snackbar.make(rootView, getString(R.string.error_loading_course_list), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestCourses();
                            }
                        })
                        .show();
            }
        });
        mVolley.addToRequestQueue(request);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
    }

    /**
     * Shows the activities loadingProgressBar.
     */
    private void showProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setEnabled(true);
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Hides the activities loadingProgressBar if it's showing.
     */
    private void hideProgressBar() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setEnabled(false);
            loadingProgressBar.setVisibility(View.GONE);
        }
    }
}
