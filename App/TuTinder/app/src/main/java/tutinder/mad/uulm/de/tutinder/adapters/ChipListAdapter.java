package tutinder.mad.uulm.de.tutinder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tutinder.mad.uulm.de.tutinder.R;

/**
 * RecyclerAdapter for Chips.
 *
 * @author 1uk4s
 * @author snap10
 */
public class ChipListAdapter extends RecyclerView.Adapter<ChipListAdapter.ChipViewHolder>{

    Context mContext;

    List<String> tagList;


    /**
     * Default Constructor.
     *
     * @param context
     * @param tags
     */
    public ChipListAdapter(Context context, List<String> tags) {
        this.mContext = context;
        this.tagList = tags;
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ChipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recycler_item_chip, parent, false);
        return new ChipViewHolder(itemView);

    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ChipViewHolder holder, int position) {
        holder.bind(tagList.get(position));
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return tagList.size();
    }

    /**
     * Adds a new Chip to the Adapter.
     *
     * @param tag
     */
    public void updateItemList(String tag) {
        this.tagList.add(tag);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder of ChipListAdapter.
     */
    public class ChipViewHolder extends RecyclerView.ViewHolder {

        private TextView chip;


        /**
         * Default Constructor.
         * @param itemView
         */
        public ChipViewHolder(View itemView) {
            super(itemView);

            chip = (TextView) itemView.findViewById(R.id.tv_chip);
        }

        /**
         * Binds a Tag to this ViewHolder Instance.
         * @param tag
         */
        public void bind(String tag) {
            this.chip.setText(tag);
        }
    }
}
