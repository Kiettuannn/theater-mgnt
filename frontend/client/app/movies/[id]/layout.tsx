import type { Metadata } from 'next'
import { generateMovieMetadata } from './metadata-generator'

export async function generateMetadata({
  params
}: {
  params: Promise<{ id: string }>
}): Promise<Metadata> {
  const { id } = await params
  return generateMovieMetadata(id)
}

export default function MovieLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return <>{children}</>
}
