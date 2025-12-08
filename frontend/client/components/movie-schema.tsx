import Script from 'next/script'

interface MovieSchemaProps {
  movie: {
    title: string
    description?: string
    poster: string
    rating?: string
    duration?: number
    releaseDate?: string
    director?: string
    cast?: string[]
    genre?: string[]
  }
}

export function MovieSchema({ movie }: MovieSchemaProps) {
  const schema = {
    "@context": "https://schema.org",
    "@type": "Movie",
    "name": movie.title,
    "image": movie.poster,
    "description": movie.description || `Xem phim ${movie.title}`,
    "genre": movie.genre || [],
    "contentRating": movie.rating || "Not Rated",
    "director": movie.director ? {
      "@type": "Person",
      "name": movie.director
    } : undefined,
    "actor": movie.cast?.map(actor => ({
      "@type": "Person",
      "name": actor
    })) || [],
    "datePublished": movie.releaseDate,
    "duration": movie.duration ? `PT${movie.duration}M` : undefined,
  }

  return (
    <Script
      id="movie-schema"
      type="application/ld+json"
      dangerouslySetInnerHTML={{
        __html: JSON.stringify(schema)
      }}
    />
  )
}
