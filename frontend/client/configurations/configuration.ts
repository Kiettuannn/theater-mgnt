export const CONFIG = {
  API: "http://localhost:8080/api/theater-mgnt",
};

export const API = {
  // Auth endpoints
  LOGIN: "/auth/customer/login",
  REGISTER: "/register",
  GOOGLE_AUTH: "/auth/outbound/authenticate",
  CREATE_PASSWORD: "/auth/accounts/create-password",
  MY_INFO: "/customers/myInfo",
  
  // Customer endpoints
  UPDATE_CUSTOMER: "/customers/${customerId}",
  CUSTOMER_LOYALTY_POINTS: "/customers/${customerId}/loyalty-points",
};

export const OAuthConfig = {
  clientId: "389202643503-grb2t3an3e95vn6fl1bp1m1039u3srij.apps.googleusercontent.com",
  redirectUri: "http://localhost:3000/authenticate",
  authUri: "https://accounts.google.com/o/oauth2/auth",
};

