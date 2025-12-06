import {
  Film,
  Calendar,
  Theater,
  Ticket,
  LayoutDashboard,
  Users,
  UserCog,
  Briefcase,
  TicketCheck,
  ShoppingBag,
  Receipt,
  BarChart3,
  DoorOpen,
} from "lucide-react";
import { ROUTES } from "@/constants/routes";
import { PERMISSIONS } from "@/constants/permissions";
import type { PermissionType } from "@/constants/permissions";
import type { LucideIcon } from "lucide-react";

export interface MenuItem {
  id: string;
  label: string;
  icon: LucideIcon;
  path: string;
  requiredPermissions?: PermissionType[];
}

export const MENU_ITEMS: MenuItem[] = [
  {
    id: "overview",
    label: "Dashboard",
    icon: LayoutDashboard,
    path: ROUTES.DASHBOARD,
  },
  {
    id: "movies",
    label: "Movies",
    icon: Film,
    path: ROUTES.MOVIES,
    requiredPermissions: [PERMISSIONS.MOVIE_READ],
  },
  {
    id: "cinemas",
    label: "Cinemas",
    icon: Theater,
    path: ROUTES.CINEMAS,
    requiredPermissions: [PERMISSIONS.CINEMAS_READ],
  },
  {
    id: "rooms",
    label: "Rooms",
    icon: DoorOpen,
    path: ROUTES.ROOMS,
    requiredPermissions: [PERMISSIONS.ROOM_READ],
  },
  {
    id: "customers",
    label: "Customers",
    icon: Users,
    path: ROUTES.CUSTOMERS,
    requiredPermissions: [PERMISSIONS.CUSTOMER_READ],
  },
  {
    id: "staff",
    label: "Staff",
    icon: UserCog,
    path: ROUTES.STAFF,
    requiredPermissions: [PERMISSIONS.STAFF_READ],
  },
  {
    id: "showtimes",
    label: "Showtimes",
    icon: Calendar,
    path: ROUTES.SHOWTIMES,
    requiredPermissions: [PERMISSIONS.SHOWTIME_READ],
  },
  {
    id: "bookings",
    label: "Bookings",
    icon: Briefcase,
    path: ROUTES.BOOKINGS,
    requiredPermissions: [PERMISSIONS.BOOKING_READ],
  },
  {
    id: "ticket-booking",
    label: "Ticket Booking",
    icon: TicketCheck,
    path: ROUTES.TICKET_BOOKING,
    requiredPermissions: [PERMISSIONS.TICKET_BOOKING_READ],
  },
  {
    id: "tickets",
    label: "Tickets",
    icon: Ticket,
    path: ROUTES.TICKETS,
    requiredPermissions: [PERMISSIONS.TICKET_READ],
  },
  {
    id: "combos",
    label: "Combos",
    icon: ShoppingBag,
    path: ROUTES.COMBOS,
    requiredPermissions: [PERMISSIONS.COMBO_READ],
  },
  {
    id: "invoices",
    label: "Invoices",
    icon: Receipt,
    path: ROUTES.INVOICES,
    requiredPermissions: [PERMISSIONS.INVOICE_READ],
  },
  {
    id: "reports",
    label: "Reports",
    icon: BarChart3,
    path: ROUTES.REPORTS,
    requiredPermissions: [PERMISSIONS.REPORT_READ],
  },
];
