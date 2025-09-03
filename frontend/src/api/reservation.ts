import apiClient from './client';

interface ReservationData {
  hospitalId: string;
  productId: string;
  startTime: string;
  endTime: string;
  memo?: string;
}

export const createReservation = async (data: ReservationData) => {
  const response = await apiClient.post('/medical-records', data);
  return response.data;
};

export const followUpReservation = async (data: ReservationData) => {
  const response = await apiClient.post('/medical-records/follow-up', data);
  return response.data;
};

export const getReservations = async (hospitalId: string, startTime: string, endTime: string) => {
  const response = await apiClient.get(`/medical-records/hospitals/${hospitalId}`, {
    params: { startTime, endTime },
  });
  return response.data;
};

export const confirmReservation = async (recordId: string, userId: string, hospitalId: string, adminId: string) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/status/confirm`, {
    userId: userId,
    hospitalId: hospitalId,
    adminId: adminId,
  });
  return response.data;
};

export const pendingReservation = async (recordId: string, userId: string, hospitalId: string, adminId: string) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/status/pending`, {
    userId: userId,
    hospitalId: hospitalId,
    adminId: adminId,
  });
  return response.data;
};

export const cancelReservation = async (recordId: string, userId: string, hospitalId: string, adminId: string) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/status/cancel`, {
    userId: userId,
    hospitalId: hospitalId,
    adminId: adminId,
  });
  return response.data;
};

export const completeReservation = async (recordId: string, userId: string, hospitalId: string, adminId: string) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/status/complete`, {
    userId: userId,
    hospitalId: hospitalId,
    adminId: adminId,
  });
  return response.data;
};

export const updateReservationStatus = async (recordId: string, status: string) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/status`, { status });
  return response.data;
};

export const getReservationById = async (recordId: string) => {
  const response = await apiClient.get(`/medical-records/${recordId}`);
  return response.data;
};

export const getReservationEvents = async (recordId: string) => {
  const response = await apiClient.get(`/medical-records/${recordId}/events`);
  return response.data;
};

export const updateMedicalRecordNote = async (recordId: string, userId: string, hospitalId: string, note: string) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/note`, {
    note: note,
    userId: userId,
    hospitalId: hospitalId,
  });
  return response.data;
};

export const getReservationsByUserId = async (userId: string) => {
  const response = await apiClient.get(`/medical-records/users/${userId}`);
  return response.data;
};

export const getReservationsForUserInHospital = async (userId: string, hospitalId: string) => {
  const response = await apiClient.get(`/medical-records/users/${userId}/hospitals/${hospitalId}`);
  return response.data;
};
