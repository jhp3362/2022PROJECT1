package com.example.zolp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.zolp.databinding.FragmentFavorRejectViewBinding;

import java.util.ArrayList;

public class RejectionsAdapter extends RecyclerView.Adapter<RejectionsAdapter.ViewHolder> {
    private final ArrayList<RestaurantInfo> mValues;
    private OnItemClickListener listener;
    private Context context;
    public RejectionsAdapter(ArrayList<RestaurantInfo> items) {
        mValues = items;
    }

    interface OnItemClickListener {
        void itemClick(int position);
        void deleteRejection(int position);
    }

    @NonNull
    @Override
    public RejectionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new RejectionsAdapter.ViewHolder(FragmentFavorRejectViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setViews(mValues.get(position));
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
        public final Button button, delete;

        public ViewHolder(FragmentFavorRejectViewBinding binding) {
            super(binding.getRoot());
            name = binding.name;
            keywords = binding.keywords;
            location = binding.location;
            phoneNumber = binding.phoneNumber;
            imageView = binding.image;
            button = binding.button;
            delete = binding.delete;

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.itemClick(getBindingAdapterPosition());
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.deleteRejection(getBindingAdapterPosition());
                }
            });
        }

        public void setViews(RestaurantInfo info) {
            name.setText(info.name);
            keywords.setText(info.keyword);
            location.setText(info.location);
            phoneNumber.setText(info.phoneNumber);

            if(info.imageUrl != null) {
                Glide.with(context).load(info.imageUrl).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
            }
            else{
                Glide.with(context).load(R.drawable.nopictures).into(imageView);
            }
        }

    }
}
