package com.example.nt118_marketingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class UsermanagerActivity extends AppCompatActivity {

    private RecyclerView recyclerUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermanager);

        recyclerUsers = findViewById(R.id.recyclerUsers);
        btnAdd = findViewById(R.id.btnAddUser);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu mẫu
        userList.add(new User("Trần Thị B", "tranthib@email.com", "0338066476", "tranb", "1234", "Quản trị viên"));
        userList.add(new User("Lê Văn C", "levanc@email.com", "0379988776", "lec", "1234", "Nhân viên"));
        userList.add(new User("Phạm Thu D", "phamthud@email.com", "0391122334", "thud", "1234", "Nhân viên"));

        userAdapter = new UserAdapter(this, userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(int position) {
                User user = userList.get(position);
                AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, user, position, true, (updatedUser, pos, isEdit) -> {
                    userList.set(pos, updatedUser);
                    userAdapter.notifyItemChanged(pos);
                    Toast.makeText(UsermanagerActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                });
                dialog.show();
            }

            @Override
            public void onDelete(int position) {
                ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(UsermanagerActivity.this, position, pos -> {
                    userList.remove(pos);
                    userAdapter.notifyItemRemoved(pos);
                    Toast.makeText(UsermanagerActivity.this, "Đã xóa người dùng!", Toast.LENGTH_SHORT).show();
                });
                dialog.show();
            }
        });

        recyclerUsers.setAdapter(userAdapter);

        btnAdd.setOnClickListener(v -> {
            AddEditUserDialog dialog = new AddEditUserDialog(UsermanagerActivity.this, null, -1, false, (newUser, pos, isEdit) -> {
                userList.add(newUser);
                userAdapter.notifyItemInserted(userList.size() - 1);
                Toast.makeText(UsermanagerActivity.this, "Đã thêm người dùng!", Toast.LENGTH_SHORT).show();
            });
            dialog.show();
        });
    }
}

