package tutinder.mad.uulm.de.tutinder.utils;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.AppCompatRadioButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.singletons.Tutinder;

/**
 * Created by Lukas on 02.05.2016.
 */
public class InputChecker {

    private Context mContext;
    private Tutinder mTutinder;

    /**
     * Constructor
     */
    public InputChecker(Context context) {
        this.mContext = context;
        this.mTutinder = Tutinder.getInstance();
    }

    /**
     * Returns true, if the name is valid.
     *
     * @param name
     * @return
     */
    public boolean checkName(String name) {
        if (name.length() < 3) {
            Toast.makeText(mContext, R.string.error_namelength, Toast.LENGTH_LONG).show();
            return false;
        }
        if (containsNumber(name)) {
            Toast.makeText(mContext, R.string.error_namenumber, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkName(String name, TextInputEditText textInputEditText) {
        if (name.length() < 3) {
            textInputEditText.setError(mContext.getString(R.string.error_namelength));
            textInputEditText.requestFocus();
            return false;
        }
        if (containsNumber(name)) {
            textInputEditText.setError(mContext.getString(R.string.error_namenumber));
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Returns true, if the email is valid.
     *
     * @param email
     * @return
     */
    public boolean checkEmail(String email) {
        if (!email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}")) {
            Toast.makeText(mContext, R.string.error_email, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkEmail(String email, TextInputEditText textInputEditText) {
        if (!email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}")) {
            textInputEditText.setError(mContext.getString(R.string.error_email));
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Returns true, if the studycourse is valid.
     *
     * @param studycourse
     * @return
     */
    public boolean checkStudycourse(String studycourse) {
        if (containsNumber(studycourse)) {
            Toast.makeText(mContext, R.string.error_studycoursenumber, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkStudycourse(String studycourse, TextInputEditText textInputEditText) {
        if (containsNumber(studycourse)) {
            textInputEditText.setError(mContext.getString(R.string.error_studycoursenumber));
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Returns true, if the passwords are valid.
     *
     * @param pw
     * @param pw2
     * @return
     */
    public boolean checkPasswords(String pw, String pw2) {
        if (!checkPassword(pw)) {
            return false;
        }
        if (!pw.equals(pw2)) {
            Toast.makeText(mContext, R.string.error_passwordmatch, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkPasswords(String pw, String pw2, TextInputEditText textInputEditText, TextInputEditText textInputEditText2) {
        if (!checkPassword(pw, textInputEditText)) {
            return false;
        }
        if (!pw.equals(pw2)) {
            textInputEditText.setError(mContext.getString(R.string.error_passwordmatch));
            textInputEditText.requestFocus();
            textInputEditText2.setError(mContext.getString(R.string.error_passwordmatch));
            return false;
        }
        return true;
    }

    /**
     * Returns true, if the password is valid.
     *
     * @param pw
     * @return
     */
    public boolean checkPassword(String pw) {
        if (pw.length() < 8) {
            Toast.makeText(mContext, R.string.error_passwordlength, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkPassword(String pw, TextInputEditText textInputEditText) {
        if (pw.length() < 8) {
            textInputEditText.setError(mContext.getString(R.string.error_passwordlength));
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Returns true, if the old password is valid.
     *
     * @param pw
     * @return
     */
    public boolean checkOldPassword(String pw) {
        if (!mTutinder.getLoggedInUser().getPassword().equals(pw)) {
            Toast.makeText(mContext, R.string.error_passwordoldinvalid, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkOldPassword(String pw, TextInputEditText textInputEditText) {
        if (!mTutinder.getLoggedInUser().getPassword().equals(pw)) {
            textInputEditText.setError(mContext.getString(R.string.error_passwordoldinvalid));
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Returns true, if the login credentials are valid.
     *
     * @param matriculationNr
     * @param pw
     * @return
     */
    public boolean checkLoginCredentials(int matriculationNr, String pw) {
        if (matriculationNr < 0) {
            return false;
        }
        if (!checkPassword(pw)) {
            return false;
        }
        return true;
    }

    /**
     * Returns true, if all RadioGroups are selected.
     *
     * @param radioGroup
     * @return
     */
    public boolean checkAllTagsSelected(List<RadioGroup> radioGroup) {
        boolean areAllSelected = true;

        for (RadioGroup g : radioGroup) {
            if (g.getCheckedRadioButtonId() == -1) {
                for (int i = 0; i < g.getChildCount(); i++) {
                    AppCompatRadioButton button = (AppCompatRadioButton) g.getChildAt(i);
                    button.setTextColor(mContext.getResources().getColor(R.color.red));
                }
                areAllSelected = false;
            } else {
                for (int i = 0; i < g.getChildCount(); i++) {
                    AppCompatRadioButton button = (AppCompatRadioButton) g.getChildAt(i);
                    button.setTextColor(mContext.getResources().getColor(R.color.textPrimary));
                }
            }
        }
        return areAllSelected;
    }

    /**
     * Returns true, if a String contains at leas one number from 0-9.
     *
     * @param str
     * @return
     */
    private boolean containsNumber(String str) {
        if (str.matches(".*\\d-*")) {
            return true;
        }
        return false;
    }
}
