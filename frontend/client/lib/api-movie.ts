import axios from 'axios'
import { clearAuthData } from '@/services/localStorageService'
import { requestTokenRefresh } from "@/services/tokenRefresh";
import { useAuthStore } from '@/store'


const API_BASE_URL = 'http://localhost:8080/api/theater-mgnt'


const api = axios.create({
  baseURL: API_BASE_URL,
  // timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

const PUBLIC_API_PATHS = [
  "/movies",
  "/genres",
  "/screenings",
  "/cinemas",
  "/reviews",
  "/payment",
];

const isPublicRequest = (url?: string) => {
  if (!url) {
    return false;
  }
  const path = url.startsWith("http") ? new URL(url).pathname : url;
  return PUBLIC_API_PATHS.some((publicPath) => path.startsWith(publicPath));
};

const handleAuthFailure = () => {
  if (typeof window === "undefined") {
    return;
  }
  clearAuthData();
  useAuthStore.getState().logout();
};

const isRefreshRequest = (url?: string) => {
  if (!url) return false;
  const path = url.startsWith("http") ? new URL(url).pathname : url;
  return path.startsWith("/auth/refresh");
};


api.interceptors.request.use(
  (config) => {
    // Add auth token from localStorage
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("customer_token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    if (response.data?.result !== undefined) {
      return {
        ...response,
        data: response.data.result
      }
    }
    return response
  },
  async (error) => {
    const status = error?.response?.status;
    const code = error?.response?.data?.code;
    const url = error?.config?.url;
    const originalRequest = error?.config;
    const isCancelBookingRequest =
      typeof url === "string" &&
      url.includes("/bookings/") &&
      url.endsWith("/cancel");
    if (status === 401 || code === 1006) {
      if (
        originalRequest &&
        !originalRequest._retry &&
        !isRefreshRequest(originalRequest.url) &&
        !isPublicRequest(originalRequest.url)
      ) {
        originalRequest._retry = true;
        const refreshedToken = await requestTokenRefresh();
        if (refreshedToken && originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${refreshedToken}`;
          return api(originalRequest);
        }
      }
      handleAuthFailure();
    }
    if (
      status !== 401 &&
      !(isCancelBookingRequest && (status === 400 || status === 404))
    ) {
      console.error('API Error:', error.response?.data || error.message)
    }
    return Promise.reject(error)
  }
)

// ==================== MOVIE APIs ====================

export async function getMovieById(id: string) {
  const response = await api.get(`/movies/${id}`)
  return response.data
}

export async function getAllMovies() {
  const response = await api.get('/movies')
  return response.data
}

export async function getNowShowingMovies() {
  const response = await api.get('/movies/now-showing')
  return response.data
}

export async function getComingSoonMovies() {
  const response = await api.get('/movies/coming-soon')
  return response.data
}

export async function searchMovies(query: string) {
  const response = await api.get('/movies/search', {
    params: { title: query }
  })
  return response.data
}

// ==================== GENRE APIs ====================

export async function getAllGenres() {
  const response = await api.get('/genres')
  return response.data
}

export async function getGenreById(id: string) {
  const response = await api.get(`/genres/${id}`)
  return response.data
}

// ==================== AGE RATING APIs ====================

export async function getAllAgeRatings() {
  const response = await api.get('/age_ratings')
  return response.data
}

// ==================== SCREENING APIs ====================

export async function getScreeningsByMovieId(movieId: string) {
  const response = await api.get(`/screenings/movie/${movieId}`)
  return response.data
}

// ==================== CINEMA APIs ====================

export async function getAllCinemas() {
  const response = await api.get('/cinemas')
  return response.data
}

export async function getScreeningById(screeningId: string) {
  const response = await api.get(`/screenings/${screeningId}`)
  return response.data
}

export async function getAllScreenings() {
  const response = await api.get('/screenings')
  return response.data
}

// ==================== MAPPER ====================

export function mapMovieForDisplay(movie: any) {
  if (!movie) return null

  return {
    id: movie.id,
    title: movie.title || 'Untitled',
    description: movie.description || '',
    poster: movie.posterUrl || '/placeholder.svg',
    rating: movie.ageRating?.code || 'NR',
    duration: movie.durationMinutes || 0,
    releaseDate: movie.releaseDate || null,
    director: movie.director || 'Unknown',
    cast: movie.cast || movie.castMembers || [],
    genre: movie.genres?.map((g: any) => g.name) || [],
    trailerUrl: movie.trailerUrl || null,
    status: movie.status || 'COMING_SOON'
  }
}

export function mapScreeningToShowtime(screening: any) {
  if (!screening) return null

  const startTime = new Date(screening.startTime)
  const timeStr = startTime.toLocaleTimeString('en-US', { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false 
  })
  const dateStr = startTime.toISOString().split('T')[0]

  return {
    id: screening.id,
    movieId: screening.movieId,
    time: timeStr,
    date: dateStr,
    startDateTime: screening.startTime,
    format: screening.format || undefined,
    price: screening.price ? Number(screening.price) : undefined,
    availableSeats: screening.availableSeats !== undefined ? Number(screening.availableSeats) : undefined,
    roomId: screening.roomId,
    cinemaId: screening.cinemaId,
    roomName: screening.roomName,
    cinemaName: screening.cinemaName,
    status: screening.status
  }
}

// ==================== SCREENING SEAT APIs ====================

export async function getScreeningSeatsByScreeningId(screeningId: string) {
  const response = await api.get(`/screeningSeats/screening/${screeningId}`)
  return response.data
}

export function mapScreeningSeatToSeat(seat: any, index: number) {
  if (!seat) return null

  // Prefer new seatNumber field (row + number, e.g., "A1" or "A-1"), fallback to seatId
  const seatCode = seat.seatNumber || seat.seatId || ""
  let row = "A"
  let seatNumber = (index % 12) + 1

  if (seatCode) {
    const normalized = String(seatCode).trim()
    const match = normalized.match(/^([A-Za-z]+)[-\s]?([0-9]+)$/)

    if (match) {
      row = match[1].toUpperCase()
      seatNumber = parseInt(match[2], 10)
    } else {
      const parts = normalized.split("-")
      row = parts[0] ? String(parts[0]).toUpperCase() : row
      const parsed = parseInt(parts[1], 10)
      if (!Number.isNaN(parsed)) {
        seatNumber = parsed
      }
    }
  }

  // Map status to availability
  // Status can be: AVAILABLE, BOOKED, RESERVED, etc.
  const isAvailable = seat.status === 'AVAILABLE' || seat.status === null

  // Use seat type from response or fallback to column-based logic
  let type: 'standard' | 'vip' | 'couple' = 'standard'
  if (seat.seatType) {
    const seatTypeUpper = seat.seatType.toUpperCase()
    if (seatTypeUpper === 'VIP') {
      type = 'vip'
    } else if (seatTypeUpper === 'COUPLE') {
      type = 'couple'
    } else {
      type = 'standard'
    }
  } else {
    // Fallback: assign seat type based on column position
    const isVip = seatNumber >= 11
    const isCouple = seatNumber >= 5 && seatNumber <= 8
    type = isVip ? 'vip' : isCouple ? 'couple' : 'standard'
  }

  // Get price from response (convert from BigDecimal if needed)
  const price = seat.price ? Number(seat.price) : undefined

  return {
    id: seat.id || seat.seatNumber || seat.seatId || `${row}-${seatNumber}`,
    row,
    number: seatNumber,
    isAvailable,
    isSelected: false,
    type,
    price,
    // Transfer information
    isForTransfer: seat.isForTransfer || false,
    transferTicketId: seat.transferTicketId,
    sellerName: seat.sellerName,
    sellerEmail: seat.sellerEmail,
    sellerPhone: seat.sellerPhone,
  }
}

// ==================== COMBO APIs ====================

export async function getCombos() {
  const response = await api.get('/combos')
  return response.data
}

export async function getComboItemsByComboId(comboId: string) {
  const response = await api.get(`/comboItems/combo/${comboId}`)
  return response.data
}

// ==================== BOOKING APIs ====================

export async function createBooking(data: {
  customerId: string
  screeningId: string
  screeningSeatIds: string[]
}) {
  const response = await api.post('/bookings', data)
  return response.data
}

export async function getBookingSummary(bookingId: string) {
  const response = await api.get(`/bookings/${bookingId}/summary`)
  return response.data
}

export async function updateBookingCombos(
  bookingId: string,
  data: {
    combos: Array<{ comboId: string; quantity: number }>
  }
) {
  const response = await api.put(`/bookings/${bookingId}/combos`, data)
  return response.data
}

export async function redeemBookingPoints(
  bookingId: string,
  data: { pointsToRedeem: number }
) {
  const response = await api.post(`/bookings/${bookingId}/redeem-points`, data)
  return response.data
}

export async function cancelBooking(bookingId: string) {
  const response = await api.post(`/bookings/${bookingId}/cancel`)
  return response.data
}

// ==================== MAPPER ====================

export function mapComboForDisplay(combo: any) {
  if (!combo) return null

  return {
    id: combo.id,
    name: combo.name,
    description: combo.description || '',
    imageUrl: combo.imageUrl || '',
    price: Number(combo.price) || 0,
    // Use a popcorn emoji as a lightweight default icon since backend does not provide one
    icon: '🍿',
  }
}

export function mapComboItemDetail(item: any) {
  if (!item) return null

  return {
    id: item.id,
    comboName: item.comboName,
    name: item.name,
    quantity: Number(item.quantity) || 0,
  }
}

export default api