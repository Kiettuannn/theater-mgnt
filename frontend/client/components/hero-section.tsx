"use client";

import { ChevronRight, Play } from "lucide-react";
import { useState, useEffect } from "react";
import Link from "next/link";
import { getNowShowingMovies, mapMovieForDisplay } from "@/lib/api-movie";

export function HeroSection() {
  const [featuredMovie, setFeaturedMovie] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFeaturedMovie = async () => {
      try {
        setLoading(true);
        const movies = await getNowShowingMovies();

        if (movies && Array.isArray(movies) && movies.length > 0) {
          const sortedMovies = [...movies].sort((a, b) => {
            const dateA = new Date(a.releaseDate).getTime();
            const dateB = new Date(b.releaseDate).getTime();
            return dateB - dateA;
          });

          const latest = mapMovieForDisplay(sortedMovies[0]);
          setFeaturedMovie(latest);
        }
      } catch (error) {
        console.error("Error fetching featured movie:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchFeaturedMovie();
  }, []);

  return (
    <section className="relative min-h-screen pt-16 overflow-hidden bg-background">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-br from-purple-900/20 via-background to-pink-900/20" />

      {/* Animated background elements */}
      <div className="absolute top-20 right-10 w-72 h-72 bg-purple-600/10 rounded-full blur-3xl animate-pulse" />
      <div className="absolute bottom-20 left-10 w-96 h-96 bg-pink-600/10 rounded-full blur-3xl animate-pulse" />

      <div className="relative container-max px-4 md:px-8 h-screen flex items-center">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center w-full">
          {/* Left Content */}
          <div className="space-y-8 z-10">
            <div className="space-y-4">
              <div className="inline-block px-4 py-2 rounded-full bg-purple-500/20 border border-purple-500/50">
                <span className="text-purple-600 dark:text-purple-300 text-sm font-semibold">
                  Welcome to Cifastar
                </span>
              </div>

              <h1 className="text-5xl md:text-6xl font-bold leading-tight">
                <span className="gradient-text">Experience Cinema</span>
                <br />
                <span className="text-foreground">Like Never Before</span>
              </h1>

              <p className="text-xl text-muted-foreground max-w-lg leading-relaxed">
                Immerse yourself in premium entertainment with cutting-edge
                technology, luxury seating, and unforgettable moments.
              </p>
            </div>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 pt-4">
              <Link
                href="#now-showing"
                className="px-8 py-4 rounded-lg gradient-primary text-white font-semibold hover:shadow-lg hover:shadow-purple-500/50 transition-all flex items-center justify-center gap-2 group"
              >
                Book Tickets
                <ChevronRight
                  size={20}
                  className="group-hover:translate-x-1 transition-transform"
                />
              </Link>
              {featuredMovie?.trailerUrl && (
                <a
                  href={featuredMovie.trailerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="px-8 py-4 rounded-lg border border-border text-foreground font-semibold hover:bg-muted transition-all flex items-center justify-center gap-2"
                >
                  <Play size={20} />
                  Watch Trailer
                </a>
              )}
            </div>

            {/* Stats */}
            <div className="flex gap-8 pt-8 border-t border-border">
              <div>
                <p className="text-3xl font-bold gradient-text">50K+</p>
                <p className="text-muted-foreground text-sm">Happy Customers</p>
              </div>
              <div>
                <p className="text-3xl font-bold gradient-text">100+</p>
                <p className="text-muted-foreground text-sm">Movies Yearly</p>
              </div>
              <div>
                <p className="text-3xl font-bold gradient-text">15+</p>
                <p className="text-muted-foreground text-sm">Premium Screens</p>
              </div>
            </div>
          </div>

          {/* Right Visual */}
          <div className="relative h-96 md:h-full hidden md:flex items-center justify-center">
            {loading ? (
              <div className="relative w-full max-w-sm animate-pulse">
                <div className="bg-gray-700 aspect-[2/3] rounded-2xl" />
              </div>
            ) : featuredMovie ? (
              <div className="relative w-full max-w-sm">
                {/* Movie poster card */}
                <div className="relative rounded-2xl overflow-hidden shadow-2xl aspect-[2/3]">
                  <img
                    src={
                      featuredMovie.poster || "/premium-cinema-movie-poster.jpg"
                    }
                    alt={featuredMovie.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-background via-transparent to-transparent" />

                  {/* Play button overlay */}
                  {featuredMovie.trailerUrl && (
                    <a
                      href={featuredMovie.trailerUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="absolute inset-0 flex items-center justify-center group"
                    >
                      <div className="w-20 h-20 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center group-hover:bg-white/30 transition-all">
                        <Play size={40} className="text-white fill-white" />
                      </div>
                    </a>
                  )}
                </div>

                {/* Floating cards */}
                <div className="absolute -bottom-6 -left-6 bg-card rounded-xl p-4 shadow-xl border border-border max-w-xs">
                  <p className="text-sm text-muted-foreground mb-2">
                    Now Showing
                  </p>
                  <p className="text-foreground font-semibold">
                    {featuredMovie.title}
                  </p>
                  <div className="flex gap-2 mt-2 flex-wrap">
                    {featuredMovie.genre?.slice(0, 2).map((g: string) => (
                      <span
                        key={g}
                        className="text-xs bg-purple-500/20 text-purple-600 dark:text-purple-300 px-2 py-1 rounded"
                      >
                        {g}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              <div className="relative w-full max-w-sm">
                {/* Fallback */}
                <div className="relative rounded-2xl overflow-hidden shadow-2xl">
                  <img
                    src="/premium-cinema-movie-poster.jpg"
                    alt="Featured Movie"
                    className="w-full h-auto object-cover"
                  />
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
