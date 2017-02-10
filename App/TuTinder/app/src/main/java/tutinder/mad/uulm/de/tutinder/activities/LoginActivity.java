package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.dd.processbutton.iml.ActionProcessButton;

import java.util.HashMap;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.handlers.volley_custom.GsonRequest;
import tutinder.mad.uulm.de.tutinder.models.User;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.InputChecker;

/**
 * Created by 1uk4s on 27.04.16.
 */
public class LoginActivity extends AppCompatActivity {

    private ActionProcessButton btnLogin;
    private Button btnRegister;
    private ImageButton btnShowPassword, btnHidePassword;
    private TextInputEditText inMatriculationNr, inPassword;
    private Tutinder helper;
    private VolleySingleton volley;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*
            Get the Application Reference
        */
        helper = Tutinder.getInstance();
        volley = VolleySingleton.getInstance(getApplicationContext());

            /*
            get view elements
         */
        btnLogin = (ActionProcessButton) findViewById(R.id.btn_login);
        btnRegister = (Button) findViewById(R.id.btn_register);
        btnShowPassword = (ImageButton) findViewById(R.id.btn_showPassword);
        btnHidePassword = (ImageButton) findViewById(R.id.btn_hidePassword);
        inMatriculationNr = (TextInputEditText) findViewById(R.id.in_matriculationNr);
        inPassword = (TextInputEditText) findViewById(R.id.in_password);
        //have passwords set automatically when strange error in SplashActivity occured
        try {
            String password = getSharedPreferences("logged_in_user", MODE_PRIVATE).getString("password", null);
            inPassword.setText(password);
            int matrikelnr = getSharedPreferences("logged_in_user", MODE_PRIVATE).getInt("matrikelnr", 0);
            if (matrikelnr != 0) inMatriculationNr.setText(matrikelnr + "");
        } catch (Exception e) {
            Log.w("MAINACTIVITY", e.getMessage());
        }
        /*
            initialise view elements
         */
        btnLogin.setMode(ActionProcessButton.Mode.ENDLESS);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get login credentials
                int matriculationNr = getMatriculationNr();
                final String password = getPassword();
                // check login credentials
                InputChecker inputChecker = new InputChecker(getApplicationContext());
                if (inputChecker.checkLoginCredentials(matriculationNr, password)) {
                    // disable lgoin button
                    disableBtnLogin();
                    // send request
                    final int MATRIKELNR = matriculationNr;
                    HashMap<String, String> credentials = new HashMap<String, String>();
                    credentials.put("matrikelnr", "" + matriculationNr);
                    credentials.put("password", password);
                    GsonRequest req = new GsonRequest<User>(Request.Method.GET, volley.getAPIRoot() + "/user/login", null, User.class, credentials, new Response.Listener<User>() {
                        @Override
                        public void onResponse(User user) {
                            // enable login button
                            enableBtnLogin(100);
                            // check matriculation nr
                            if (user.getMatrikelnr() == MATRIKELNR) {
                                //Login correct
                                //saves the logged in User in the ApplicationSingleton as GlobalUser Object
                                user.setPassword(password);
                                helper.setLoggedInUser(user);
                                SharedPreferences prefs = getSharedPreferences("logged_in_user", MODE_PRIVATE);
                                prefs.edit().putInt("matrikelnr", user.getMatrikelnr()).putString("password", user.getPassword()).commit();
                                // start main activity (clear activity stack)
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                //Something strange happened, can possibly not be the case...
                                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // enable login button
                            enableBtnLogin(0);
                            // show error message
                            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                Toast.makeText(getApplicationContext(), R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.strangeError, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    volley.addToRequestQueue(req);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnShowPassword.setVisibility(View.INVISIBLE);
                btnHidePassword.setVisibility(View.VISIBLE);
                inPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });

        btnHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnHidePassword.setVisibility(View.INVISIBLE);
                btnShowPassword.setVisibility(View.VISIBLE);
                inPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    /**
     * Returns -1, if no number was input. Else returns the matriculation nr.
     *
     * @return
     */
    private int getMatriculationNr() {
        int matriculationNr = -1;
        String temp = inMatriculationNr.getText().toString();
        try {
            matriculationNr = Integer.parseInt(temp);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // invalid matriculation-number
            Toast.makeText(getApplicationContext(), R.string.error_invalid_matriculationnr, Toast.LENGTH_LONG).show();
        }
        return matriculationNr;
    }

    /**
     * Returns the password.
     *
     * @return
     */
    private String getPassword() {
        return inPassword.getText().toString();
    }

    /**
     * Starts animation and disables the login button.
     */
    private void disableBtnLogin() {
        btnLogin.setProgress(1);
        btnLogin.setEnabled(false);
        btnLogin.setClickable(false);
    }

    /**
     * Stops animation and enables the login button.
     */
    private void enableBtnLogin(int progress) {
        btnLogin.setProgress(progress);
        btnLogin.setEnabled(true);
        btnLogin.setClickable(true);
    }
}
