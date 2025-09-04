// Core entity types
export interface Hospital {
  id: string;
  name: string;
  address: string;
  phone: string;
  email: string;
  businessHours: BusinessHour[];
  treatmentProducts: TreatmentProduct[];
}

export interface Doctor {
  id: string;
  name: string;
  specialization: string;
  hospitalId: string;
}

export interface TreatmentProduct {
  id: string;
  name: string;
  description: string;
  price: number;
  duration: number;
  hospitalId: string;
}

export interface BusinessHour {
  id: string;
  hospitalId: string;
  dayOfWeek: string; // "MONDAY", "TUESDAY", etc.
  openTime: string; // "09:00"
  closeTime: string; // "18:00"
  breakStartTime: string; // "12:00"
  breakEndTime: string; // "13:00"
  isClosed: boolean;
}

export type BusinessHours = BusinessHour[];

// Reservation types
export interface Reservation {
  id: string;
  userId: string;
  userName: string;
  hospitalId: string;
  treatmentProduct: TreatmentProduct;
  treatmentProductId: string;
  treatmentProductName: string;
  doctorId?: string;
  doctorName?: string;
  name: string;
  scheduledDate: string;
  startTime: string;
  endTime: string;
  status: string;
  userMemo?: string;
  createdAt: string;
}

// User event types
export interface UserEvent {
  id: string;
  hospitalName: string;
  treatmentProductName: string;
  startTime: string;
  endTime: string;
  userMemo?: string;
  status: string;
}

// Service slot types
export interface ServiceSlot {
  id: string;
  time: string;
  isAvailable: boolean;
  reservationStatus?: string;
}

// Response types
export interface AdminUpdatePasswordResponse {
  success: boolean;
  message: string;
}
