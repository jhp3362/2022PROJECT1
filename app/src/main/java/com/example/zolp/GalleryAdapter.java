package com.example.zolp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zolp.databinding.GalleryItemBinding;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<Uri> uriList;
    private ArrayList<ImageInfo> infoList;
    private OnItemClickListener listener;
    private Pair<View, String>[] pairs;


    public GalleryAdapter(Context context) {
        this.context = context;
    }

    public void setList(ArrayList<Uri> uriList, ArrayList<ImageInfo> infoList) {
        this.uriList = uriList;
        this.infoList = infoList;
        pairs = new Pair[uriList.size()];
    }

    public void setInfoList(ArrayList<ImageInfo> infoList){
        this.infoList = infoList;
    }

    public ArrayList<Uri> getUriList() {
        return uriList;
    }

    public ArrayList<ImageInfo> getInfoList() {
        return infoList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(GalleryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = uriList.get(position);
        holder.setImageView(uri);
        holder.imageView.setTag(position);
        ViewCompat.setTransitionName(holder.imageView,"trans"+position);
        pairs[position] = Pair.create(holder.imageView, "trans"+position);
    }

    public Pair<View, String>[] getPairs() {
        return pairs;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        GalleryItemBinding binding;
        public ViewHolder(GalleryItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            imageView = binding.image;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.itemClick(v, getBindingAdapterPosition());
                }
            });
        }
        public void setImageView(Uri uri){
            Glide.with(context).load(uri).into(imageView);
        }
    }

    public void destroyItem(int position){
        uriList.remove(position);
        infoList.remove(position);
    }

}
