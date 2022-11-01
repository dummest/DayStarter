package com.example.daystarter.ui.news;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.daystarter.R;

import java.util.ArrayList;

public class NewAdapter extends RecyclerView.Adapter {
    private ArrayList<NewData> data;
    Context context;
    View itemView;
    String TAG = "new";
    public NewAdapter(ArrayList<NewData> items, Context context) {
        this.data = items;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_news,parent,false);
        VH vh= new VH(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VH vh= (VH)holder;
        //현재번째(position) 아이템 얻어오기
        NewData item= data.get(position);

        vh.tvTitle.setText(item.getTitle());
        vh.tvDesc.setText(item.getDesc());
        vh.tvDate.setText(item.getDate());
        if(item.getImgUrl() ==null){
            vh.iv.setVisibility(View.GONE);
        }
        else
            vh.iv.setVisibility(View.VISIBLE);
            Glide.with(vh.itemView.getContext()).load(item.getImgUrl()).into(vh.iv);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class VH extends RecyclerView.ViewHolder{

        TextView tvTitle, tvDesc, tvDate;
        ImageView iv;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle=itemView.findViewById(R.id.tv_title);
            tvDesc=itemView.findViewById(R.id.tv_desc);
            tvDate=itemView.findViewById(R.id.tv_date);
            iv=itemView.findViewById(R.id.iv);

            //아이템 클릭시 선택한 뉴스로 이동(
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String link= data.get(getLayoutPosition()).getLink();
                    //웹튜를 가진 새로운 액티비티
                    Intent intent= new Intent(context,ItemActivty.class);
                    intent.putExtra("Link",link);
                    context.startActivity(intent);
                }
            });
        }
    }
}

