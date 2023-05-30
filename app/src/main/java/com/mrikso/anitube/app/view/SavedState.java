package com.mrikso.anitube.app.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.mrikso.anitube.app.parser.video.model.PlayerModel;
import com.mrikso.treeview.TreeItem;

public class SavedState extends View.BaseSavedState {
    TreeItem<PlayerModel> model;

    SavedState(Parcelable superState) {
        super(superState);
    }

    private SavedState(Parcel in, ClassLoader classLoader) {
        super(in);
        model = (TreeItem<PlayerModel>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeSerializable(model);
    }

    public static final ClassLoaderCreator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel source, ClassLoader loader) {
            return new SavedState(source, loader);
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
