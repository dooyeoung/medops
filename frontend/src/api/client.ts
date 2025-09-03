import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Assuming backend runs on port 8080
  headers: {
    'Content-Type': 'application/json',
  },
});

export const setAuthToken = (token: string | null) => {
  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

export default apiClient;
