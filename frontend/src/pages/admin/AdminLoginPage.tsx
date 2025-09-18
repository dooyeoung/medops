import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardFooter, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Toaster, toast } from 'sonner';

export default function AdminLoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [hospitalName, setHospitalName] = useState('');
  const { loginAdmin, isLoading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await loginAdmin({ email, password, hospitalName });
      navigate('/admin/schedule'); // Redirect to admin dashboard on success
    } catch (err) {
      toast.error('로그인 실패', {
        description: '소속 병원, 이메일과 비밀번호를 확인하세요',
      });
    }
  };

  return (
    <div className="flex justify-center items-center w-lg mx-auto" style={{ height: '90vh' }}>
      <Toaster expand={true} richColors position="top-center" />
      <Card className="w-full">
        <form onSubmit={handleSubmit}>
          <CardHeader className="text-center py-4 px-8">
            <CardTitle className="text-2xl">관리자 로그인</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-6 px-8 py-4">
            <div className="grid gap-2">
              <Label htmlFor="hospitalName">소속 병원 이름</Label>
              <Input
                id="hospitalName"
                placeholder="Grace Medical Center"
                required
                value={hospitalName}
                onChange={(e) => setHospitalName(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="adminEmail"
                type="email"
                placeholder="admin@example.com"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password">비밀번호</Label>
              <Input
                id="adminPassword"
                type="password"
                placeholder="비밀번호를 입력하세요"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isLoading}
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4 px-8">
            <Button className="w-full" type="submit" disabled={isLoading}>
              {isLoading ? '로그인 중...' : '로그인'}
            </Button>
            <div className="text-center text-sm space-y-2">
              <p className="text-gray-600">
                계정이 없으신가요?{' '}
                <Link to="/admin/register" className="text-blue-600 hover:underline font-medium">
                  병원 등록 및 회원가입
                </Link>
              </p>
              <p className="text-gray-600">
                사용자이신가요?{' '}
                <Link to="/login" className="text-green-600 hover:underline font-medium">
                  사용자 로그인
                </Link>
              </p>
              <p className="text-gray-600">
                체험을 원하시나요?{' '}
                <Popover>
                  <PopoverTrigger asChild>
                    <span className="text-yellow-600 hover:underline font-medium cursor-pointer">
                      데모계정 확인하기
                    </span>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-4">
                    <div className="text-sm text-left">
                      <p>
                        <span className="font-semibold">소속병원:</span> 조은의원
                      </p>
                      <p>
                        <span className="font-semibold">이메일:</span> admin@gmail.com
                      </p>
                      <p>
                        <span className="font-semibold">비밀번호:</span> 1234!@#$
                      </p>
                    </div>
                  </PopoverContent>
                </Popover>
              </p>
              <p className="text-gray-600">
                네트워크 문제로 동작에 문제가 있는경우 데모 영상을 참고하세요
              </p>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
