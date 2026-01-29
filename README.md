# 🎬 Dự án Phần mềm Quản lý Chuỗi Rạp Phim

## 📌 Tổng quan
Hệ thống phần mềm quản lý chuỗi rạp phim bao gồm website dành cho **khách hàng** và **hệ thống quản trị**, hỗ trợ đặt vé, thanh toán trực tuyến, thông báo realtime và tích hợp AI tư vấn.

---

## 🛠️ Công nghệ sử dụng

### 🔹 Frontend (FE)

#### Admin
- **ReactJS**
- Deploy tại:  
  👉 https://uitcifastar-admin-ver-2.vercel.app/

#### Customer
- **Next.js**
- Deploy tại:  
  👉 https://cifastaruit.vercel.app/

---

### 🔹 Backend (BE)
- **Spring Boot**
- Deploy trên **AWS EC2**

---

### 🔹 Realtime & Communication
- **WebSocket**  
  → Dùng cho chức năng realtime thông báo đến nhân viên hoặc khách hàng (qua hệ thống hoặc email)

- **Email Service**  
  → Gửi email bằng **Brevo**

---

### 🔹 Thanh toán
- **VNPAY**  
  → Tích hợp cổng thanh toán trực tuyến VNPAY

---

### 🔹 Bảo mật
- **JWT** kết hợp **OAuth2**

---

### 🔹 Trí tuệ nhân tạo (AI)
- **Chatbot Gemini**
- Huấn luyện truy cập **nguồn dữ liệu động** được đẩy lên hệ thống
- Trả lời câu hỏi và **trích dẫn thông tin chính xác**

---

## 📖 Hướng dẫn sử dụng

### 👤 Khách hàng (Customer)
- Có thể truy cập hệ thống và **đăng nhập bằng Google**
- Thực hiện các chức năng đặt vé, thanh toán, nhận thông báo

### 🛠️ Quản trị viên (Admin)
- Đăng nhập bằng tài khoản:
  - **Username:** `admin`
  - **Password:** `admin`
- Dùng để **test và quản lý các chức năng trong hệ thống**

---
