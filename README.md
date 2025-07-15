
```markdown
# Product Sale Android App

Ứng dụng bán hàng Shopee style - Android + RESTful API

## 📱 Mô tả

Product Sale là ứng dụng Android cho phép người dùng:
- Đăng ký, đăng nhập tài khoản
- Duyệt, tìm kiếm, lọc sản phẩm
- Xem chi tiết, thêm vào giỏ hàng
- Quản lý giỏ hàng, đặt hàng, thanh toán (nhiều cổng)
- Nhận thông báo, hỗ trợ chat CSKH, xem bản đồ cửa hàng

> **Backend API** .NET, Node.js
> **Database:** SQL Server/MySQL

---

## 🚀 Công nghệ sử dụng

- Java 8+
- Android Studio (ViewBinding, Navigation)
- Retrofit2 (API client)
- Gson (JSON convert)
- Google Material UI
- View Binding
- SharedPreferences (lưu login, token)

---

## 📂 Cấu trúc thư mục

```

---

## ⚡️ Hướng dẫn cài đặt & chạy

1. **Clone code về máy:**
   ```sh
   git clone https://github.com/<your-account>/productsale-android.git
````

2. **Mở bằng Android Studio**

3. **Thêm cấu hình API base URL**
   Mặc định ở file `ApiClient.java`:

   ```java
   private static final String BASE_URL = "http://10.0.2.2:3000/"; // Đổi lại nếu backend chạy trên máy thật
   ```

4. **Khai báo quyền mạng trong `AndroidManifest.xml`**

   ```xml
   <uses-permission android:name="android.permission.INTERNET"/>
   ```

5. **Bật cho phép HTTP nếu backend chưa dùng HTTPS**

   * Tạo file `res/xml/network_security_config.xml`
   * Khai báo `<application android:networkSecurityConfig="@xml/network_security_config" ...>`

6. **Sync Gradle (Tools > Sync Project with Gradle Files)**

7. **Chạy ứng dụng trên Emulator hoặc điện thoại thật**

---

