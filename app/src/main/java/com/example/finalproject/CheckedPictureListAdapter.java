package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CheckedPictureListAdapter extends ArrayAdapter<CheckedPictureItem> {

    public CheckedPictureListAdapter(Context context, int resource, ArrayList<CheckedPictureItem> objects) {
        super(context, resource, objects);
    }

    public CheckedPictureListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.story_list_item, parent, false);
        }
        CheckedPictureItem currentItem = getItem(position);
        CheckBox checkBox = convertView.findViewById(R.id.checked);
        checkBox.setTag(position);
        ImageView image = convertView.findViewById(R.id.image);
        TextView tags = convertView.findViewById(R.id.tags);
        TextView date = convertView.findViewById(R.id.date);

        assert currentItem != null;
        checkBox.setChecked(currentItem.getIsChecked());
        image.setImageBitmap(currentItem.getImageResource());
        tags.setText(currentItem.getTags());
        date.setText(currentItem.getDate());
        return convertView;
    }
}
