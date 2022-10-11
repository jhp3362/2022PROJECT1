package com.example.zolp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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

    public void addItem(RestaurantInfo info){
        mValues.add(info);
    }

    public void deleteItem(int position){
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView keywords;
        public final TextView location;
        public final TextView phoneNumber;
        public final ImageView imageView;
        public final Button button, favorites, rejection;
        public OnItemClickListener listener;
        public Boolean isFavorites;


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
            favorites = binding.favorites;
            favorites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.setFavorites((Button) v, getBindingAdapterPosition());
                }
            });
            rejection = binding.rejection;
            rejection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.rejectItem(getBindingAdapterPosition());
                }
            });
        }

        @SuppressLint("ResourceAsColor")
        public void setViews(RestaurantInfo info) {
            name.setText(info.name);
            keywords.setText(Arrays.toString(info.keywords));     // Restaurant의 keywords는 String[]
            location.setText(info.location);
            phoneNumber.setText(info.phoneNumber);
            isFavorites = info.isFavorites;

            if(info.imageUrl != null) {
                Glide.with(context).load(info.imageUrl).into(imageView);
            }
            else{
                Glide.with(context).load(R.drawable.nopictures).into(imageView);
            }

            if(isFavorites){
                favorites.setBackgroundResource(R.drawable.bookmark_after);
            }
            else{
                favorites.setBackgroundResource(R.drawable.bookmark_before);
            }
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

    }
}