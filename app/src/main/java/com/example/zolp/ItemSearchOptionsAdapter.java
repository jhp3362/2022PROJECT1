package com.example.zolp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zolp.databinding.ViewSearchOptionBinding;

import java.util.ArrayList;

public class ItemSearchOptionsAdapter  extends RecyclerView.Adapter<ItemSearchOptionsAdapter.ViewHolder> {
    private final ArrayList<String> list;
    private OnItemClickListener listener;
    private int selectedNum;

    public ItemSearchOptionsAdapter(ArrayList<String> list, int selectedNum) {
        this.list = list;
        this.selectedNum = selectedNum;
    }

    interface OnItemClickListener {
        void itemClick(int position);
    }

    @NonNull
    @Override
    public ItemSearchOptionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemSearchOptionsAdapter.ViewHolder(ViewSearchOptionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemSearchOptionsAdapter.ViewHolder holder, int position) {
        holder.textView.setText(list.get(position));
        if(position==selectedNum){
            holder.textView.setBackgroundResource(R.drawable.location_and_keyword_selected_box);
            holder.textView.setTextColor(Color.parseColor("#000000"));
        }
        else{
            holder.textView.setBackgroundResource(R.drawable.location_and_keyword_box);
            holder.textView.setTextColor(Color.parseColor("#837C7C"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void selectItem(int newNum){
        if(selectedNum!=newNum) {
            int oldNum = selectedNum;
            this.selectedNum = newNum;
            notifyItemChanged(oldNum);
            notifyItemChanged(newNum);
        }
    }

    public String getItem(int position){
        return list.get(position);
    }

    public int getSelectedNum(){
        return selectedNum;
    }

    public void setOnItemClickListener(ItemSearchOptionsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView textView;
        public ViewHolder(ViewSearchOptionBinding binding) {
            super(binding.getRoot());
            textView = binding.textView;

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.itemClick(getBindingAdapterPosition());
                }
            });
        }
    }
}
