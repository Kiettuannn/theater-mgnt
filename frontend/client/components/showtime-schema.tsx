import Script from 'next/script'

interface ShowtimeSchemaProps {
  movie: {
    title: string
    poster: string
  }
  showtime: {
    id: string
    time: string
    date?: string
    price: number
    format?: string
  }
}

export function ShowtimeSchema({ movie, showtime }: ShowtimeSchemaProps) {
  const schema = {
    "@context": "https://schema.org",
    "@type": "ScreeningEvent",
    "name": `${movie.title} - ${showtime.time}`,
    "image": movie.poster,
    "startDate": showtime.date || new Date().toISOString(),
    "workPresented": {
      "@type": "Movie",
      "name": movie.title,
      "image": movie.poster
    },
    "location": {
      "@type": "MovieTheater",
      "name": "Theater Management",
      "address": {
        "@type": "PostalAddress",
        "addressCountry": "VN"
      }
    },
    "offers": {
      "@type": "Offer",
      "price": showtime.price,
      "priceCurrency": "VND",
      "availability": "https://schema.org/InStock",
      "url": `${process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000'}/booking`,
      "validFrom": new Date().toISOString()
    },
    "videoFormat": showtime.format || "Standard"
  }

  return (
    <Script
      id={`showtime-schema-${showtime.id}`}
      type="application/ld+json"
      dangerouslySetInnerHTML={{
        __html: JSON.stringify(schema)
      }}
    />
  )
}
