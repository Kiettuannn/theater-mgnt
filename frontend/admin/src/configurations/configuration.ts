export const CONFIG = {
  // API: "http://localhost:8080/api/theater-mgnt",
  API: import.meta.env.VITE_API_URL, // Đã có /api/theater-mgnt trong env
}
export const API = {
  LOGIN: "/auth/admin/login",
  FORGOT_PASSWORD: "/auth/forgot-password",
  RESET_PASSWORD: "/auth/reset-password",
  MY_INFO: "staffs/myInfo",
  UPDATE_STAFF: "/staffs/${staffId}",
}
