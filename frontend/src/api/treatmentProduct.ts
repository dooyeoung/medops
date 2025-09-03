import apiClient from './client';

export const getTreatmentProductsByHospital = async (hospitalId: string) => {
  const response = await apiClient.get(`/hospital/${hospitalId}/treatment-products`);
  return response.data;
};

interface TreatmentProductData {
  name: string;
  description: string;
  duration: number;
  maxCapacity: number;
  hospitalId?: string; // Optional for update, required for creation
}

export const createTreatmentProduct = async (data: TreatmentProductData) => {
  const response = await apiClient.post('/treatment-products', data);
  return response.data;
};

export const updateTreatmentProduct = async (productId: string, data: Partial<TreatmentProductData>) => {
  const response = await apiClient.put(`/treatment-products/${productId}`, data);
  return response.data;
};

export const deleteTreatmentProduct = async (productId: string) => {
  const response = await apiClient.delete(`/treatment-products/${productId}`);
  return response.data;
};

export const recoverTreatmentProduct = async (productId: string) => {
  const response = await apiClient.patch(`/treatment-products/${productId}/recover`);
  return response.data;
};
