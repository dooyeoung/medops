import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { verifyAdminInvitationCode } from '@/api/admin';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardFooter, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function AdminVerifyInvitationPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const urlCode = searchParams.get('code');
    if (urlCode) {
      setCode(urlCode);
    }
    const urlEmail = searchParams.get('email');
    if (urlEmail) {
      setEmail(urlEmail);
    }
  }, [searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const response = await verifyAdminInvitationCode({ email, code });
      // The API response body is the token string itself
      if (response.result.resultCode === 200 && typeof response.body === 'string' && response.body) {
        // On success, navigate to the setup page with the token and email as URL parameters
        const params = new URLSearchParams();
        params.set('token', response.body);
        params.set('email', email);
        navigate(`/admin/setup-account?${params.toString()}`);
      } else {
        setError(response.result.resultMessage || '초대 코드 검증에 실패했습니다. 이메일과 코드를 확인해주세요.');
      }
    } catch (err) {
      setError('검증 중 오류가 발생했습니다. 네트워크 연결을 확인해주세요.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center w-lg mx-auto" style={{ height: '90vh' }}>
      <Card className="w-full">
        <form onSubmit={handleSubmit}>
          <CardHeader className="text-center py-4 px-8">
            <CardTitle className="text-2xl">관리자 초대 검증</CardTitle>
            <CardDescription>받으신 이메일과 초대 코드를 입력하여 계정을 활성화하세요.</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-6 px-8 py-4">
            {error && <p className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg">{error}</p>}
            <div className="grid gap-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                type="email"
                placeholder="admin@example.com"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="code">초대 코드</Label>
              <Input
                id="code"
                type="text"
                placeholder="초대 코드를 입력하세요"
                required
                value={code}
                onChange={(e) => setCode(e.target.value)}
                disabled={isLoading}
              />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4 px-8">
            <Button className="w-full" type="submit" disabled={isLoading}>
              {isLoading ? '검증 중...' : '초대 검증하기'}
            </Button>
            <div className="text-center text-sm text-gray-500">
              <Link to="/admin/login" className="text-green-600 hover:underline font-medium">
                이미 계정이 있으신가요? 로그인
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
