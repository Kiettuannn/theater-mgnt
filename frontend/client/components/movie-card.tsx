"use client"

import { Star, Clock } from "lucide-react"
import { useState } from "react"
import Link from "next/link"
import Image from "next/image"

interface MovieCardProps {
  movie: any
  onBook?: () => void
}

export function MovieCard({ movie, onBook }: MovieCardProps) {
  const [isHovered, setIsHovered] = useState(false)
  const [imageError, setImageError] = useState(false)

  return (
    <div>
      <Link href={`/movies/${movie.id}`}>
        <div
          className="group relative rounded-xl overflow-hidden bg-card dark:bg-slate-900 border border-border dark:border-slate-800 hover:border-purple-500/50 transition-all duration-300 cursor-pointer"
          onMouseEnter={() => setIsHovered(true)}
          onMouseLeave={() => setIsHovered(false)}
        >
          {/* Movie Poster */}
          <div className="relative h-80 overflow-hidden bg-muted dark:bg-slate-800">
            <Image
              src={imageError ? "/placeholder.svg" : (movie.poster || movie.posterUrl || "/placeholder.svg")}
              alt={movie.title}
              fill
              className="object-cover group-hover:scale-105 transition-transform duration-300"
              sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
              onError={() => setImageError(true)}
            />

            {/* Rating Badge */}
            <div className="absolute top-4 right-4 bg-yellow-500 text-slate-950 px-3 py-1 rounded-full text-sm font-bold">
              {movie.rating || movie.ageRatingName || 'NR'}
            </div>

            {/* Book Now Button (no blur, no dark overlay) */}
            {isHovered && (
              <div className="absolute inset-0 flex items-center justify-center">
                <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-md px-6 py-3 rounded-lg shadow-lg">
                  <span className="text-purple-700 dark:text-purple-300 font-semibold">View Details</span>
                </div>
              </div>
            )}
          </div>

          {/* Movie Info */}
          <div className="p-4 space-y-3">
            <h3 className="text-lg font-bold text-foreground dark:text-white line-clamp-2">{movie.title}</h3>

            {/* Genre Tags */}
            <div className="flex flex-wrap gap-2">
              {movie.genre && movie.genre.length > 0 ? (
                movie.genre.slice(0, 3).map((g: string) => (
                  <span
                    key={g}
                    className="text-xs bg-purple-500/20 text-purple-600 dark:text-purple-300 px-2 py-1 rounded"
                  >
                    {g}
                  </span>
                ))
              ) : movie.genreNames && movie.genreNames.length > 0 ? (
                movie.genreNames.slice(0, 3).map((g: string) => (
                  <span
                    key={g}
                    className="text-xs bg-purple-500/20 text-purple-600 dark:text-purple-300 px-2 py-1 rounded"
                  >
                    {g}
                  </span>
                ))
              ) : (
                <span className="text-xs text-muted-foreground">No genres</span>
              )}
            </div>

            {/* Director */}
            <p className="text-sm text-muted-foreground">
              <span className="text-muted-foreground/70">Director:</span> {movie.director}
            </p>

            {/* Movie Details */}
            <div className="flex items-center gap-4 text-sm text-muted-foreground pt-2 border-t border-border dark:border-slate-800">
              <div className="flex items-center gap-1">
                <Clock size={16} />
                <span>{movie.duration || movie.durationMinutes || 0} min</span>
              </div>
              {movie.rating && (
                <div className="flex items-center gap-1">
                  <Star size={16} className="text-yellow-500" />
                  <span>8.5/10</span>
                </div>
              )}
            </div>

            {/* Description */}
            {movie.description && (
              <p className="text-sm text-muted-foreground line-clamp-2">{movie.description}</p>
            )}
          </div>
        </div>
      </Link>
    </div>
  )
}
