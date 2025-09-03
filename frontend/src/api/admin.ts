import apiClient from './client';

interface HospitalCreateRequest {
  name: string;
  address: string;
  adminEmail: string;
  adminName: string;
  adminPassword: string;
}

interface HospitalCreateResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: {
    // Hospital object structure
    id: string;
    name: string;
    address: string;
    createdAt: string;
  };
}

interface AdminLoginRequest {
  email: string;
  password: string;
  hospitalName: string;
}

interface AdminLoginResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: string; // This will be the auth token
}

interface VerifyAdminInvitationCodeRequest {
  email: string;
  code: string;
}

interface VerifyAdminInvitationCodeResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: string; // The token string is returned directly in the body
}

interface Admin {
  id: string;
  name: string;
  email: string;
  role: string; // Assuming AdminRole enum is string
  status: string; // Assuming AdminStatus enum is string
  hospital: {
    // Assuming Hospital object structure
    id: string;
    name: string;
    address: string;
    createdAt: string;
  };
}

interface AdminResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: Admin;
}

interface AdminUpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

interface AdminUpdatePasswordResponse {
  data: string; // Assuming a success message or status
  message: string | null;
  status: string;
}

export const createHospitalAndAdmin = async (request: HospitalCreateRequest): Promise<HospitalCreateResponse> => {
  try {
    const response = await apiClient.post<HospitalCreateResponse>('/hospital', request);
    return response.data;
  } catch (error) {
    console.error('Hospital and Admin creation failed:', error);
    throw error;
  }
};

export const adminLogin = async (credentials: AdminLoginRequest): Promise<AdminLoginResponse> => {
  try {
    const response = await apiClient.post<AdminLoginResponse>('/admin/login', credentials);
    return response.data;
  } catch (error) {
    console.error('Admin login failed:', error);
    throw error;
  }
};

export const verifyAdminInvitationCode = async (
  request: VerifyAdminInvitationCodeRequest,
): Promise<VerifyAdminInvitationCodeResponse> => {
  try {
    const response = await apiClient.post<VerifyAdminInvitationCodeResponse>('/admin/verify-invitation-code', request);
    return response.data;
  } catch (error) {
    console.error('Invitation code verification failed:', error);
    throw error;
  }
};

export const getCurrentAdmin = async (): Promise<AdminResponse> => {
  try {
    const response = await apiClient.get<AdminResponse>('/admin/me');
    return response.data;
  } catch (error) {
    console.error('Failed to fetch current admin:', error);
    throw error;
  }
};

export const updateAdminPassword = async (
  request: AdminUpdatePasswordRequest,
): Promise<AdminUpdatePasswordResponse> => {
  try {
    const response = await apiClient.put<AdminUpdatePasswordResponse>('/admin/update-password', request);
    return response.data;
  } catch (error) {
    console.error('Admin password update failed:', error);
    throw error;
  }
};

interface AdminInviteRequest {
  email: string;
}

// For Api<Void> which returns Api.OK(null)
interface AdminInviteResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: null;
}

export const inviteAdmin = async (request: AdminInviteRequest): Promise<AdminInviteResponse> => {
  try {
    const response = await apiClient.post<AdminInviteResponse>('/admin/invite', request);
    return response.data;
  } catch (error) {
    console.error('Admin invitation failed:', error);
    throw error;
  }
};

// For Step 4: Finalizing the account setup
interface ActivateAdminAccountRequest {
  adminEmail: string;
  adminName: string;
  password: string;
  registrationToken: string;
}

interface ActivateAdminAccountResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: null; // Api<Void>
}

export const activateAdminAccount = async (
  request: ActivateAdminAccountRequest,
): Promise<ActivateAdminAccountResponse> => {
  try {
    const response = await apiClient.post<ActivateAdminAccountResponse>('/admin/activate-account', request);
    return response.data;
  } catch (error) {
    console.error('Admin account activation failed:', error);
    throw error;
  }
};

// Legacy function for backward compatibility
interface CompleteAdminSetupRequest {
  registrationToken: string;
  name: string;
  password: string;
}

interface CompleteAdminSetupResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: null; // Api<Void>
}

export const completeAdminSetup = async (request: CompleteAdminSetupRequest): Promise<CompleteAdminSetupResponse> => {
  try {
    const response = await apiClient.post<CompleteAdminSetupResponse>('/admin/activate-account', request);
    return response.data;
  } catch (error) {
    console.error('Admin account setup failed:', error);
    throw error;
  }
};
