"use client";

import type { Seat } from "@/lib/types";

interface SeatSelectionStepProps {
  seats: Seat[];
  selectedSeats: Seat[];
  onSelectSeats: (seats: Seat[]) => void;
  seatPrice: number;
}

export default function SeatSelectionStep({
  seats,
  selectedSeats,
  onSelectSeats,
  seatPrice,
}: SeatSelectionStepProps) {
  const handleSeatClick = (seat: Seat) => {
    if (!seat.isAvailable) return;

    const isSelected = selectedSeats.some((s) => s.id === seat.id);
    if (isSelected) {
      onSelectSeats(selectedSeats.filter((s) => s.id !== seat.id));
    } else {
      onSelectSeats([...selectedSeats, seat]);
    }
  };

  const getSeatColor = (seat: Seat, isSelected: boolean) => {
    // Booked seat (not available)
    if (!seat.isAvailable) {
      return "bg-red-600 dark:bg-red-600 cursor-not-allowed opacity-85";
    }

    // Selected seat
    if (isSelected) {
      return "bg-purple-600 dark:bg-purple-500 text-white";
    }

    // Available seat based on type
    switch (seat.type) {
      case "vip":
        return "bg-amber-500 dark:bg-amber-400 hover:bg-amber-600 dark:hover:bg-amber-500 text-slate-900 font-bold";
      case "couple":
        return "bg-pink-500 dark:bg-pink-400 hover:bg-pink-600 dark:hover:bg-pink-500 text-white font-bold";
      default: // standard
        return "bg-slate-200 dark:bg-slate-300 hover:bg-slate-300 dark:hover:bg-slate-200 text-slate-900 font-bold";
    }
  };

  const isCoupleLeftSeat = (seat: Seat) => {
    // Check if this is a couple seat and if it's the left seat of a pair
    const coupleColumns = [5, 6, 7, 8];
    return seat.type === "couple" && coupleColumns.includes(seat.number);
  };

  const getCoupleDisplayText = (seat: Seat) => {
    // For couple seats, show both seat numbers if it's the left seat
    if (seat.type === "couple") {
      const currentIndex = seats
        .filter((s) => s.row === seat.row)
        .findIndex((s) => s.number === seat.number);
      const nextSeat = seats.find(
        (s) => s.row === seat.row && s.number === seat.number + 1,
      );
      if (nextSeat && nextSeat.type === "couple") {
        return `${seat.number}-${seat.number + 1}`;
      }
    }
    return seat.number;
  };

  const getSeatTypeLabel = (type?: string) => {
    switch (type) {
      case "vip":
        return "VIP";
      case "couple":
        return "Couple";
      default:
        return "Standard";
    }
  };

  const getSeatLabel = (seat: Seat) => `${seat.row}${seat.number}`;

  const rows = Array.from(new Set(seats.map((s) => s.row))).sort((a, b) =>
    a.localeCompare(b, undefined, { numeric: true, sensitivity: "base" }),
  );

  return (
    <div className="bg-card dark:bg-slate-900 border border-border dark:border-slate-800 rounded-xl p-8">
      <h2 className="text-2xl font-bold mb-8">Select Your Seats</h2>

      {/* Screen */}
      <div className="mb-12 text-center">
        <div className="inline-block w-full max-w-2xl h-2 bg-gradient-to-r from-purple-600 to-pink-600 rounded-full mb-8 relative">
          <p className="absolute -bottom-8 left-1/2 transform -translate-x-1/2 text-sm font-semibold text-muted-foreground">
            SCREEN
          </p>
        </div>
      </div>

      {/* Seats Grid */}
      <div className="space-y-4 mb-12">
        {rows.map((row) => (
          <div key={row} className="flex items-center justify-center gap-2">
            <span className="w-8 font-bold text-muted-foreground">{row}</span>
            <div className="flex gap-2 flex-wrap justify-center">
              {seats
                .filter((s) => s.row === row)
                .sort((a, b) => a.number - b.number)
                .map((seat, index, rowSeats) => {
                  const isSelected = selectedSeats.some(
                    (s) => s.id === seat.id,
                  );
                  const colorClass = getSeatColor(seat, isSelected);
                  const isCouple = seat.type === "couple";
                  const seatLabel = getSeatLabel(seat);

                  return (
                    <button
                      key={seat.id}
                      onClick={() => {
                        handleSeatClick(seat);
                      }}
                      disabled={!seat.isAvailable}
                      className={`rounded transition-all font-semibold text-xs ${
                        isCouple
                          ? "w-18 h-8 flex items-center justify-center border-2 border-purple-200 dark:border-purple-300"
                          : "w-10 h-8"
                      } ${colorClass}`}
                      title={`Seat ${seatLabel} - ${getSeatTypeLabel(seat.type)}`}
                    >
                      {seatLabel}
                    </button>
                  );
                })}
            </div>
          </div>
        ))}
      </div>

      {/* Legend */}
      <div className="flex flex-wrap gap-6 justify-center pt-8 border-t border-border dark:border-slate-800">
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded bg-slate-200 dark:bg-slate-300" />
          <span className="text-sm text-muted-foreground">Standard</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-12 h-6 rounded bg-pink-500 dark:bg-pink-400 flex items-center justify-center border-2 border-pink-300 dark:border-pink-300 text-xs font-bold text-white">
            2x
          </div>
          <span className="text-sm text-muted-foreground">Couple</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded bg-amber-500 dark:bg-amber-400" />
          <span className="text-sm text-muted-foreground">VIP</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded bg-purple-600 dark:bg-purple-500" />
          <span className="text-sm text-muted-foreground">Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-6 h-6 rounded bg-red-500 dark:bg-red-600 opacity-85" />
          <span className="text-sm text-muted-foreground">Booked</span>
        </div>
      </div>

      {selectedSeats.length > 0 && (
        <div className="mt-8 p-4 bg-purple-500/10 dark:bg-purple-900/20 border border-purple-500/30 rounded-lg">
          <p className="text-sm">
            <span className="font-semibold">Selected Seats:</span>{" "}
            {selectedSeats
              .map(
                (s) =>
                  `${getSeatLabel(s)} (${getSeatTypeLabel(s.type)}${s.price ? ` - ${s.price.toLocaleString()} VND` : ""})`,
              )
              .join(", ")}
          </p>
          <p className="text-sm mt-2">
            <span className="font-semibold">Total:</span>{" "}
            {selectedSeats
              .reduce((sum, s) => sum + (s.price || seatPrice), 0)
              .toLocaleString()}{" "}
            VND
          </p>
        </div>
      )}
    </div>
  );
}
