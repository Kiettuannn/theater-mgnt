import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { SearchAddBar } from "@/components/ui/SearchAddBar";
import { PageHeader } from "@/components/ui/PageHeader";
import { ConfirmDialog } from "@/components/ui/ConfirmDialog";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner";
import { CustomerTable, CustomerFormDialog } from "@/components/customers";
import { useCustomerManager } from "@/hooks/useCustomerManager";
import type { CustomerProfile } from "@/types/CustomerType/CustomerProfile";
import type { CustomerRequest } from "@/types/CustomerType/CustomerProfile";
import { Search, X, Users } from "lucide-react";

export const CustomerList = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerProfile | null>(null);
  const [filteredCustomers, setFilteredCustomers] = useState<CustomerProfile[]>([]);

  const {
    customers,
    loading,
    saving,
    confirmDialog,
    loadData,
    handleCreateCustomer,
    handleUpdateCustomer,
    handleDeleteCustomer,
    closeConfirmDialog,
  } = useCustomerManager();

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Filter customers based on search query (client-side filtering)
  useEffect(() => {
    // Safety check: ensure customers is an array
    if (!Array.isArray(customers)) {
      setFilteredCustomers([]);
      return;
    }

    if (!searchQuery.trim()) {
      setFilteredCustomers(customers);
    } else {
      const filtered = customers.filter((customer) => {
        try {
          // Safety check: ensure customer object exists
          if (!customer) return false;

          const firstName = (customer.firstName || "").toLowerCase();
          const lastName = (customer.lastName || "").toLowerCase();
          const email = (customer.email || "").toLowerCase();
          const phoneNumber = (customer.phoneNumber || "").toLowerCase();
          const fullName = `${firstName} ${lastName}`.trim();
          const query = searchQuery.toLowerCase();

          return (
            firstName.includes(query) ||
            lastName.includes(query) ||
            fullName.includes(query) ||
            email.includes(query) ||
            phoneNumber.includes(query)
          );
        } catch (error) {
          console.error("Error filtering customer:", customer, error);
          return false;
        }
      });
      setFilteredCustomers(filtered);
    }
  }, [customers, searchQuery]);

  const handleOpenCreateDialog = () => {
    setSelectedCustomer(null);
    setDialogOpen(true);
  };

  const handleOpenEditDialog = (customer: CustomerProfile) => {
    setSelectedCustomer(customer);
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedCustomer(null);
  };

  const handleSubmit = async (request: CustomerRequest) => {
    if (selectedCustomer) {
      // When editing, only send update fields (without username/password)
      const updateRequest = {
        firstName: request.firstName,
        lastName: request.lastName,
        phoneNumber: request.phoneNumber,
        address: request.address,
        gender: request.gender,
        dob: request.dob,
      };
      return await handleUpdateCustomer(selectedCustomer.customerId, updateRequest);
    } else {
      return await handleCreateCustomer(request);
    }
  };

  const handleDelete = (customer: CustomerProfile) => {
    handleDeleteCustomer(customer.customerId, `${customer.firstName} ${customer.lastName}`);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Customer Management"
        description="Manage your cinema customers"
      />

      {/* Search and Actions Bar */}
      <SearchAddBar
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        placeholder="Search by name, email, or phone number..."
        totalCount={customers.length}
        filteredCount={filteredCustomers.length}
        icon={<Users className="w-4 h-4" />}
        label="customers"
        buttonText="Add Customer"
        onAddClick={handleOpenCreateDialog}
      />

      {/* Customer Table */}
      {loading ? (
        <div className="bg-white rounded-lg border border-gray-200 p-12">
          <LoadingSpinner
            message="Loading customers..."
            size="lg"
            fullScreen={false}
          />
        </div>
      ) : searchQuery.trim() && filteredCustomers.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <Search className="w-12 h-12 text-muted-foreground mb-4" />
          <h3 className="text-lg font-medium text-foreground mb-2">
            No customers found
          </h3>
          <p className="text-muted-foreground mb-4">
            No customers match your search for "{searchQuery}"
          </p>
          <Button
            variant="outline"
            onClick={() => setSearchQuery("")}
            className="gap-2"
          >
            <X className="w-4 h-4" />
            Clear search
          </Button>
        </div>
      ) : (
        <CustomerTable
          customers={filteredCustomers}
          isLoading={loading}
          onEdit={handleOpenEditDialog}
          onDelete={handleDelete}
          updatingCell={saving ? "updating" : undefined}
        />
      )}

      {/* Customer Form Dialog */}
      <CustomerFormDialog
        isOpen={dialogOpen}
        onClose={handleCloseDialog}
        onSubmit={handleSubmit}
        customer={selectedCustomer}
        saving={saving}
      />

      {/* Confirmation Dialog */}
      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        onClose={closeConfirmDialog}
        onConfirm={confirmDialog.onConfirm}
        title={confirmDialog.title}
        description={confirmDialog.description}
        confirmText={confirmDialog.confirmText || "Confirm"}
        cancelText="Cancel"
        variant={confirmDialog.variant || "destructive"}
      />
    </div>
  );
};
