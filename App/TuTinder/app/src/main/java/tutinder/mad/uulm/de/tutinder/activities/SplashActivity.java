package tutinder.mad.uulm.de.tutinder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;
import tutinder.mad.uulm.de.tutinder.utils.LoginChecker;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Tutinder helper;
    private VolleySingleton volley;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LoginChecker.logInCurrentUser(this, new LoginChecker.LoginCheckerListener() {
            @Override
            public void onLoginComplete(int status) {
                if (status == LoginChecker.SUCCESS) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                } else if (status == LoginChecker.INVALID_CREDENTIALS) {
                    Toast.makeText(getApplicationContext(), R.string.error_invalid_credentials, Toast.LENGTH_LONG).show();
                } else if (status == LoginChecker.STRANGE_ERROR) {
                    Toast.makeText(getApplicationContext(), R.string.connectionError, Toast.LENGTH_LONG).show();
                } else if (status == LoginChecker.NO_CREDENTIALS_PROVIDED) {
                    Toast.makeText(getApplicationContext(), R.string.welcome, Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }

}
