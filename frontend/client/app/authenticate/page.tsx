"use client";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, useEffect } from "react";
import { setToken } from "@/services/localStorageService";
import { useAuthStore } from "@/store";
import { getMyInfo } from "@/services/customerService";
import { createPassword } from "@/services/authService";
import { CreatePasswordModal } from "@/components/create-password-modal";

export default function Authenticate() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [isLoggedin, setIsLoggedin] = useState(false);
  const [showCreatePassword, setShowCreatePassword] = useState(false);
  const login = useAuthStore((state) => state.login);

  useEffect(() => {
    const authCodeRegex = /code=([^&]+)/;
    const isMatch = window.location.href.match(authCodeRegex);

    if (isMatch) {
      const authCode = isMatch[1];

      fetch(
        `http://localhost:8080/api/theater-mgnt/auth/outbound/authenticate?code=${authCode}`,
        {
          method: "POST",
        }
      )
        .then((response) => {
          return response.json();
        })
        .then(async (data) => {
          setToken(data.result?.token);
          login(); // Cập nhật auth store để Header re-render

          // Fetch user info to check noPassword
          try {
            const userInfo = await getMyInfo();

            if (userInfo.noPassword === true) {
              // Show create password modal
              setShowCreatePassword(true);
            } else {
              // Redirect to home if password already exists
              setIsLoggedin(true);
            }
          } catch (error) {
            console.error("Failed to fetch user info:", error);
            // Still redirect to home on error
            setIsLoggedin(true);
          }
        })
        .catch((error) => {
          console.error("Authentication error:", error);
        });
    }
  }, [login]);

  useEffect(() => {
    if (isLoggedin) {
      router.push("/");
    }
  }, [isLoggedin, router]);

  const handleCreatePassword = async (
    password: string,
    confirmPassword: string
  ) => {
    try {
      await createPassword(password, confirmPassword);
      setShowCreatePassword(false);
      setIsLoggedin(true);
    } catch (error) {
      console.error("Failed to create password:", error);
      throw error;
    }
  };

  const handleCloseModal = () => {
    // Allow user to skip and still redirect to home
    setShowCreatePassword(false);
    setIsLoggedin(true);
  };

  return (
    <>
      <div className="flex flex-col gap-8 justify-center items-center h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
        <p className="text-lg text-gray-700">Authenticating...</p>
      </div>

      <CreatePasswordModal
        isOpen={showCreatePassword}
        onClose={handleCloseModal}
        onSubmit={handleCreatePassword}
      />
    </>
  );
}
