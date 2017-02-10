package tutinder.mad.uulm.de.tutinder.views;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @author 1uk4s
 */
public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {

    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}
