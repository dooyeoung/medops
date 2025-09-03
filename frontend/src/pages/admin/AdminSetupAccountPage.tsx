import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { activateAdminAccount } from '@/api/admin';
import { toast } from 'sonner';

export default function AdminSetupAccountPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    password: '',
    confirmPassword: '',
  });

  const registrationToken = searchParams.get('token');
  const adminEmail = searchParams.get('email');

  useEffect(() => {
    if (!registrationToken || !adminEmail) {
      toast.error('유효하지 않은 링크입니다.');
      navigate('/admin/login');
    }
  }, [registrationToken, adminEmail, navigate]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      toast.error('비밀번호가 일치하지 않습니다.');
      return;
    }

    if (!registrationToken || !adminEmail) {
      toast.error('유효하지 않은 토큰입니다.');
      return;
    }

    setIsLoading(true);
    try {
      await activateAdminAccount({
        adminEmail,
        adminName: formData.name,
        password: formData.password,
        registrationToken,
      });

      toast.success('계정 설정이 완료되었습니다. 로그인해주세요.');
      navigate('/admin/login');
    } catch (error) {
      console.error('Account setup failed:', error);
      toast.error('계정 설정에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl text-center">계정 설정</CardTitle>
          <CardDescription className="text-center">관리자 계정을 설정하여 가입을 완료하세요</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">이름</Label>
              <Input
                id="name"
                name="name"
                type="text"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="이름을 입력하세요"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">비밀번호</Label>
              <Input
                id="password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleInputChange}
                placeholder="비밀번호를 입력하세요"
                required
                minLength={6}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">비밀번호 확인</Label>
              <Input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                value={formData.confirmPassword}
                onChange={handleInputChange}
                placeholder="비밀번호를 다시 입력하세요"
                required
                minLength={6}
              />
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? '설정 중...' : '계정 설정 완료'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
