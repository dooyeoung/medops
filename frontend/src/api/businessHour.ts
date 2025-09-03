import apiClient from './client';

export const getBusinessHoursByHospital = async (hospitalId: string) => {
  const response = await apiClient.get(`/business-hours/hospital/${hospitalId}`);
  return response.data;
};

export const updateBusinessHour = async (businessHourId: string, data) => {
  const response = await apiClient.put(`/business-hours/${businessHourId}`, data);
  return response.data;
};
