import { UserRole } from './auth.models';

export interface PatientProfile {
  id: number;
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  tcNo?: string | null;
  birthDate?: string | null;
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  emergencyContact?: string | null;
  bloodType?: string | null;
  isDeleted: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PatientPanelUser {
  id: number;
  username: string;
  email: string;
  roles: UserRole[];
}
