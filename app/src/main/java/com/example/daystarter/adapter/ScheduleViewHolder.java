package com.example.daystarter.adapter;

import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.daystarter.R;

public class ScheduleViewHolder extends RecyclerView.ViewHolder{
    TextView mainText;
    TextView subText;

    public ScheduleViewHolder(View itemView, ScheduleRecyclerViewAdapter.OnItemClickListener itemClickListener, ScheduleRecyclerViewAdapter.OnItemLongClickListener itemLongClickListener) {
        super(itemView); // 뷰 객체에 대한 참조
        mainText = itemView.findViewById(R.id.main_text_view);
        subText = itemView.findViewById(R.id.sub_text_view);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(itemView, position);
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(itemView, position);
                }
                return true;
            }
        });
    }
}