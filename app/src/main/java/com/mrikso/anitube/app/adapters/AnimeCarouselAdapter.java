package com.mrikso.anitube.app.adapters;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.anitube.app.comparator.InteresingDiffCallback;
import com.mrikso.anitube.app.databinding.ItemInteresingAnimeBinding;
import com.mrikso.anitube.app.model.InteresingModel;
import com.mrikso.anitube.app.utils.ParserUtils;
import com.mrikso.anitube.app.utils.ViewUtils;

import java.util.List;

public class AnimeCarouselAdapter extends RecyclerView.Adapter<AnimeCarouselAdapter.ViewHolder> {

    private final AsyncListDiffer<InteresingModel> mDiffer;
    private OnItemClickListener listener;
    private Bitmap tiledBitmap;

    public AnimeCarouselAdapter() {
        mDiffer = new AsyncListDiffer<>(this, new InteresingDiffCallback());
    }

    public void submitList(List<InteresingModel> list) {
        mDiffer.submitList(list);
    }

    public void submitList(List<InteresingModel> list, Runnable commitCallback) {
        mDiffer.submitList(list, commitCallback);
    }

    public List<InteresingModel> getCurrentList() {
        return mDiffer.getCurrentList();
    }

    public void setTiledBitmap(Bitmap bitmap) {
        this.tiledBitmap = bitmap;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        List<InteresingModel> currentList = mDiffer.getCurrentList();
        int actualCount = currentList.size();
        // If we have only 1 item or it's empty, we don't need infinite scrolling logic
        return actualCount > 1 ? Integer.MAX_VALUE : actualCount;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<InteresingModel> currentList = mDiffer.getCurrentList();
        int actualCount = currentList.size();
        if (actualCount == 0) {
            return;
        }

        // Standard infinite scroll math
        int realPosition = position % actualCount;
        if (realPosition < 0 || realPosition >= actualCount) {
            return;
        }
        
        InteresingModel item = currentList.get(realPosition);
        if (item != null) {
            holder.bind(item);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemInteresingAnimeBinding binding = ItemInteresingAnimeBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemInteresingAnimeBinding binding;
        private final Matrix matrix1 = new Matrix();
        private final Matrix matrix2 = new Matrix();

        public ViewHolder(@NonNull ItemInteresingAnimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(InteresingModel model) {
            ViewUtils.loadImage(binding.carouselImageView, ParserUtils.normalizeUrl(model.getPosterUrl()));

            if (tiledBitmap != null) {
                setupTiledBackground(binding.carouselBg1, tiledBitmap);
                setupTiledBackground(binding.carouselBg2, tiledBitmap);
            }

            if (listener != null) {
                binding.getRoot().setOnClickListener(v -> listener.onCarouselItemSelected(model.getAnimeUrl()));
            }
        }

        private void setupTiledBackground(ImageView imageView, Bitmap bitmap) {
            if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
                BitmapDrawable drawable = new BitmapDrawable(imageView.getResources(), bitmap);
                drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                imageView.setImageDrawable(drawable);
            }
        }

        public void setOffset(float offset) {
            // Ensure matrices are updated correctly even during recycling
            matrix1.setTranslate(offset * 0.5f, 0);
            binding.carouselBg1.setImageMatrix(matrix1);

            matrix2.setTranslate(offset * 0.3f, 0);
            binding.carouselBg2.setImageMatrix(matrix2);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onCarouselItemSelected(String link);
    }
}
