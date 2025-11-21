package com.example.nt118_marketingapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.nt118_marketingapp.model.User;

// lớp AddEditUserDialog -> dialog dùng để thêm dữ liệu
public class AddEditUserDialog extends Dialog {
    private User user; // 1 user kiểu user để lưu trữ
    private int position; // vị trí truyền vào trong list
    private boolean isEditMode; // chế độ edit hay thêm người dùng
    private AddEditListener listener; // 1 cái listener


    public interface AddEditListener { // interface để sử dụng callback
        void onSave(User user, int position, boolean isEdit, String password);
    }


    // khai báo các phương thức cần thiết
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

        // tham chiếu đến các thành phần giao diện
        TextView tvTitle = findViewById(R.id.tvDialogTitle);
        EditText edtName = findViewById(R.id.edtName);
        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPhone = findViewById(R.id.edtPhone);
        EditText edtPassword = findViewById(R.id.edtPassword);
        Spinner spRole = findViewById(R.id.spRole);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);


        // -----spiner -------------------
        // tạo adapter cho spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                                                                        R.array.user_roles,
                                                                        android.R.layout.simple_spinner_item);
        // thiết lập layout cho adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // thiết lập adapter cho spinner
        spRole.setAdapter(adapter);
        // ----- end spiner --------------


        // ----- tiêu đề của dialog -------
        if (isEditMode && user != null) {
            tvTitle.setText("Chỉnh sửa người dùng");
            edtName.setText(user.getFullName()); // set name và email truyền từ user
            edtEmail.setText(user.getEmail());
            edtPhone.setText(user.getPhone());
//            edtUsername.setText(user.getUsername());
//            edtPassword.setText(user.getPassword());
            spRole.setSelection(user.getRoleName().equals("Quản trị viên") ? 0 : 1);
            // ẩn edit password
            edtPassword.setVisibility(View.GONE);
        }
        // ----- end tiêu đề của dialog -------


        btnSave.setOnClickListener(v -> {
            // lấy giá trị
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String phone = edtPhone.getText().toString();
            String role = spRole.getSelectedItem().toString();
            String password = edtPassword.getText().toString();


            //tạo 1 user mới
            User newUser = new User("",name, role, email, phone);
            // gọi callback để lưu dữ liệu
             listener.onSave(newUser, position, isEditMode, password);
            // đóng dialog
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
