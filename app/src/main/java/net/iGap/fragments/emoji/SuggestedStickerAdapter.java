package net.iGap.fragments.emoji;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.iGap.fragments.emoji.struct.StructIGSticker;
import net.iGap.helper.LayoutCreator;
import net.iGap.module.customView.StickerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SuggestedStickerAdapter extends RecyclerView.Adapter<SuggestedStickerAdapter.StickerViewHolder> {
    private List<StructIGSticker> igStickers = new ArrayList<>();
    private AddStickerDialogListener listener;

    public void setListener(AddStickerDialogListener listener) {
        this.listener = listener;
    }

    @NotNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View normalSticker = new StickerView(parent.getContext());
        normalSticker.setLayoutParams(LayoutCreator.createFrame(75, 75, Gravity.CENTER, 2, 0, 2, 0));
        return new StickerViewHolder(normalSticker);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        holder.bindView(igStickers.get(position));
    }

    @Override
    public int getItemCount() {
        return igStickers.size();
    }

    public void setIgStickers(List<StructIGSticker> igStickers) {
        this.igStickers = igStickers;
        notifyDataSetChanged();
    }

    public void clearData() {
        igStickers.clear();
        notifyDataSetChanged();
    }

    public class StickerViewHolder extends RecyclerView.ViewHolder {
        private StickerView normalStickerCell;

        StickerViewHolder(View itemView) {
            super(itemView);
            normalStickerCell = (StickerView) itemView;
        }

        public void bindView(StructIGSticker structIGSticker) {
            normalStickerCell.loadSticker(structIGSticker);

            normalStickerCell.setOnClickListener(v -> listener.onStickerClick(structIGSticker));

            normalStickerCell.setOnLongClickListener(v -> {
                listener.onStickerClick(structIGSticker);
                return false;
            });
        }
    }

    public interface AddStickerDialogListener {
        void onStickerClick(StructIGSticker structIGSticker);
    }
}

