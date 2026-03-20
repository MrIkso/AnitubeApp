package com.mrikso.anitube.app.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.treeview.TreeItem;

import java.util.HashMap;
import java.util.Map;

public class SavedState extends View.BaseSavedState {
    TreeItem<PlayerModel> model;
    Map<String, Integer> levelColors;
    SavedState(Parcelable superState) {
        super(superState);
    }

    private SavedState(Parcel in) {
        super(in);
        model = (TreeItem<PlayerModel>) in.readSerializable();

        int size = in.readInt();
        levelColors = new HashMap<>();
        for (int i = 0; i < size; i++) {
            levelColors.put(in.readString(), in.readInt());
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(model);

        if (levelColors == null) {
            out.writeInt(0);
        } else {
            out.writeInt(levelColors.size());
            for (Map.Entry<String, Integer> entry : levelColors.entrySet()) {
                out.writeString(entry.getKey());
                out.writeInt(entry.getValue());
            }
        }
    }

    public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new SavedState(source);
        }

        @Override
        public SavedState createFromParcel(Parcel source) {
            return createFromParcel(null);
        }

        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };
}
