import axios from 'axios'


const API_BASE_URL = 'http://localhost:8080/api/theater-mgnt'


const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})


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
  (error) => {
    console.error('API Error:', error.response?.data || error.message)
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

export default api