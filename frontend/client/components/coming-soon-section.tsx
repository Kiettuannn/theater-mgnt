"use client"

import type React from "react"
import { Bell } from "lucide-react"
import { MovieCard } from "./movie-card"
import { useState, useEffect } from "react"
import { getComingSoonMovies, mapMovieForDisplay } from "@/lib/api-movie"

export function ComingSoonSection() {
  const [movies, setMovies] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [notifyEmail, setNotifyEmail] = useState("")
  const [notified, setNotified] = useState(false)

  // Fetch movies khi component mount
    useEffect(() => {
      const fetchMovies = async () => {
        try {
          setLoading(true)
          const data = await getComingSoonMovies()

          // ✅ Kiểm tra data trước khi map
          if (data && Array.isArray(data)) {
            const mappedMovies = data.map(mapMovieForDisplay)
            setMovies(mappedMovies)
          } else {
            console.error('API response is not an array:', data)
            setMovies([])
          }
        } catch (error) {
          console.error('Error fetching movies:', error)
          setMovies([])
        } finally {
          setLoading(false)
        }
      }

      fetchMovies()
    }, [])

  const handleNotify = (e: React.FormEvent) => {
    e.preventDefault()
    setNotified(true)
    setTimeout(() => setNotified(false), 3000)
  }

  return (
    <section
      id="coming-soon"
      className="section-padding bg-gradient-to-b from-slate-950 to-slate-900 relative overflow-hidden"
    >
      {/* Background elements */}
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-pink-600/5 rounded-full blur-3xl" />

      <div className="container-max relative z-10">
        {/* Section Header */}
        <div className="mb-12 space-y-4">
          <div className="inline-block px-4 py-2 rounded-full bg-pink-500/20 border border-pink-500/50">
            <span className="text-pink-300 text-sm font-semibold">Upcoming Releases</span>
          </div>

          <h2 className="text-4xl md:text-5xl font-bold">
            <span className="gradient-text">Coming Soon</span>
          </h2>

          <p className="text-xl text-slate-400 max-w-2xl">
            Get excited for the most anticipated films coming to CINEPLEX. Reserve your seats in advance.
          </p>
        </div>

        {/* Loading state */}
        {loading && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-16">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="animate-pulse">
                <div className="bg-gray-700 aspect-[2/3] rounded-lg mb-3"></div>
                <div className="h-4 bg-gray-700 rounded mb-2"></div>
                <div className="h-3 bg-gray-700 rounded w-2/3"></div>
              </div>
            ))}
          </div>
        )}

        {/* Movies Grid */}
        {!loading && movies.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-16">
            {movies.map((movie) => (
              <MovieCard key={movie.id} movie={movie} />
            ))}
          </div>
        )}

        {/* Empty state */}
        {!loading && movies.length === 0 && (
          <div className="text-center py-12 mb-16">
            <p className="text-slate-400 text-xl">No upcoming movies at this time</p>
          </div>
        )}

        {/* Notification CTA */}
        <div className="bg-gradient-to-r from-purple-900/30 to-pink-900/30 border border-purple-500/30 rounded-2xl p-8 md:p-12">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
            <div className="space-y-4">
              <div className="flex items-center gap-3">
                <Bell className="text-pink-400" size={28} />
                <h3 className="text-2xl md:text-3xl font-bold text-white">Never Miss a Release</h3>
              </div>
              <p className="text-slate-300 text-lg">
                Subscribe to get notified about new releases, special screenings, and exclusive member events.
              </p>
            </div>

            <form onSubmit={handleNotify} className="flex flex-col gap-3">
              <input
                type="email"
                placeholder="Enter your email"
                value={notifyEmail}
                onChange={(e) => setNotifyEmail(e.target.value)}
                className="px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 transition-colors"
                required
              />
              <button
                type="submit"
                className="px-6 py-3 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg hover:shadow-purple-500/50 transition-all"
              >
                {notified ? "Subscribed!" : "Notify Me"}
              </button>
            </form>
          </div>
        </div>
      </div>
    </section>
  )
}