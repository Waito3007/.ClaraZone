package com.hoyo.cz.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hoyo.cz.Activity.DeletePostActivity;
import com.hoyo.cz.Activity.EditPostActivity;
import com.hoyo.cz.R;

public class OptionsFragment extends DialogFragment {

    private String pid;

    public OptionsFragment(String pid) {
        this.pid = pid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        TextView tvEdit = view.findViewById(R.id.tvEdit);
        TextView tvDelete = view.findViewById(R.id.tvDelete);

        // Xử lý khi nhấn vào nút Chỉnh sửa
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditPostActivity.class);
            intent.putExtra("pid", pid);
            startActivity(intent);
            dismiss(); // Đóng fragment sau khi chuyển sang EditPostActivity
        });

        // Xử lý khi nhấn vào nút Xóa
        tvDelete.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DeletePostActivity.class);
            intent.putExtra("pid", pid);
            startActivity(intent);
            dismiss(); // Đóng fragment sau khi chuyển sang DeletePostActivity
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation); // Thiết lập animation cho fragment
            getDialog().getWindow().setDimAmount(0.5f); // Thiết lập độ mờ của nền
        }
    }
}
