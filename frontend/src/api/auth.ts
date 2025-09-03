import apiClient from './client';

// As per UserLoginRequest.java
interface UserLoginCredentials {
  email: string;
  password: string;
}

// As per UserRegisterRequest.java
interface UserRegisterCredentials {
  name: string;
  email: string;
  password: string;
}

// As per the backend controller Api<String> response for login
interface LoginResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: string; // This will be the auth token
}

// As per the backend controller Api<User> response for register and get /me
interface User {
  id: string;
  name: string;
  email: string;
  // Add other fields from User.java if needed
}

interface UserResponse {
  result: {
    resultCode: number;
    resultMessage: string;
    resultDescription: string;
  };
  body: User;
}

export const loginUser = async (credentials: UserLoginCredentials): Promise<LoginResponse> => {
  try {
    const response = await apiClient.post<LoginResponse>('/user/login', credentials);
    return response.data;
  } catch (error) {
    // In a real app, you'd want to handle this more gracefully
    console.error('Login failed:', error);
    throw error;
  }
};

export const registerUser = async (credentials: UserRegisterCredentials): Promise<UserResponse> => {
  try {
    const response = await apiClient.post<UserResponse>('/user', credentials);
    return response.data;
  } catch (error) {
    console.error('Registration failed:', error);
    throw error;
  }
};

export const getCurrentUser = async (): Promise<UserResponse> => {
  try {
    const response = await apiClient.get<UserResponse>('/user/me');
    return response.data;
  } catch (error) {
    console.error('Failed to fetch current user:', error);
    throw error;
  }
};
