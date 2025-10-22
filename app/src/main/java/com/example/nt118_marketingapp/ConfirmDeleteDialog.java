package com.example.nt118_marketingapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ConfirmDeleteDialog extends Dialog {

    public interface DeleteListener {
        void onDeleteConfirmed(int position);
    }

    private int position;
    private DeleteListener listener;

    public ConfirmDeleteDialog(@NonNull Context context, int position, DeleteListener listener) {
        super(context);
        this.position = position;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm_delete);

//        TextView tvMessage = findViewById(R.id.tvMessage);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnCancel = findViewById(R.id.btnCancel);

//        tvMessage.setText("Bạn có chắc chắn muốn xóa người dùng này không?\nHành động này không thể hoàn tác.");

        btnDelete.setOnClickListener(v -> {
            listener.onDeleteConfirmed(position);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
