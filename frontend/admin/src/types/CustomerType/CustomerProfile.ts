export interface CustomerProfile {
  accountId: string;
  accountType: string;
  username: string;
  email: string;
  phoneNumber: string;
  firstName: string;
  lastName: string;
  address: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  dob: string;
  customerId: string;
  avatarUrl?: string | null;
  noPassword?: boolean;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  gender: "MALE" | "FEMALE" | "OTHER";
  dob: string;
  username: string;
  password: string;
}

export interface CustomerUpdateRequest {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  address?: string;
  avatarUrl?: string;
  gender?: "MALE" | "FEMALE" | "OTHER";
  dob?: string;
}
