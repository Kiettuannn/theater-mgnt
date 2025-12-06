export function DashboardOverview() {
  return (
    <div className="space-y-6">
      <div className="border-b border-border pb-4">
        <h1 className="text-2xl font-bold text-foreground">
          Tổng quan hệ thống
        </h1>
        <p className="text-muted-foreground">
          Thống kê tổng quan về hoạt động rạp phim
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-card rounded-lg border border-border p-6">
          <h3 className="text-sm font-medium text-muted-foreground">
            Tổng số phim
          </h3>
          <p className="text-2xl font-bold text-foreground">24</p>
        </div>
        <div className="bg-card rounded-lg border border-border p-6">
          <h3 className="text-sm font-medium text-muted-foreground">Rooms</h3>
          <p className="text-2xl font-bold text-foreground">8</p>
        </div>
        <div className="bg-card rounded-lg border border-border p-6">
          <h3 className="text-sm font-medium text-muted-foreground">
            Vé đã bán hôm nay
          </h3>
          <p className="text-2xl font-bold text-foreground">156</p>
        </div>
        <div className="bg-card rounded-lg border border-border p-6">
          <h3 className="text-sm font-medium text-muted-foreground">
            Doanh thu hôm nay
          </h3>
          <p className="text-2xl font-bold text-foreground">12.5M VNĐ</p>
        </div>
      </div>
    </div>
  );
}
