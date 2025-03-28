# 083205004013_NguyenHoHoangPhuc_Baitap5
## Câu 1:
- Tạo GoogleSignInClient nhằm xác định cấu hình đăng nhập từ GG.
- signInIntent để hiển thị giao diện đăng nhập của GG.
- startActivityForResult chuyển người dùng đến màn hình chọn tài khoản.
- Lấy idToken để xác thực với Firebase (firebaseAuthWithGoogle(account.idToken!!)).
- Phần birth day dùng firestore để lưu dữ liệu của người dùng nhập vào do firebase auth không cung cấp bith.
- Tạo function lưu dữ liệu người dùng vào firestore với documentID là UID của firebase.
## Hình ảnh mô tả cho câu 1:
- Màn hình đăng nhập:
- 
- Giao diện profile:
- 