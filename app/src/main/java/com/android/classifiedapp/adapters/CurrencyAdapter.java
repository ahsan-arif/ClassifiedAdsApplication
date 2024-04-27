package com.android.classifiedapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Currency;
import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CurrencyAdapter extends ArrayAdapter<Currency> implements Filterable {

    private ArrayList<Currency> countries;
    Context context;

    public CurrencyAdapter(Context context, int resource, ArrayList<Currency> countries) {
        super(context, resource, countries);
        this.context = context;
        this.countries = countries;
    }

    @Override
    public int getCount() {
        return countries.size();
    }

    @Override
    public Currency getItem(int position) {
        return countries.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_currency, parent, false);
        }

        // Set flag, name, and currency in the layout (replace with your layout IDs)
        ImageView flagImage =  convertView.findViewById(R.id.img_flag);
        //TextView nameText =  convertView.findViewById(R.id.currency_name);
        TextView currencyText = convertView.findViewById(R.id.tv_currency);

        Currency currency = getItem(position);
        Glide.with(context).load(currency.getImageUrl()).into(flagImage);
        LogUtils.e(currency);
        //flagImage.setImageResource(getFlagResource(currency.getFlag())); // Implement getFlagResource() to map flag name to resource ID
        //nameText.setText(currency.getCountry());
        currencyText.setText(currency.getCurrency());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new CurrencyFilter(this);
    }

    private static class CurrencyFilter extends Filter {
        private final CurrencyAdapter adapter;

        public CurrencyFilter(CurrencyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                ArrayList<Currency> filteredCurrencies = new ArrayList<>();
                for (Currency currency : adapter.countries) {
                    if (currency.getCountry().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        filteredCurrencies.add(currency);
                    }
                }
                results.values = filteredCurrencies;
                results.count = filteredCurrencies.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.countries = (ArrayList<Currency>) results.values;
            adapter.notifyDataSetChanged();
        }
    }
}
