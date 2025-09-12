import React, { createContext, useState, useContext, useEffect, type ReactNode } from 'react';
import { loginUser, getCurrentUser } from '@/api/auth';
import { adminLogin, getCurrentAdmin } from '@/api/admin';
import { setAuthToken } from '@/api/client'; // Import setAuthToken

// Define types for User and Admin based on your backend models
interface User {
  id: string;
  name: string;
  email: string;
}

interface Admin {
  id: string;
  name: string;
  email: string;
  role: string;
  hospital: {
    id: string;
    name: string;
  };
}

interface AuthContextType {
  user: User | null;
  admin: Admin | null;
  token: string | null;
  role: 'user' | 'admin' | null;
  isLoading: boolean;
  loginUser: (credentials: any) => Promise<void>;
  loginAdmin: (credentials: any) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [admin, setAdmin] = useState<Admin | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
  const [role, setRole] = useState<'user' | 'admin' | null>(localStorage.getItem('authRole') as 'user' | 'admin');
  const [isLoading, setIsLoading] = useState(true);
  const [hasInitialized, setHasInitialized] = useState(false);

  useEffect(() => {
    const fetchAuthData = async () => {
      if (token && role) {
        setAuthToken(token); // Set token on initial load
        try {
          if (role === 'user') {
            const userResponse = await getCurrentUser();
            if (userResponse.result.resultCode === 200) {
              setUser(userResponse.body);
            } else {
              console.error('Failed to fetch user data on init', userResponse.result.resultMessage);
              logout(); // Clear invalid session
            }
          } else if (role === 'admin') {
            try {
              const adminResponse = await getCurrentAdmin();
              if (adminResponse.result.resultCode === 200) {
                setAdmin(adminResponse.body);
              } else {
                console.error('Failed to fetch admin data on init', adminResponse.result.resultMessage);
                logout(); // Clear invalid session
              }
            } catch (adminError) {
              console.error('Failed to fetch current admin:', adminError);
              logout(); // 토큰이 유효하지 않으면 로그아웃
            }
          }
        } catch (error) {
          console.error('Error fetching auth data on init', error);
          logout(); // Clear invalid session
        }
      }
      setIsLoading(false);
    };

    // 한 번만 실행되도록 가드 조건 추가
    if (!hasInitialized) {
      setHasInitialized(true);
      fetchAuthData();
    }
  }, []); // 빈 배열로 한 번만 실행

  const handleLogin = async (newToken: string, newRole: 'user' | 'admin') => {
    setAuthToken(newToken); // Set token immediately
    setToken(newToken);
    setRole(newRole);
    localStorage.setItem('authToken', newToken);
    localStorage.setItem('authRole', newRole);

    // Re-fetch user/admin data after login to populate state
    try {
      if (newRole === 'user') {
        const res = await getCurrentUser();
        if (res.result.resultCode === 200) {
          setUser(res.body);
        } else {
          console.error('Failed to fetch user after login:', res.result.resultMessage);
        }
      } else if (newRole === 'admin') {
        const res = await getCurrentAdmin();
        if (res.result.resultCode === 200) {
          setAdmin(res.body);
        } else {
          console.error('Failed to fetch admin after login:', res.result.resultMessage);
        }
      }
    } catch (error) {
      console.error('Failed to fetch current user/admin after login:', error);
    }
  };

  const loginUserHandler = async (credentials: any) => {
    setIsLoading(true);
    try {
      const response = await loginUser(credentials);
      if (response.result.resultCode === 200) {
        await handleLogin(response.body, 'user'); // Make handleLogin async
      } else {
        throw new Error(response.result.resultMessage || 'Login failed');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const loginAdminHandler = async (credentials: any) => {
    setIsLoading(true);
    try {
      const response = await adminLogin(credentials);
      if (response.result.resultCode === 200) {
        await handleLogin(response.body, 'admin'); // Make handleLogin async
      } else {
        throw new Error(response.result.resultMessage || 'Admin login failed');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setAdmin(null);
    setToken(null);
    setRole(null);
    localStorage.removeItem('authToken');
    localStorage.removeItem('authRole');
    setAuthToken(null); // Clear token from headers
  };

  const value = {
    user,
    admin,
    token,
    role,
    isLoading,
    loginUser: loginUserHandler,
    loginAdmin: loginAdminHandler,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
