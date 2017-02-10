package tutinder.mad.uulm.de.tutinder.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;
import tutinder.mad.uulm.de.tutinder.models.Course;
import tutinder.mad.uulm.de.tutinder.models.Timeslot;
import tutinder.mad.uulm.de.tutinder.singletons.VolleySingleton;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.view.View.OnClickListener;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * {@link Adapter} that can display a {@link Course} and makes a call to the
 * specified {@link AdminCourseListFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TimeslotAdapter extends Adapter<TimeslotAdapter.TimeSlotViewHolder> {

    private final SpinnerAdapter spinneradapter;
    private List<Timeslot> timeSlotList;
    private OnRecyclerInteractionListener mListener;
    private VolleySingleton volleySingleton;

    public TimeslotAdapter(SpinnerAdapter spinneradapter, List<Timeslot> timeSlotList, OnRecyclerInteractionListener listener, VolleySingleton volleySingleton) {
        this.spinneradapter = spinneradapter;
        this.timeSlotList = timeSlotList;
        this.mListener = listener;
        this.volleySingleton = volleySingleton;

    }

    @Override
    public TimeSlotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.form_course_timeslot, parent, false);
        return new TimeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, int position) {
        holder.bind(timeSlotList.get(position));
    }


    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }


    public class TimeSlotViewHolder extends ViewHolder {

        private TextView inFrom;
        private TextView inTo;
        private Spinner daySpinner;
        private ImageButton actionButton;

        private Timeslot timeSlotItem;

        public TimeSlotViewHolder(final View v) {
            super(v);
            inFrom = (TextView) v.findViewById(R.id.in_from);
            inTo = (TextView) v.findViewById(R.id.in_to);
            daySpinner = (Spinner) v.findViewById(R.id.sp_day);
            daySpinner.setAdapter(spinneradapter);
            actionButton = (ImageButton) v.findViewById(R.id.listitem_button);
            actionButton.setImageResource(android.R.drawable.ic_delete);
            actionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onListFragmentInteraction(timeSlotItem,v,"delete");
                }
            });


        }

        public void bind(Timeslot timeSlot) {
            timeSlotItem = timeSlot;
            daySpinner.setSelection(timeSlot.getDay());
            inFrom.setText(timeSlotItem.getFrom());
            inTo.setText(timeSlotItem.getTo());

        }


    }

    public interface OnRecyclerInteractionListener {
        void onListFragmentInteraction(Timeslot item, View v, String action);
    }
}
