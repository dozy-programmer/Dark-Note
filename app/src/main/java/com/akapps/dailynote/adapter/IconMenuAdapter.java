package com.akapps.dailynote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.other.IconPowerMenuItem;
import com.skydoves.powermenu.MenuBaseAdapter;

public class IconMenuAdapter extends MenuBaseAdapter<IconPowerMenuItem> {

    private boolean isShowingNumbers;

    public IconMenuAdapter(boolean isShowingNumbers){
        this.isShowingNumbers = isShowingNumbers;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(!isShowingNumbers)
                view = inflater.inflate(R.layout.menu_icon_title_item, viewGroup, false);
            else
                view = inflater.inflate(R.layout.menu_number_item, viewGroup, false);
        }

        IconPowerMenuItem item = (IconPowerMenuItem) getItem(index);
        if(isShowingNumbers){
            final TextView title = view.findViewById(R.id.item_title);
            title.setText(item.getTitle());
        }
        else {
            final ImageView icon = view.findViewById(R.id.item_icon);
            icon.setImageDrawable(item.getIcon());
            final TextView title = view.findViewById(R.id.item_title);
            title.setText(item.getTitle());
        }
        return super.getView(index, view, viewGroup);
    }
}