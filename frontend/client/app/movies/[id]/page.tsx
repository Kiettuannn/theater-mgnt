"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import Image from "next/image"
import { use } from "react"
import { ChevronLeft, Clock } from "lucide-react"
import { showtimes } from "@/lib/mock-data"
import type { Showtime } from "@/lib/types"
import { getMovieById, mapMovieForDisplay } from "@/lib/api-movie"
import { MovieSchema } from "@/components/movie-schema"
import { ShowtimeSchema } from "@/components/showtime-schema"

export default function MovieDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const [movie, setMovie] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const movieShowtimes = showtimes[id] || []
  const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(null)

  // Fetch movie khi component mount
  useEffect(() => {
    const fetchMovie = async () => {
      try {
        setLoading(true)
        console.log('🎬 Fetching movie ID:', id)


        const data = await getMovieById(id)

        console.log('📦 Raw data:', data)

        if (data) {
          const mappedMovie = mapMovieForDisplay(data)
          console.log('✅ Mapped movie:', mappedMovie)
          setMovie(mappedMovie)
        } else {
          console.error('❌ No data returned')
        }
      } catch (error) {
        console.error('❌ Error fetching movie:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchMovie()
  }, [id])

  // Loading state
  if (loading) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 pt-20">
        <div className="container-max px-4 md:px-8 py-12">
          <div className="animate-pulse space-y-8">
            <div className="bg-gray-700 h-96 rounded-xl"></div>
            <div className="space-y-4">
              <div className="bg-gray-700 h-8 w-1/2 rounded"></div>
              <div className="bg-gray-700 h-4 w-full rounded"></div>
              <div className="bg-gray-700 h-4 w-3/4 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  // Movie not found
  if (!movie) {
    return (
      <div className="min-h-screen bg-background dark:bg-slate-950 flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-3xl font-bold mb-4">Movie Not Found</h1>
          <p className="text-muted-foreground mb-6">ID: {id}</p>
          <Link href="/" className="text-purple-600 hover:text-purple-700 font-semibold">
            Back to Home
          </Link>
        </div>
      </div>
    )
  }

  return (
    <>
      {/* SEO: Structured Data */}
      {movie && <MovieSchema movie={movie} />}
      {movie && movieShowtimes.map((showtime) => (
        <ShowtimeSchema key={showtime.id} movie={movie} showtime={showtime} />
      ))}

      <div className="min-h-screen bg-background dark:bg-slate-950 pt-20">
      {/* Movie Hero */}
      <div className="relative h-96 md:h-[500px] overflow-hidden bg-gradient-to-b from-purple-900/20 to-background dark:to-slate-950">
        <Image
          src={movie.poster || "/placeholder.svg"}
          alt={movie.title}
          fill
          priority
          className="object-cover opacity-40"
          sizes="100vw"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background dark:from-slate-950 via-transparent" />

        <div className="absolute top-6 left-6 z-20">
          <Link
            href="/"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-black/40 backdrop-blur-md
                       text-white font-medium hover:bg-black/60 transition-colors"
          >
            <ChevronLeft size={20} />
            Back to Home
          </Link>
        </div>
      </div>

      {/* Movie Info */}
      <div className="container-max px-4 md:px-8 -mt-32 relative z-10 mb-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Poster */}
          <div className="flex justify-center md:justify-start">
            <div className="relative w-48 h-72">
              <Image
                src={movie.poster || "/placeholder.svg"}
                alt={movie.title}
                fill
                className="rounded-xl shadow-2xl object-cover"
                sizes="(max-width: 768px) 192px, 192px"
              />
            </div>
          </div>

          {/* Details */}
          <div className="md:col-span-2 space-y-6">
            <div>
              <h1 className="text-4xl md:text-5xl font-bold mb-4">{movie.title}</h1>
              <div className="flex flex-wrap gap-2 mb-4">
                {movie.genre && movie.genre.length > 0 ? (
                  movie.genre.map((g: string) => (
                    <span
                      key={g}
                      className="px-3 py-1 rounded-full bg-purple-500/20 text-purple-600 dark:text-purple-300 text-sm font-semibold"
                    >
                      {g}
                    </span>
                  ))
                ) : (
                  <span className="text-muted-foreground text-sm">No genres</span>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Rating</p>
                <p className="text-2xl font-bold">{movie.rating || 'NR'}</p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Duration</p>
                <p className="text-2xl font-bold flex items-center gap-2">
                  <Clock size={20} />
                  {movie.duration || 0}m
                </p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Release</p>
                <p className="text-lg font-bold">
                  {movie.releaseDate ? new Date(movie.releaseDate).toLocaleDateString() : 'TBA'}
                </p>
              </div>
              <div className="bg-card dark:bg-slate-900 rounded-lg p-4 border border-border dark:border-slate-800">
                <p className="text-muted-foreground text-sm mb-1">Director</p>
                <p className="text-lg font-bold">{movie.director || 'Unknown'}</p>
              </div>
            </div>

            {/* ✅ FIX: Cast có thể là string hoặc array */}
            {movie.cast && movie.cast.length > 0 && (
              <div>
                <h3 className="text-lg font-bold mb-2">Cast</h3>
                <p className="text-muted-foreground">
                  {Array.isArray(movie.cast) ? movie.cast.join(", ") : movie.cast}
                </p>
              </div>
            )}

            <div>
              <h3 className="text-lg font-bold mb-2">Synopsis</h3>
              <p className="text-muted-foreground leading-relaxed">
                {movie.description || 'No description available.'}
              </p>
            </div>

            {/* Trailer button nếu có */}
            {movie.trailerUrl && (
              <div>
                <a
                  href={movie.trailerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-2 px-6 py-3 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 transition-colors"
                >
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                  Watch Trailer
                </a>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Showtimes */}
      <div className="container-max px-4 md:px-8 py-12">
        <h2 className="text-3xl font-bold mb-8">Select Showtime</h2>

        {movieShowtimes.length > 0 ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              {movieShowtimes.map((showtime) => (
                <button
                  key={showtime.id}
                  onClick={() => setSelectedShowtime(showtime)}
                  className={`p-6 rounded-xl border-2 transition-all ${
                    selectedShowtime?.id === showtime.id
                      ? "border-purple-600 bg-purple-500/10 dark:bg-purple-900/20"
                      : "border-border dark:border-slate-800 bg-card dark:bg-slate-900 hover:border-purple-600"
                  }`}
                >
                  <p className="text-2xl font-bold mb-2">{showtime.time}</p>
                  <p className="text-sm text-muted-foreground mb-3">{showtime.format}</p>
                  <p className="text-lg font-semibold text-purple-600 mb-3">
                    {showtime.price.toLocaleString()} VND
                  </p>
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <span>{showtime.availableSeats} seats available</span>
                  </div>
                </button>
              ))}
            </div>

            {selectedShowtime && (
              <div className="mt-8 flex justify-center">
                <Link
                  href={`/booking/${id}/${selectedShowtime.id}`}
                  className="px-8 py-4 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg transition-all"
                >
                  Continue to Booking
                </Link>
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-12 bg-slate-800/50 rounded-xl">
            <p className="text-muted-foreground text-lg">
              Showtimes for this movie will be available soon
            </p>
          </div>
        )}
      </div>
    </div>
    </>
  )
}












