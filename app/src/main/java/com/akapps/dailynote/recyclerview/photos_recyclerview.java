package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.bumptech.glide.Glide;
import io.realm.RealmResults;

public class photos_recyclerview extends RecyclerView.Adapter<photos_recyclerview.MyViewHolder>{

    // project data
    private final RealmResults<Photo> allPhotos;
    private final FragmentActivity activity;
    private final Context context;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final ImageView delete;
        private final View view;

        public MyViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            delete = v.findViewById(R.id.delete);
            view = v;
        }
    }

    public photos_recyclerview(RealmResults<Photo> allPhotos, FragmentActivity activity, Context context) {
        this.allPhotos = allPhotos;
        this.activity = activity;
        this.context = context;
    }

    @Override
    public photos_recyclerview.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_photo_layout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        // retrieves current photo object
        Photo currentPhoto = allPhotos.get(position);

        // populates photo into the recyclerview
        Glide.with(context).load(currentPhoto.getPhotoLocation())
                .placeholder(activity.getDrawable(R.drawable.error_icon))
                .into(holder.image);

        // if photo is clicked, it opens it in the default device gallery
        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(currentPhoto.getPhotoLocation()), "image/*");
            activity.startActivity(intent);
        });

        // if photo is long clicked, it is deleted
        holder.delete.setOnClickListener(v -> {
            deleteDialog(position);
        });
    }

    @Override
    public int getItemCount() {
        return allPhotos.size();
    }

    // dialog to ensure user wants to delete photo
    private void deleteDialog(int position){
        InfoSheet info = new InfoSheet(4, position);
        info.show(activity.getSupportFragmentManager(), info.getTag());
    }
}
