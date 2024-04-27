package com.akapps.dailynote.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.akapps.dailynote.R;
import com.akapps.dailynote.classes.data.Photo;
import com.akapps.dailynote.classes.helpers.Helper;
import com.akapps.dailynote.classes.other.InfoSheet;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.listeners.OnDismissListener;

import java.io.File;
import java.util.ArrayList;

import io.realm.RealmResults;

public class photos_recyclerview extends RecyclerView.Adapter<photos_recyclerview.MyViewHolder> {

    // project data
    private final RealmResults<Photo> allPhotos;
    private final FragmentActivity activity;
    private final Context context;
    private final boolean showDelete;
    private final int photosSize;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final ImageView delete;
        private final ImageView share;
        private final View view;
        private final MaterialCardView background;
        private final TextView imageSize;

        public MyViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            delete = v.findViewById(R.id.delete);
            share = v.findViewById(R.id.share);
            background = v.findViewById(R.id.background);
            imageSize = v.findViewById(R.id.image_size);
            view = v;
        }
    }

    public photos_recyclerview(RealmResults<Photo> allPhotos, FragmentActivity activity,
                               Context context, boolean showDelete) {
        this.allPhotos = allPhotos;
        this.activity = activity;
        this.context = context;
        this.showDelete = showDelete;
        photosSize = allPhotos.size();
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
        Photo currentPhoto;
        try {
            currentPhoto = allPhotos.get(position);
        } catch (Exception e) {
            Helper.restart(activity);
            return;
        }

        if (!showDelete) {
            holder.delete.setVisibility(View.GONE);
            holder.share.setVisibility(View.GONE);
            holder.imageSize.setVisibility(View.GONE);
            holder.background.setCardBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        // populates photo into the recyclerview
        Glide.with(context).load(currentPhoto.getPhotoLocation())
                .centerCrop()
                .placeholder(activity.getDrawable(R.drawable.placeholder_image_icon))
                .into(holder.image);

        holder.imageSize.setText(Helper.getFormattedFileSize(context,
                new File(currentPhoto.getPhotoLocation()).length()));

        // if photo is clicked, it opens it in the default device gallery
        holder.view.setOnClickListener(v -> {
            ArrayList<String> images = new ArrayList<>();
            for (int i = 0; i < allPhotos.size(); i++) {
                if (!allPhotos.get(i).getPhotoLocation().isEmpty())
                    images.add(allPhotos.get(i).getPhotoLocation());
            }

            new StfalconImageViewer.Builder<>(context, images, (imageView, image) ->
                    Glide.with(context).load(image).into(imageView))
                    .withBackgroundColor(context.getResources().getColor(R.color.gray))
                    .allowZooming(true)
                    .withDismissListener(() -> notifyItemChanged(position))
                    .allowSwipeToDismiss(true)
                    .withHiddenStatusBar(false)
                    .withStartPosition(position)
                    .withTransitionFrom(holder.image)
                    .show();
        });

        // if photo is long clicked, it is deleted
        holder.delete.setOnClickListener(v -> {
            deleteDialog(position);
        });

        holder.share.setOnClickListener(view -> Helper.shareFile(activity, "image", "*", currentPhoto.getPhotoLocation(), ""));
    }

    @Override
    public int getItemCount() {
        try {
            return allPhotos.size();
        } catch (Exception e) {
            Helper.restart(activity);
            return photosSize;
        }
    }

    // dialog to ensure user wants to delete photo
    private void deleteDialog(int position) {
        InfoSheet info = new InfoSheet(4, position);
        info.show(activity.getSupportFragmentManager(), info.getTag());
    }
}
