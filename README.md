# Project Structure 
app/
├── manifests/
│   └── AndroidManifest.xml              # Khai báo activity, permission, intent-filter,...
│
├── java/
│   └── com.example.myapp/
│       │
│       ├── firebase/                    # (1) Các lớp xử lý Firebase 
│       │   ├── FirebaseConfig.java      # Cấu hình Firebase (Auth, Database, Storage)
│       │   ├── AuthManager.java         # Đăng nhập / đăng ký / đăng xuất -> tham khảo 
│       │   ├── DatabaseManager.java     # CRUD với Realtime DB hoặc Firestore -> tham khảo 
│       │   └── StorageManager.java      # Upload/download hình ảnh, file -> tham khảo
│       │
│       │
│       ├── model/                       # (2) Các lớp mô hình dữ liệu (Java Object)
│       │   ├── User.java
│       │   ├── Product.java
│       │   └── Order.java
│       │
│       │
│       │── adapters/                    # (3) Adapter cho RecyclerView / ViewPager
│       │   ├── ProductAdapter.java
│       │   └── OrderAdapter.java
│       │    
│       │
│       ├── MainActivity.java            # Màn hình chính  
│       │── LoginActivity.java           # Các activity chính (minh họa)
│       │── RegisterActivity.java
│       │── HomeActivity.java
│       │── ProfileActivity.java
│         
│       
│
└── res/
    ├── layout/                          # Layout XML cho Activity/Fragment
    │   ├── activity_login.xml
    │   ├── activity_home.xml
    │   └── item_product.xml
    │
    ├── drawable/                        # Hình ảnh, icon, background,...
    ├── values/
        ├── colors.xml
        ├── strings.xml
        └── styles.xml
