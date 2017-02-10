package tutinder.mad.uulm.de.tutinder.Interfaces;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

/**
 * @author 1uk4s
 */
public interface OnListItemInteractListener {

    public void onListItemInteract(String method, Bundle args);

    public void onListItemInteract(String method, Bundle args, RecyclerView.ViewHolder viewHolder);
}
