import apiClient from './client';

export interface DashboardStatsResponse {
  today: {
    total: number;
    pending: number;
    confirmed: number;
    canceled: number;
    completed: number;
    revenue: number;
  };
  thisMonth: {
    totalBookings: number;
    completedTreatments: number;
    revenue: number;
    newCustomers: number;
    returningCustomers: number;
  };
  performance: {
    confirmationRate: number;
    cancellationRate: number;
    avgDailyBookings: number;
    noShowRate: number;
  };
}

export interface DashboardTrendsResponse {
  dailyTrends: Array<{
    date: string;
    totalReservations: number;
    pendingReservations: number;
    confirmedReservations: number;
    canceledReservations: number;
    completedReservations: number;
    revenue: number;
  }>;
  timeSlotDistribution: Array<{
    time: string;
    count: number;
    revenue: number;
  }>;
  treatmentPopularity: Array<{
    treatmentName: string;
    count: number;
    revenue: number;
    percentage: number;
  }>;
}

export const getDashboardStats = async (hospitalId: string): Promise<DashboardStatsResponse> => {
  const response = await apiClient.get(`/dashboard/stats/${hospitalId}`);
  return response.data.body;
};

export const getDashboardTrends = async (hospitalId: string, days = 7): Promise<DashboardTrendsResponse> => {
  const response = await apiClient.get(`/dashboard/trends/${hospitalId}?days=${days}`);
  return response.data.body;
};

export const getRealTimeSummary = async (hospitalId: string) => {
  const response = await apiClient.get(`/dashboard/summary/${hospitalId}`);
  return response.data.body;
};

export interface HeatmapData {
  dayOfWeek: number; // 0=일요일, 1=월요일, ... 6=토요일
  hour: number; // 0-23
  count: number;
}

export const getHeatmapData = async (hospitalId: string, days = 30): Promise<HeatmapData[]> => {
  const response = await apiClient.get(`/dashboard/heatmap/${hospitalId}?days=${days}`);
  return response.data.body;
};

export interface DoctorStatsResponse {
  doctorId: string;
  doctorName: string;
  totalReservations: number;
  confirmedReservations: number;
  pendingReservations: number;
  canceledReservations: number;
  completedReservations: number;
  revenue: number;
  confirmationRate: number;
}

export const getDoctorStats = async (hospitalId: string, days = 7): Promise<DoctorStatsResponse[]> => {
  const response = await apiClient.get(`/dashboard/doctor-stats/${hospitalId}?days=${days}`);
  return response.data.body;
};

export interface DoctorTreatmentStatsResponse {
  doctorId: string;
  doctorName: string;
  treatmentStats: Array<{
    treatmentName: string;
    reservationCount: number;
    revenue: number;
  }>;
}

export const getDoctorTreatmentStats = async (
  hospitalId: string,
  days = 7,
): Promise<DoctorTreatmentStatsResponse[]> => {
  const response = await apiClient.get(`/dashboard/doctor-treatment-stats/${hospitalId}?days=${days}`);
  return response.data.body;
};
