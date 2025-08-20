package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class PictureListAdapter extends ArrayAdapter<PictureItem> {

    public PictureListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public PictureListAdapter(Context context, int resource, ArrayList<PictureItem> objects) {
        super(context, resource, objects);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.picture_list_item, parent, false);
        }
        PictureItem currentItem = getItem(position);
        ImageView image = convertView.findViewById(R.id.image);
        TextView tags = convertView.findViewById(R.id.tags);
        TextView date = convertView.findViewById(R.id.date);

        assert currentItem != null;
        image.setImageBitmap(currentItem.getImageResource());
        tags.setText(currentItem.getTags());
        date.setText(currentItem.getDate());
        return convertView;
    }
}
