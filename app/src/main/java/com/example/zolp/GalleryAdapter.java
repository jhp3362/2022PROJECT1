package com.example.zolp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.zolp.databinding.GalleryItemBinding;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Context context;
    private final ArrayList<Uri> uriList;
    private final ArrayList<String> nameList;
    private OnItemClickListener listener;


    public GalleryAdapter(Context context, ArrayList<Uri> uriList, ArrayList<String> nameList) {
        this.context = context;
        this.uriList = uriList;
        this.nameList = nameList;
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
        ViewCompat.setTransitionName(holder.imageView,"trans"+position);
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

    public void addItem(Uri uri, String name){
        uriList.add(uri);
        nameList.add(name);
    }

    public void destroyItem(int position){
        uriList.remove(position);
        nameList.remove(position);
    }
    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            Log.d("aaaaaaaaaa","안녕");
            convertView = inflater.inflate(R.layout.gallery_item, parent, false);
        }Log.d("aaaaaaaaaa","안녕2");
        ImageView imageView = convertView.findViewById(R.id.imageView);
        Glide.with(context).load(list.get(position)).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ImageActivity.class);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                byte[] byteArray = stream.toByteArray();
                intent.putExtra("index", position);
                intent.putExtra("image", byteArray);
                intent.putParcelableArrayListExtra("list", list);
                context.startActivity(intent);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.itemClick(v, position);
            }
        });
        return convertView;
    }*/

}
