# Muzic

## Giới thiệu
Muzic là ứng dụng nghe nhạc hiện đại, hỗ trợ phát nhạc trực tuyến, quản lý thư viện cá nhân, tạo playlist, tìm kiếm nghệ sĩ, và nhiều tính năng thông minh khác. Ứng dụng hướng tới trải nghiệm người dùng mượt mà, giao diện đẹp, hỗ trợ cả chế độ sáng/tối và đồng bộ dữ liệu đám mây.

## Môi trường phát triển
- **Hệ điều hành:** Windows 10/11, macOS, Linux
- **IDE:** Android Studio (Giraffe trở lên khuyến nghị)
- **JDK:** Java 17 trở lên
- **Gradle:** 7.x
- **Thiết bị:** Android 7.0 (API 24) trở lên

## Công cụ & Thư viện sử dụng
- **Ngôn ngữ:** Java, XML
- **Firebase Authentication & Firestore** (quản lý người dùng, dữ liệu)
- **ExoPlayer** (phát nhạc)
- **Picasso** (tải & hiển thị ảnh)
- **Material Components** (giao diện)
- **Retrofit2** (giao tiếp API)
- **Supabase** (lưu trữ ảnh)
- **LocalBroadcastManager** (đồng bộ UI)
- **SharedPreferences** (lưu cấu hình)

## Cài đặt môi trường
1. **Clone source:**
   ```bash
   git clone <repo-url>
   ```
2. **Mở bằng Android Studio**
3. **Cấu hình file `google-services.json`** (Firebase):
   - Tải file từ Firebase Console, đặt vào `app/`.
4. **Sync Gradle** và build project.
5. **Chạy trên thiết bị/emulator Android >= 7.0**

## Chức năng chính
- Đăng ký, đăng nhập, xác thực email
- Phát nhạc trực tuyến, điều khiển qua notification
- Tìm kiếm bài hát, nghệ sĩ, playlist
- Quản lý thư viện nhạc cá nhân, tạo/sửa/xóa playlist
- Lưu nhạc offline, phát nhạc chất lượng cao/thấp
- Giao diện đẹp, hỗ trợ dark mode
- Đồng bộ dữ liệu với đám mây

## Bảo mật & Hiệu suất
- Mã hóa thông tin nhạy cảm qua Firebase Auth
- Không lưu mật khẩu dưới dạng text thuần
- Sử dụng Firestore Security Rules
- Tối ưu tải ảnh, cache dữ liệu, giảm tiêu thụ bộ nhớ
- Sử dụng foreground service cho phát nhạc nền

## Tài liệu
- [Firebase Documentation](https://firebase.google.com/docs)
- [ExoPlayer](https://exoplayer.dev/)
- [Material Components](https://m3.material.io/)
- [Retrofit](https://square.github.io/retrofit/)

## Thành viên thực hiện
- **Lê Phước Ngọc Tân**
- **Phan Ngọc Sơn**
- **Hoàng Văn Tài** 