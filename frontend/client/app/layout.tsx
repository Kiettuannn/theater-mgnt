import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { ThemeProvider } from "@/components/theme-provider";
import { Header } from "@/components/header";
import { Footer } from "@/components/footer";
import { RouteGuard } from "@/components/route-guard";
import { ProtectedRouteNotification } from "@/components/protected-route-notification";
import { StoreInitializer } from "@/components/store-initializer";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000'),
  title: {
    default: "Theater Management - Đặt vé xem phim trực tuyến",
    template: "%s | Theater Management",
  },
  description: "Hệ thống đặt vé xem phim trực tuyến hàng đầu. Xem phim mới nhất, đặt vé nhanh chóng, ghế ngồi thoải mái, giá cả hợp lý.",
  keywords: ["đặt vé xem phim", "rạp chiếu phim", "phim mới", "book vé online", "cinema", "movie theater", "đặt vé rạp"],
  authors: [{ name: "Theater Management" }],
  creator: "Theater Management",
  publisher: "Theater Management",
  openGraph: {
    title: "Theater Management - Đặt vé xem phim trực tuyến",
    description: "Đặt vé xem phim nhanh chóng, tiện lợi. Phim mới nhất, suất chiếu đa dạng.",
    url: process.env.NEXT_PUBLIC_SITE_URL || 'http://localhost:3000',
    siteName: "Theater Management",
    images: [
      {
        url: "/og-image.jpg",
        width: 1200,
        height: 630,
        alt: "Theater Management - Đặt vé xem phim trực tuyến"
      }
    ],
    locale: "vi_VN",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "Theater Management - Đặt vé xem phim",
    description: "Đặt vé xem phim trực tuyến nhanh chóng, tiện lợi",
    images: ["/twitter-image.jpg"],
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
  verification: {
    google: "s2dNoZ8IsNWtzSX9hSo3WHsWO-T4sHHCTvQca_K-Cj8",
  },
};

export const viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 5,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" suppressHydrationWarning>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
        suppressHydrationWarning
      >
        <ThemeProvider attribute="class" defaultTheme="dark" enableSystem>
          <StoreInitializer>
            <RouteGuard>
              <Header />
              <ProtectedRouteNotification />
              {children}
              <Footer />
            </RouteGuard>
          </StoreInitializer>
        </ThemeProvider>
      </body>
    </html>
  );
}
