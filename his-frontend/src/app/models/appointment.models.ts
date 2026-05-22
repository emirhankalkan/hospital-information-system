export type AppointmentStatus = 'SCHEDULED' | 'COMPLETED' | 'CANCELED';

export interface Appointment {
  id: number;
  patientId: number;
  patientFirstName: string;
  patientLastName: string;
  patientTcNo?: string | null;
  doctorId: number;
  doctorFirstName: string;
  doctorLastName: string;
  doctorSpecialization?: string | null;
  departmentName?: string | null;
  appointmentDate: string;
  appointmentTime: string;
  status: AppointmentStatus;
  notes?: string | null;
  createdByUserId?: number | null;
  createdByUsername?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface AppointmentRequest {
  patientId: number;
  doctorId: number;
  appointmentDate: string;   // yyyy-MM-dd
  appointmentTime: string;   // HH:mm
  notes?: string | null;
}

export interface AppointmentStatusUpdateRequest {
  status: AppointmentStatus;
  notes?: string | null;
}
