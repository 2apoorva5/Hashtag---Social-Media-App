package com.assistance.hashtagapp.BottomSheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.assistance.hashtagapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class UsernameRequirementsBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_username_requirement, container, false);
        return view;
    }
}
