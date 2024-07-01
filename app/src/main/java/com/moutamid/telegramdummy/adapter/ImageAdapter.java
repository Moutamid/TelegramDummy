package com.moutamid.telegramdummy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.utili.OnClick;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageVH>{
    Context context;
    ArrayList<String> list;
    OnClick onClick;

    public ImageAdapter(Context context, ArrayList<String> list, OnClick onClick) {
        this.context = context;
        this.list = list;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageVH(LayoutInflater.from(context).inflate(R.layout.image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageVH holder, int position) {
        String s = list.get(holder.getAbsoluteAdapterPosition());
        Glide.with(context).load(s).placeholder(R.color.black).into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            int[] location = new int[2];
            holder.imageView.getLocationOnScreen(location); // Get image view location on screen

            int clickedX = (int) v.getX();
            int clickedY = (int) v.getY();

            int imageX = location[0];
            int imageY = location[1];
            int imageWidth = holder.imageView.getWidth();
            int imageHeight = holder.imageView.getHeight();

            if (clickedX >= imageX && clickedX <= imageX + imageWidth && clickedY >= imageY && clickedY <= imageY + imageHeight) {
                // Check for transparent pixel
                int pixel = getPixelColor(holder.imageView, clickedX - imageX, clickedY - imageY);
                int alpha = Color.alpha(pixel);

                if (alpha != 255) {
                    // Click on transparent area, close preview
                    // Your logic to close preview here
                    Log.d("Preview", "Transparent area clicked");
                    onClick.onClick();
                } else {
                    // Click on non-transparent area, perform intended action
                    Log.d("Preview", "Non-transparent area clicked");
                }
            } else {
                Log.d("Preview", "Clicked outside image");
                onClick.onClick();
            }
        });
    }

    private int getPixelColor(ImageView imageView, int x, int y) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        return bitmap.getPixel(x, y);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ImageVH extends RecyclerView.ViewHolder{
        ImageView imageView;
        public ImageVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }

}
