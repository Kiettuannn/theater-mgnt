import type { Metadata } from 'next'
import { getMovieById, mapMovieForDisplay } from '@/lib/api-movie'

export async function generateMovieMetadata(id: string): Promise<Metadata> {
  try {
    const data = await getMovieById(id)

    if (!data) {
      return {
        title: 'Movie Not Found',
        description: 'The requested movie could not be found.',
      }
    }

    const movie = mapMovieForDisplay(data)

    if (!movie) {
      return {
        title: 'Movie Not Found',
        description: 'The requested movie could not be found.',
      }
    }

    const title = `${movie.title} - Đặt vé xem phim`
    const description = movie.description || `Xem phim ${movie.title}. ${movie.genre?.join(', ')}. Đặt vé ngay hôm nay!`
    const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000'
    const url = `${baseUrl}/movies/${id}`

    return {
      title,
      description,
      keywords: [
        movie.title,
        'đặt vé xem phim',
        ...(movie.genre || []),
        'rạp chiếu phim',
        'cinema',
      ],
      openGraph: {
        title: movie.title,
        description,
        url,
        siteName: 'Theater Management',
        images: [
          {
            url: movie.poster,
            width: 800,
            height: 1200,
            alt: movie.title,
          }
        ],
        type: 'video.movie',
        locale: 'vi_VN',
      },
      twitter: {
        card: 'summary_large_image',
        title: movie.title,
        description,
        images: [movie.poster],
      },
      alternates: {
        canonical: url,
      },
    }
  } catch (error) {
    console.error('Error generating metadata:', error)
    return {
      title: 'Movie Details',
      description: 'View movie details and book tickets',
    }
  }
}
