import apiClient from './client';

interface Hospital {
  id: string;
  name: string;
  address: string;
  createdAt: string;
}

interface HospitalListResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: Hospital[];
}

interface HospitalDetailResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: Hospital;
}

export const getHospitals = async (): Promise<HospitalListResponse> => {
  try {
    const response = await apiClient.get<HospitalListResponse>('/hospital');
    return response.data;
  } catch (error) {
    console.error('Failed to fetch hospitals:', error);
    throw error;
  }
};

export const getHospitalById = async (id: string): Promise<HospitalDetailResponse> => {
  try {
    const response = await apiClient.get<HospitalDetailResponse>(`/hospital/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Failed to fetch hospital with ID ${id}:`, error);
    throw error;
  }
};

interface AdminListResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: {
    id: string;
    name: string;
    email: string;
    role: string;
    status: string;
    hospital: Hospital;
    createdAt: string;
  }[];
}

export const getHospitalAdmins = async (hospitalId: string): Promise<AdminListResponse> => {
  try {
    const response = await apiClient.get<AdminListResponse>(`/hospital/${hospitalId}/admins`);
    return response.data;
  } catch (error) {
    console.error(`Failed to fetch admins for hospital ${hospitalId}:`, error);
    throw error;
  }
};
