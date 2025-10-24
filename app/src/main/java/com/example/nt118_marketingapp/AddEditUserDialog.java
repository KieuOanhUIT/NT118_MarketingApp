package com.example.nt118_marketingapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class AddEditUserDialog extends Dialog {
    private User user;
    private int position;
    private boolean isEditMode;
    private AddEditListener listener;

    public interface AddEditListener {
        void onSave(User user, int position, boolean isEdit);
    }

    public AddEditUserDialog(@NonNull Context context, User user, int position, boolean isEditMode, AddEditListener listener) {
        super(context);
        this.user = user;
        this.position = position;
        this.isEditMode = isEditMode;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_edit_user);

        TextView tvTitle = findViewById(R.id.tvDialogTitle);
        EditText edtName = findViewById(R.id.edtName);
        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPhone = findViewById(R.id.edtPhone);
        EditText edtUsername = findViewById(R.id.edtUsername);
        EditText edtPassword = findViewById(R.id.edtPassword);
        Spinner spRole = findViewById(R.id.spRole);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        if (isEditMode && user != null) {
            tvTitle.setText("Chỉnh sửa người dùng");
            edtName.setText(user.getName());
            edtEmail.setText(user.getEmail());
            edtPhone.setText(user.getPhone());
            edtUsername.setText(user.getUsername());
            edtPassword.setText(user.getPassword());
            spRole.setSelection(user.getRole().equals("Quản trị viên") ? 0 : 1);
        }

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String phone = edtPhone.getText().toString();
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();
            String role = spRole.getSelectedItem().toString();

            User newUser = new User(name, email, phone, username, password, role);
            listener.onSave(newUser, position, isEditMode);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
