import { RoomCard, AddRoomButton } from "@/components/rooms";
import { useRoomManager } from "@/hooks/useRoomManager";
import type { Room } from "@/types/RoomType/room";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ROUTES } from "@/constants/routes";
import { PageHeader } from "@/components/ui/PageHeader";
import { SearchAddBar } from "@/components/ui/SearchAddBar";
import { Monitor } from "lucide-react";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";

export function RoomList() {
  const { rooms, loadData, loading } = useRoomManager();
  const [searchQuery, setSearchQuery] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, [loadData]);

  console.log("Rooms:", rooms);

  const handleEdit = (room: Room) => {
    navigate(ROUTES.ROOMS_EDIT.replace(":id", room.id.toString()));
  };

  const handleViewSchedule = (room: Room) => {
    console.log("View schedule:", room);
    // TODO: Implement view schedule functionality
  };

  const handleAddRoom = () => {
    navigate(ROUTES.ROOMS_CREATE);
  };

  const filteredRooms = rooms.filter((room) =>
    room.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (loading) {
    return <LoadingSpinner message="Loading rooms..." />;
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Rooms Management"
        description="Manage your theater rooms here."
      />
      {/* Search and Actions Bar */}
      <SearchAddBar
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        placeholder="Search by room name..."
        totalCount={rooms.length}
        filteredCount={filteredRooms.length}
        icon={<Monitor className="w-4 h-4" />}
        label="rooms"
        buttonText="Add Room"
        onAddClick={handleAddRoom}
      />
      {/* Rooms Grid */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {filteredRooms.map((room) => (
          <RoomCard
            key={room.id}
            room={room}
            onEdit={handleEdit}
            onViewSchedule={handleViewSchedule}
          />
        ))}
      </div>
    </div>
  );
}
