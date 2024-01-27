package com.mukesh.countrypicker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mukeshsolanki.R;
import com.mukesh.countrypicker.listeners.OnItemClickListener;
import java.util.List;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesAdapter.ViewHolder> {

  private OnItemClickListener listener;
  private List<Country> countries;
  private Context context;
  private int textColor;


  public CountriesAdapter(Context context, List<Country> countries,
      OnItemClickListener listener, int textColor) {
    this.context = context;
    this.countries = countries;
    this.listener = listener;
    this.textColor = textColor;
  }


  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    final Country country = countries.get(position);
    holder.countryNameText.setText(country.getName());
    holder.countryNameText.setTextColor(textColor == 0 ? Color.BLACK : textColor);
    holder.countryCurrencySymbol.setText(country.getCurrencySymbol());
    country.loadFlagByCode(context);
    if (country.getFlag() != -1) holder.countryFlagImageView.setImageResource(country.getFlag());
    holder.rootView.setOnClickListener(v -> listener.onItemClicked(country));
  }

  @Override public int getItemCount() {
    return countries.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    private ImageView countryFlagImageView;
    private TextView countryNameText;
    private TextView countryCurrencySymbol;
    private LinearLayout rootView;

    ViewHolder(View itemView) {
      super(itemView);
      countryFlagImageView = itemView.findViewById(R.id.country_flag);
      countryNameText = itemView.findViewById(R.id.country_title);
      countryCurrencySymbol = itemView.findViewById(R.id.currency_symbol);
      rootView = itemView.findViewById(R.id.rootView);
    }
  }

}
