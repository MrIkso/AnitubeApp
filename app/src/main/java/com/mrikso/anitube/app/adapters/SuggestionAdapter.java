package com.mrikso.anitube.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ItemSearchBinding;
import com.mrikso.anitube.app.model.SimpleModel;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends ArrayAdapter<SimpleModel> {

    private List<SimpleModel> suggestionList;
    private OnSuggestionClickListener onClickListener;

    public SuggestionAdapter(Context context, int resource) {
        super(context, resource);
        suggestionList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return suggestionList.size();
    }

    @Override
    public SimpleModel getItem(int position) {
        return suggestionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<SimpleModel> suggestionList) {
        this.suggestionList = suggestionList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSearchBinding binding = ItemSearchBinding.inflate(inflater, parent, false);
        SimpleModel suggestion = suggestionList.get(position);
        binding.queryTv.setText(suggestion.getText());
        binding.searchImageView.setImageDrawable(
                parent.getContext().getDrawable(R.drawable.ic_north_east));
        binding.getRoot()
                .setOnClickListener(v -> onClickListener.suggestionClicked(suggestion.getUrl()));
        return binding.getRoot();
    }

    public interface OnSuggestionClickListener {
        void suggestionClicked(String url);
    }

    public void setOnClickListener(OnSuggestionClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
