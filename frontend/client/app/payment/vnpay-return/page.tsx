'use client';

import { useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { CheckCircle, XCircle, Loader } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cancelBooking } from '@/lib/api-movie';

interface PaymentResult {
  code: string;
  message: string;
  txnRef?: string;
  amount?: number;
  orderInfo?: string;
  bookingId?: string;
}

export default function VNPayReturnPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<PaymentResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const clearBookingSession = () => {
    sessionStorage.removeItem('current_booking_id');
    sessionStorage.removeItem('current_booking_movie_id');
    sessionStorage.removeItem('current_booking_showtime_id');
    Object.keys(sessionStorage).forEach(key => {
      if (key.startsWith('booking_') || key.startsWith('booking_reload_')) {
        sessionStorage.removeItem(key);
      }
    });
  };

  const findBookingRoute = () => {
    const bookingKey = Object.keys(sessionStorage).find(key => key.startsWith('booking_') && !key.startsWith('booking_reload_'));
    if (!bookingKey) return null;
    const parts = bookingKey.split('_');
    if (parts.length < 3) return null;
    const movieId = parts[1];
    const showtimeId = parts.slice(2).join('_');
    return { movieId, showtimeId };
  };

  const findRouteFromFallback = () => {
    const movieId = sessionStorage.getItem('current_booking_movie_id');
    const showtimeId = sessionStorage.getItem('current_booking_showtime_id');
    if (movieId && showtimeId) return { movieId, showtimeId };
    return null;
  };

  const cancelBookingIfPossible = async (bookingId?: string) => {
    if (!bookingId) {
      console.log('No bookingId to cancel');
      return;
    }
    try {
      console.log('Attempting to cancel booking:', bookingId);
      await cancelBooking(bookingId);
      console.log('Booking cancelled successfully:', bookingId);
    } catch (cancelErr) {
      console.error('Error cancelling booking:', bookingId, cancelErr);
    }
  };

  useEffect(() => {
    const handlePaymentCallback = async () => {
      try {
        // Get all params from URL
        const params = new URLSearchParams(searchParams);
        const responseCode = params.get('vnp_ResponseCode');
        const txnRef = params.get('vnp_TxnRef');
        const amount = params.get('vnp_Amount');
        const orderInfo = params.get('vnp_OrderInfo');

        // Log for debugging
        console.log('Payment Callback Params:', {
          responseCode,
          txnRef,
          amount,
          orderInfo,
        });

        // Call backend to verify and process payment
        const queryString = params.toString();
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/payment/vnpay-return?${queryString}`,
          {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        if (!response.ok) {
          throw new Error(`Backend error: ${response.status}`);
        }

        const data = await response.json();
        
        // Clean URL to remove sensitive payment params from browser history
        window.history.replaceState({}, document.title, window.location.pathname);
        
        // Try to get bookingId from sessionStorage (saved during booking process)
        if (!data.bookingId) {
          const savedBookingId = sessionStorage.getItem('current_booking_id');
          if (savedBookingId) {
            data.bookingId = savedBookingId;
            console.log('Retrieved bookingId from sessionStorage:', savedBookingId);
          }
        }
        
        // Alternative: Use txnRef as fallback if still no bookingId
        if (!data.bookingId && txnRef) {
          data.bookingId = txnRef;
          console.log('Using txnRef as bookingId fallback:', txnRef);
        }
        
        setResult(data);

        // Log result
        console.log('Payment Result:', data);
        
        // Keep booking data in session so user can retry or view ticket
      } catch (err) {
        console.error('Payment verification failed:', err);
        setError(
          err instanceof Error ? err.message : 'Có lỗi xảy ra khi xác thực thanh toán'
        );
        
        // Keep booking data so user can retry
      } finally {
        setLoading(false);
      }
    };

    if (searchParams.size > 0) {
      handlePaymentCallback();
    }
  }, [searchParams]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background text-foreground flex items-center justify-center px-4 py-12">
        <Card className="w-full max-w-md text-center shadow-lg">
          <CardContent className="p-8">
          <Loader className="w-12 h-12 text-blue-600 mx-auto mb-4 animate-spin" />
          <h2 className="text-xl font-semibold text-gray-800 mb-2">
            Verifying payment...
          </h2>
          <p className="text-gray-600">
            Please wait a moment
          </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-background text-foreground flex items-center justify-center px-4 py-12">
        <Card className="w-full max-w-md text-center shadow-lg">
          <CardContent className="p-8">
          <XCircle className="w-16 h-16 text-red-600 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-red-600 mb-2">
            Verification Error
          </h2>
          <p className="text-gray-600 mb-6">
            {error}
          </p>
            <Button
              onClick={async () => {
                await cancelBookingIfPossible(result?.bookingId ?? result?.txnRef);
                clearBookingSession();
                const route = findBookingRoute() || findRouteFromFallback();
                if (route) {
                  router.push(`/booking/${route.movieId}/${route.showtimeId}`);
                } else {
                  router.push('/booking');
                }
              }}
              className="w-full bg-red-600 hover:bg-red-700"
            >
              Back to Booking
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const isSuccess = result?.code === '00';

  return (
    <div className="min-h-screen bg-background text-foreground flex items-center justify-center px-4 py-12">
      <Card className="w-full max-w-md text-center shadow-lg">
        <CardHeader className="pb-4">
          <CardTitle className="flex items-center justify-center gap-2 text-lg text-gray-700">
            <Badge variant={isSuccess ? 'default' : 'destructive'} className="uppercase">
              {isSuccess ? 'Success' : 'Failed'}
            </Badge>
            <span className="text-sm text-gray-500">VNPay</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0 pb-6">
        {isSuccess ? (
          <>
            <CheckCircle className="w-16 h-16 text-green-600 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-green-600 mb-2">
              Payment Successful!
            </h2>
            <p className="text-gray-600 mb-6">
              {result?.message}
            </p>

            <div className="bg-gray-50 rounded-lg p-4 mb-6 text-left space-y-3">
              {result?.txnRef && (
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">Transaction ID</p>
                  <p className="text-base font-semibold text-gray-800 break-all">
                    {result.txnRef}
                  </p>
                </div>
              )}
              {result?.amount && (
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">Amount</p>
                  <p className="text-lg font-semibold text-gray-900">
                    {result.amount.toLocaleString('vi-VN')} ₫
                  </p>
                </div>
              )}
              {result?.orderInfo && (
                <div>
                  <p className="text-xs uppercase tracking-wide text-gray-500">Order Info</p>
                  <p className="text-base font-semibold text-gray-800">
                    {result.orderInfo}
                  </p>
                </div>
              )}
            </div>

            <div className="space-y-3">
              <Button
                onClick={() => {
                  if (result?.bookingId) {
                    console.log('Navigating to success page with bookingId:', result.bookingId);
                    router.push(`/booking/success/${result.bookingId}`);
                  } else {
                    console.warn('No bookingId found, redirecting to my-tickets');
                    router.push('/my-tickets');
                  }
                  clearBookingSession();
                }}
                className="w-full bg-green-600 hover:bg-green-700"
              >
                View My Tickets
              </Button>
              <Button
                variant="secondary"
                onClick={() => {
                  clearBookingSession();
                  router.push('/');
                }}
                className="w-full"
              >
                Back to Home
              </Button>
            </div>
          </>
        ) : (
          <>
            <XCircle className="w-16 h-16 text-red-600 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-red-600 mb-2">
              Payment Failed
            </h2>
            <p className="text-gray-600 mb-6">
              {result?.message || 'Transaction was not successful'}
            </p>

            {result?.txnRef && (
              <div className="bg-gray-50 rounded-lg p-4 mb-6 text-left">
                <p className="text-xs uppercase tracking-wide text-gray-500">Transaction ID</p>
                <p className="text-base font-semibold text-gray-800 break-all">
                  {result.txnRef}
                </p>
              </div>
            )}

            <div className="space-y-3">
              <Button
                onClick={async () => {
                  await cancelBookingIfPossible(result?.bookingId ?? result?.txnRef);
                  clearBookingSession();
                  const route = findBookingRoute() || findRouteFromFallback();
                  if (route) {
                    router.push(`/booking/${route.movieId}/${route.showtimeId}`);
                  } else {
                    router.push('/booking');
                  }
                }}
                className="w-full bg-orange-600 hover:bg-orange-700 text-white"
              >
                Try Again
              </Button>
              <Button
                variant="secondary"
                onClick={async () => {
                  await cancelBookingIfPossible(result?.bookingId ?? result?.txnRef);
                  clearBookingSession();
                  router.push('/');
                }}
                className="w-full"
              >
                Back to Home
              </Button>
            </div>
          </>
        )}
        </CardContent>
      </Card>
    </div>
  );
}
