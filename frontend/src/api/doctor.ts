import apiClient from './client';

export interface Doctor {
  id: string;
  name: string;
  hospitalId: string;
}

export interface CreateDoctorRequest {
  name: string;
  hospitalId: string;
}

export interface UpdateDoctorRequest {
  name: string;
}

export const getDoctorsByHospital = async (hospitalId: string) => {
  const response = await apiClient.get(`/doctor/hospitals/${hospitalId}`);
  return response.data;
};

export const createDoctor = async (data: CreateDoctorRequest) => {
  const response = await apiClient.post('/doctor', data);
  return response.data;
};

export const updateDoctor = async (doctorId: string, data: UpdateDoctorRequest) => {
  const response = await apiClient.put(`/doctor/${doctorId}`, data);
  return response.data;
};

export const deleteDoctor = async (doctorId: string) => {
  const response = await apiClient.delete(`/doctor/${doctorId}`);
  return response.data;
};

export const recoverDoctor = async (doctorId: string) => {
  const response = await apiClient.patch(`/doctor/${doctorId}/recover`);
  return response.data;
};

export interface AssignDoctorRequest {
  userId: string;
  hospitalId: string;
  doctorId: string;
}

export const assignDoctorToReservation = async (recordId: string, data: AssignDoctorRequest) => {
  const response = await apiClient.patch(`/medical-records/${recordId}/doctor`, data);
  return response.data;
};
