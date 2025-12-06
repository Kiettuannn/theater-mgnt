import { useState, useCallback } from "react";

export interface ConfirmDialogState {
  isOpen: boolean;
  title: string;
  description: string;
  onConfirm: () => void;
  variant?: "default" | "destructive";
  confirmText?: string;
}

export function useConfirmDialog() {
  const [confirmDialog, setConfirmDialog] = useState<ConfirmDialogState>({
    isOpen: false,
    title: "",
    description: "",
    onConfirm: () => {},
    variant: "destructive",
    confirmText: "Confirm",
  });

  const showConfirmDialog = useCallback((config: Omit<ConfirmDialogState, 'isOpen'>) => {
    setConfirmDialog({
      ...config,
      isOpen: true,
    });
  }, []);

  const closeConfirmDialog = useCallback(() => {
    setConfirmDialog((prev) => ({ ...prev, isOpen: false }));
  }, []);

  const confirmAndClose = useCallback((callback: () => void) => {
    return () => {
      setConfirmDialog((prev) => ({ ...prev, isOpen: false }));
      callback();
    };
  }, []);

  return {
    confirmDialog,
    showConfirmDialog,
    closeConfirmDialog,
    confirmAndClose,
  };
}