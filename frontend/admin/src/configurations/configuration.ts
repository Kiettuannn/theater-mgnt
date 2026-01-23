export const CONFIG = {
  // API: "http://localhost:8080/api/theater-mgnt",
  API: "http://18.183.39.22:8080/api/theater-mgnt",
}
export const API = {
  LOGIN: "/auth/admin/login",
  FORGOT_PASSWORD: "/auth/forgot-password",
  RESET_PASSWORD: "/auth/reset-password",
  MY_INFO: "staffs/myInfo",
  UPDATE_STAFF: "/staffs/${staffId}",
}

export const OAuthConfig = {
  clientId: "389202643503-grb2t3an3e95vn6fl1bp1m1039u3srij.apps.googleusercontent.com",
  redirectUri: "http://localhost:5173/authenticate",
  authUri: "https://accounts.google.com/o/oauth2/auth",
}