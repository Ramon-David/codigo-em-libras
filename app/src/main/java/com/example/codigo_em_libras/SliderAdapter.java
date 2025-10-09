package com.example.codigo_em_libras;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHoler> {
    private List<SliderItem> sliderItems;
    private ViewPager2 viewPager2;
    private OnItemClickListener onItemClickListener;

    SliderAdapter(List<SliderItem> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SliderViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHoler(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.slide_item_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHoler holder, int position) {
        holder.setImage(sliderItems.get(position));
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    class SliderViewHoler extends RecyclerView.ViewHolder {
        private RoundedImageView imageView;

        SliderViewHoler(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
            imageView.setClickable(true);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
        }

        void setImage(SliderItem sliderItem) {
            imageView.setImageResource(sliderItem.getImage());
        }
    }
}
