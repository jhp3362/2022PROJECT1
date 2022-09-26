package com.example.zolp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zolp.databinding.FragmentItemViewBinding;

import java.util.ArrayList;
import java.util.Arrays;


public class RecommendViewAdapter extends RecyclerView.Adapter<RecommendViewAdapter.ViewHolder> {

    private final ArrayList<RestaurantInfo> mValues;
    private OnItemClickListener listener;
    private Context context;
    public RecommendViewAdapter(ArrayList<RestaurantInfo> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(FragmentItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setViews(mValues.get(position));
        holder.setOnItemClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public RestaurantInfo getItem(int position) {
        return mValues.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void addItems(RestaurantInfo info){
        mValues.add(info);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView keywords;
        public final TextView location;
        public final TextView phoneNumber;
        public final ImageView imageView;
        public final Button button;
        public int id;
        public OnItemClickListener listener;


        public ViewHolder(FragmentItemViewBinding binding) {
            super(binding.getRoot());
            name = binding.name;
            keywords = binding.keywords;
            location = binding.location;
            phoneNumber = binding.phoneNumber;
            imageView = binding.image;
            button = binding.button;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.itemClick(v, getBindingAdapterPosition());
                }
            });
        }

        @SuppressLint("ResourceAsColor")
        public void setViews(RestaurantInfo info) {
            id = info.id;
            name.setText(info.name);
            keywords.setText(Arrays.toString(info.keywords));     // Restaurant의 keywords는 String[]
            location.setText(info.location);
            phoneNumber.setText(info.phoneNumber);

//            imageView.setBackgroundColor(R.color.purple_200);
            Glide.with(context).load(info.imageUrl).into(imageView);
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

    }
}