package com.rma.voicerecorder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rma.voicerecorder.R;
import com.rma.voicerecorder.models.VoiceRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AudioListAdapter extends SelectableAdapter<AudioListAdapter.AudioViewHolder> {
    private ArrayList<VoiceRecord> voiceRecords;
    private ItemClickListener itemClickListener;
    private Context context;

    public AudioListAdapter(Context context, ArrayList<VoiceRecord> voiceRecords, ItemClickListener itemClickListener) {
        this.voiceRecords = voiceRecords;
        this.itemClickListener = itemClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_layout, parent, false);
        return new AudioViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        final VoiceRecord voiceRecord = voiceRecords.get(position);
        holder.listTitle.setText(voiceRecord.getFileName());
        holder.listDate.setText(voiceRecord.getTimeAgo(Locale.getDefault().getLanguage()));
        if (voiceRecord.isPlaying()) {
            holder.isPlayingImage.setVisibility(View.VISIBLE);
        } else {
            holder.isPlayingImage.setVisibility(View.INVISIBLE);
        }
        switch (voiceRecord.getStatus()) {
            case "seen":
                holder.listStatusImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_done_all));
                break;
            case "uploaded":
                holder.listStatusImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_cloud_done));
                break;
            default:
                holder.listStatusImage.setImageDrawable(null);
        }

        holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position, @NonNull List<Object> payloads) {
        final VoiceRecord voiceRecord = voiceRecords.get(position);
        if (!payloads.isEmpty()) {
            if (voiceRecord.isPlaying()) {
                holder.isPlayingImage.setVisibility(View.VISIBLE);
            } else {
                holder.isPlayingImage.setVisibility(View.INVISIBLE);
            }
            switch (voiceRecord.getStatus()) {
                case "seen":
                    holder.listStatusImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_done_all));
                    break;
                case "uploaded":
                    holder.listStatusImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_cloud_done));
                    break;
                default:
                    holder.listStatusImage.setImageDrawable(null);
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return voiceRecords.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView listTitle;
        private TextView listDate;
        private ImageView isPlayingImage;
        private ImageView listStatusImage;
        private View selectedOverlay;

        private ItemClickListener listener;

        public AudioViewHolder(@NonNull View itemView, ItemClickListener listener) {
            super(itemView);

            isPlayingImage = itemView.findViewById(R.id.image_list_playing);
            listStatusImage = itemView.findViewById(R.id.image_list_status);
            listTitle = itemView.findViewById(R.id.text_list_title);
            listDate = itemView.findViewById(R.id.text_list_date);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);

            this.listener = listener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onItemClicked(voiceRecords.get(getAdapterPosition()), getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null)
                return listener.onItemLongClicked(getAdapterPosition());
            return false;
        }
    }

    public void removeItem(int position) {
        voiceRecords.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItems(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });
        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }
                if (count == 1) {
                    removeItem(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }
                for (int i = 0; i < count; ++i) {
                    positions.remove(0);
                }
            }
        }
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            voiceRecords.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public interface ItemClickListener {
        void onItemClicked(VoiceRecord voiceRecord, int position);

        boolean onItemLongClicked(int position);
    }
}
