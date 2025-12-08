import { MetadataRoute } from 'next'
import { getAllMovies } from '@/lib/api-movie'

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000'

  try {
    // Fetch all movies from API
    const movies = await getAllMovies()

    // Generate movie URLs
    const movieUrls = movies.map((movie: any) => ({
      url: `${baseUrl}/movies/${movie.id}`,
      lastModified: new Date(movie.updatedAt || new Date()),
      changeFrequency: 'daily' as const,
      priority: 0.8,
    }))

    return [
      {
        url: baseUrl,
        lastModified: new Date(),
        changeFrequency: 'daily',
        priority: 1,
      },
      {
        url: `${baseUrl}/booking`,
        lastModified: new Date(),
        changeFrequency: 'daily',
        priority: 0.7,
      },
      {
        url: `${baseUrl}/authenticate`,
        lastModified: new Date(),
        changeFrequency: 'monthly',
        priority: 0.5,
      },
      ...movieUrls,
    ]
  } catch (error) {
    console.error('Error generating sitemap:', error)
    // Return basic sitemap if API fails - include static pages + sample movie URLs
    return [
      {
        url: baseUrl,
        lastModified: new Date(),
        changeFrequency: 'daily',
        priority: 1,
      },
      {
        url: `${baseUrl}/booking`,
        lastModified: new Date(),
        changeFrequency: 'daily',
        priority: 0.7,
      },
      {
        url: `${baseUrl}/authenticate`,
        lastModified: new Date(),
        changeFrequency: 'monthly',
        priority: 0.5,
      },
      // Add sample movie URLs (update IDs based on your actual movies)
      {
        url: `${baseUrl}/movies/1`,
        lastModified: new Date(),
        changeFrequency: 'weekly',
        priority: 0.8,
      },
      {
        url: `${baseUrl}/movies/2`,
        lastModified: new Date(),
        changeFrequency: 'weekly',
        priority: 0.8,
      },
      {
        url: `${baseUrl}/movies/3`,
        lastModified: new Date(),
        changeFrequency: 'weekly',
        priority: 0.8,
      },
    ]
  }
}
