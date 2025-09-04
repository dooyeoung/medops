import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '@/api/auth';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function SignupPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await registerUser({ name, email, password });
      alert('Registration successful! Please log in.');
      navigate('/login'); // Redirect to login page on success
    } catch (err) {
      setError('Registration failed. Please try again.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  {
    /* 회원가입 폼 */
  }
  return (
    <div className="flex justify-center items-center w-lg mx-auto" style={{ height: '90vh' }}>
      <Card className="w-full">
        <form onSubmit={handleSubmit}>
          <CardHeader className="text-center py-4 px-8">
            <CardTitle className="text-2xl">회원가입</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-6 px-8 py-4">
            {error && <p className="text-red-500 text-sm text-center">{error}</p>}
            <div className="grid gap-2">
              <Label htmlFor="name">이름</Label>
              <Input
                id="name"
                placeholder="홍길동"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={isLoading}
              />
            </div>
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
              {isLoading ? '계정 생성 중...' : '회원가입'}
            </Button>
            <div className="text-center text-sm">
              <p className="text-gray-600">
                이미 계정이 있으신가요?{' '}
                <Link to="/login" className="text-blue-600 hover:underline font-medium">
                  로그인
                </Link>
              </p>
              <p className="text-gray-600">
                관리자 계정이 필요하신가요?{' '}
                <Link to="/admin/register" className="text-green-600 hover:underline font-medium">
                  관리자 계정 생성
                </Link>
              </p>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
