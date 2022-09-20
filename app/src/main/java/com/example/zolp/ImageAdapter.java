package com.example.zolp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zolp.databinding.ViewImageBinding;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    ArrayList<Uri> list;
    Context context;
    public ImageAdapter(Context context, ArrayList<Uri>list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ViewImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        Uri uri = list.get(position);
        holder.setImageView(uri);
        holder.imageView.setTag(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return list.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView imageView;
        public ViewHolder(ViewImageBinding binding) {
            super(binding.getRoot());
            imageView = binding.image;
        }
        public void setImageView(Uri uri){
            Glide.with(context).load(uri).into(imageView);
        }

    }

    public void destroyItem(int position){
        list.remove(position);
    }
}
