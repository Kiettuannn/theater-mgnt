import httpClient from "@/configurations/httpClient";
import type { Cinema } from "@/types/CinemaType/cinemaType";
import { handleApiResponse, type ApiResponse } from "@/utils/apiResponse";

// Get all cinemas for selection
export const getAllCinemas = async (): Promise<Cinema[]> => {
  return handleApiResponse<Cinema[]>(
    httpClient.get<ApiResponse<Cinema[]>>("/cinemas")
  );
};