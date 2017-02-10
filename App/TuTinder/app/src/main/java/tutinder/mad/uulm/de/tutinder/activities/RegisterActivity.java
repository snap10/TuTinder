package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.CustomJsonRequest;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.InputChecker;

/**
 * @author 1uk4s
 * @author snap10
 */
public class RegisterActivity extends AppCompatActivity {

    private Context mContext;
    private Tutinder mTutinder;
    private VolleySingleton mVolley;

    private CoordinatorLayout rootView;

    private TextInputEditText inName, inEmail, inMatriculationNr, inStudycourse, inPassword, inPassword2;
    private ImageButton btnShowPassword, btnHidePassword;
    private LinearLayout layoutTags;

    private List<RadioGroup> radioGroups;

    private String name, email, studycourse, password, password2;
    private int matriculationNr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        /*
            Get the Application References
        */
        mContext = getApplicationContext();
        mTutinder = Tutinder.getInstance();
        mVolley = VolleySingleton.getInstance(mContext);
        /*
            Toolbar
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*
            Get the view elements
         */
        rootView = (CoordinatorLayout) findViewById(R.id.cl_register);
        inName = (TextInputEditText) findViewById(R.id.in_name);
        inEmail = (TextInputEditText) findViewById(R.id.in_email);
        inMatriculationNr = (TextInputEditText) findViewById(R.id.in_matriculationNr);
        inStudycourse = (TextInputEditText) findViewById(R.id.in_studycourse);
        btnShowPassword = (ImageButton) findViewById(R.id.btn_showPassword);
        btnHidePassword = (ImageButton) findViewById(R.id.btn_hidePassword);
        inPassword = (TextInputEditText) findViewById(R.id.in_password);
        inPassword2 = (TextInputEditText) findViewById(R.id.in_password2);
        layoutTags = (LinearLayout) findViewById(R.id.layout_tags);
        /*
            Initialise Buttons
         */
        btnShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show password and switch buttons
                btnShowPassword.setVisibility(View.INVISIBLE);
                btnHidePassword.setVisibility(View.VISIBLE);
                inPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                inPassword2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });
        btnHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide password and switch buttons
                btnHidePassword.setVisibility(View.INVISIBLE);
                btnShowPassword.setVisibility(View.VISIBLE);
                inPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inPassword2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        /*
            Build tag list
         */
        radioGroups = new ArrayList<RadioGroup>();
        requestTags();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_register_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_register) {
            // check input
            if (checkUserInput()) {
                List<String> selectedTags = new ArrayList<>();
                for (RadioGroup rg : radioGroups) {
                    int id = rg.getCheckedRadioButtonId();
                    AppCompatRadioButton btn = (AppCompatRadioButton) findViewById(id);
                    selectedTags.add(btn.getTag().toString());
                }
                try {
                    // create user JsonObject
                    final JSONObject newuser = new JSONObject();
                    newuser.put("name", name);
                    newuser.put("email", email);
                    newuser.put("matrikelnr", matriculationNr);
                    newuser.put("studycourse", studycourse);
                    newuser.put("password", password);
                    newuser.put("personalitytags", new JSONArray(selectedTags));

                    String postMessage = newuser.toString();
                    GsonRequest req = new GsonRequest<User>(Request.Method.POST, mVolley.getAPIRoot() + "/user/register", postMessage, User.class, null, new Response.Listener<User>() {
                        @Override
                        public void onResponse(User user) {
                            if (user.getMatrikelnr() == matriculationNr) {
                                //Register correct
                                //Saves the User as the current LoggedInUser in the ApplicationSingleton as GlobalObject
                                user.setPassword(password);
                                mTutinder.setLoggedInUser(user);
                                SharedPreferences prefs = getSharedPreferences("logged_in_user", MODE_PRIVATE);
                                prefs.edit().putInt("matrikelnr", user.getMatrikelnr()).putString("password", user.getPassword()).commit();

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("editaccount", true);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();

                                //Something strange happened, can possibly not be the case...
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                                Toast.makeText(getApplicationContext(), R.string.error_registration, Toast.LENGTH_LONG).show();
                            } else if (error.networkResponse != null && error.networkResponse.statusCode == 409) {
                                Toast.makeText(getApplicationContext(), R.string.error_already_registered, Toast.LENGTH_LONG).show();
                            } else {

                                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    mVolley.addToRequestQueue(req);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{

            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Generates a RadioGroup from a given TagJsonObject and returns it.
     *
     * @return
     */
    public RadioGroup generateRadioGroup(JSONObject tag) throws JSONException {
        String value1=null, value2=null;
        String tagId, tagId1, tagId2;
        try {
            JSONArray tagsWrapper = tag.getJSONArray("tags");
            JSONArray languageTagsLeft = tagsWrapper.getJSONObject(0).getJSONArray("tag");
            JSONArray languageTagsRight = tagsWrapper.getJSONObject(1).getJSONArray("tag");
            tagId1 = tagsWrapper.getJSONObject(0).getString("_id");
            tagId2 = tagsWrapper.getJSONObject(1).getString("_id");
            for (int i = 0; i < languageTagsLeft.length(); i++) {
                JSONObject tagObject = languageTagsLeft.getJSONObject(i);
                if (tagObject.getString("lang").equals(Locale.getDefault().getLanguage().toUpperCase())) {
                    value1 = tagObject.getString("value");
                }
            }
            for (int i = 0; i < languageTagsRight.length(); i++) {
                JSONObject tagObject = languageTagsRight.getJSONObject(i);
                if (tagObject.getString("lang").equals(Locale.getDefault().getLanguage().toUpperCase())) {
                    value2 = tagObject.getString("value");
                }
            }
            tagId = tag.getString("_id");
            if (value1 == null || value2 == null)
                throw new JSONException("values were not provided correctly");

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        // generate RadioGroup
        RadioGroup radioGroup = new RadioGroup(this);
        //Set ID of Tag to RadioGroup
        radioGroup.setTag(tagId);
        RadioGroup.LayoutParams groupParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        groupParams.setMargins(0, 4, 0, 4);
        radioGroup.setLayoutParams(groupParams);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        // generate RadioButtons
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT, 0.5f);
        for (int i = 0; i < 2; i++) {
            AppCompatRadioButton radioButton = new AppCompatRadioButton(this);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(11);
            radioButton.generateViewId();

            if (i == 0) {
                radioButton.setText(value1);
                radioButton.setTag(tagId1);
            } else {
                radioButton.setText(value2);
                radioButton.setTag(tagId2);
            }
            radioGroup.addView(radioButton);
        }
        return radioGroup;
    }

    /**
     * Displays all RadioGroups.
     */
    public void displayAllRadioGroups() {
        for (RadioGroup rg : radioGroups) {
            layoutTags.addView(rg);
        }
    }

    /**
     * Returns true, if all inputs are valid.
     *
     * @return
     */
    private boolean checkUserInput() {

        InputChecker inputChecker = new InputChecker(getApplicationContext());

        // name
        name = inName.getText().toString();
        if (!inputChecker.checkName(name, inName)) {
            return false;
        }

        // email
        email = inEmail.getText().toString();
        if (!inputChecker.checkEmail(email, inEmail)) {
            return false;
        }

        // matriculation nr
        matriculationNr = -1;
        try {
            matriculationNr = Integer.parseInt(inMatriculationNr.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();

            inMatriculationNr.requestFocus();
            inMatriculationNr.setError(getString(R.string.error_invalid_matriculationnr));
            return false;
        }

        // studycourse
        studycourse = inStudycourse.getText().toString();
        if (!inputChecker.checkStudycourse(studycourse, inStudycourse)) {
            return false;
        }

        // password
        password = inPassword.getText().toString();
        password2 = inPassword2.getText().toString();
        if (!inputChecker.checkPasswords(password, password2, inPassword, inPassword2)) {
            return false;
        }

        //tags
        if (!inputChecker.checkAllTagsSelected(radioGroups)) {
            Snackbar.make(rootView, getString(R.string.error_not_all_tags_selected), Snackbar.LENGTH_LONG)
                    .show();

            return false;
        }

        return true;
    }



    /**
     * Makes an api call and loads all tags. After download it will display all tags.
     */
    private void requestTags() {
        String url = mVolley.getAPIRoot() + "/variables/tags/personality";
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET, url, null, null, new Response.Listener<String>() {
            @Override
            public void onResponse(String tags) {
                try {
                    if (tags == null) {
                        throw new Resources.NotFoundException("No Tags were found on the Server");
                    } else {
                        JSONArray jsonTags = new JSONArray(tags);
                        for (int i = 0; i < jsonTags.length(); i++) {
                            radioGroups.add(generateRadioGroup(jsonTags.getJSONObject(i)));
                        }
                        displayAllRadioGroups();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(rootView, getString(R.string.error_loading_tags), Snackbar.LENGTH_INDEFINITE)
                        .setAction(getString(R.string.btn_retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestTags();
                            }
                        })
                        .show();
            }
        });
        mVolley.addToRequestQueue(request);
    }

}
