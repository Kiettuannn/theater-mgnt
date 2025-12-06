import { PageHeader } from "@/components/ui/PageHeader";
import { Card } from "@/components/ui/card";
import { Theater } from "lucide-react";

export function TheaterList() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="Cinemas Management"
        description="Manage Cinemas and screening rooms"
      />

      <Card className="p-8">
        <div className="flex flex-col items-center justify-center space-y-4 text-center">
          <div className="rounded-full bg-purple-100 p-6">
            <Theater className="h-12 w-12 text-purple-600" />
          </div>
          <h3 className="text-2xl font-semibold">Cinemas Management</h3>
          <p className="text-muted-foreground max-w-md">
            This is the Cinemas management page. Here you can view, create,
            edit, and delete Cinemas and screening rooms.
          </p>
        </div>
      </Card>
    </div>
  );
}
