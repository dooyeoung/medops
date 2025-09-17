import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Toaster, toast } from 'sonner';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { loginUser, isLoading } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await loginUser({ email, password });
      navigate('/hospitals'); // Redirect to home page on success
    } catch (err) {
      toast.error('로그인 실패', {
        description: '이메일과 비밀번호를 확인하세요',
      });
    }
  };

  return (
    <div className="flex justify-center items-center w-lg mx-auto" style={{ height: '90vh' }}>
      <Toaster expand={true} richColors position="top-center" />
      <Card className="w-full">
        <form onSubmit={handleSubmit}>
          <CardHeader className="text-center py-4 px-8">
            <div>
              <CardTitle className="text-2xl">{isLoading ? '로그인 중...' : '로그인'}</CardTitle>
            </div>
          </CardHeader>
          <CardContent className="grid gap-6 px-8 py-4">
            <div className="grid gap-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                type="email"
                placeholder="example@email.com"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password">비밀번호</Label>
              <Input
                id="password"
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
              로그인
            </Button>

            <div className="text-center text-sm space-y-2">
              <p className="text-gray-600">
                계정이 없으신가요?{' '}
                <Link to="/signup" className="text-blue-600 hover:underline font-medium">
                  회원가입
                </Link>
              </p>
              <p className="text-gray-600">
                관리자이신가요?{' '}
                <Link to="/admin/login" className="text-green-600 hover:underline font-medium">
                  관리자 로그인
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
                        <span className="font-semibold">이메일:</span> testuser1@gmail.com
                      </p>
                      <p>
                        <span className="font-semibold">비밀번호:</span> 1234!@#$
                      </p>
                    </div>
                  </PopoverContent>
                </Popover>
              </p>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
