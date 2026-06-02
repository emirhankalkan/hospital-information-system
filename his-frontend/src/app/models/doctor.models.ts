export interface Doctor {
  id: number;
  userId: number;
  username: string;
  email: string;
  departmentId: number;
  departmentName: string;
  firstName: string;
  lastName: string;
  specialization?: string | null;
  phone?: string | null;
  createdAt?: string;
  updatedAt?: string;
}
