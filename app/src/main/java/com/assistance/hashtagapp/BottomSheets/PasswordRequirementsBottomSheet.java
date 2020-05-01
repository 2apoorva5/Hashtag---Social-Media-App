package com.assistance.hashtagapp.BottomSheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.assistance.hashtagapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PasswordRequirementsBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_requirement_bottom_sheet, container, false);
        return view;
    }
}
