import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createHospitalAndAdmin } from '@/api/admin';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardFooter, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function AdminRegisterPage() {
  const [hospitalName, setHospitalName] = useState('');
  const [hospitalAddress, setHospitalAddress] = useState('');
  const [adminName, setAdminName] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // 비밀번호 확인
    if (adminPassword !== confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.');
      return;
    }

    if (adminPassword.length < 6) {
      setError('비밀번호는 최소 6자 이상이어야 합니다.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await createHospitalAndAdmin({
        name: hospitalName,
        address: hospitalAddress,
        adminName: adminName,
        adminEmail: adminEmail,
        adminPassword: adminPassword,
      });

      if (response.result.resultCode === 200) {
        navigate('/admin/login');
      } else {
        setError(response.result.resultMessage || '병원 생성에 실패했습니다.');
      }
    } catch (err) {
      setError('병원 생성 중 오류가 발생했습니다.');
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
            <div>
              <CardTitle className="text-2xl mb-2">병원 등록</CardTitle>
              <CardDescription className="text-gray-600 text-base">
                새로운 병원을 등록하고 최고 관리자 계정을 생성하세요
              </CardDescription>
            </div>
          </CardHeader>

          <CardContent className="grid gap-6 px-8 py-4">
            {error && <p className="text-red-500 text-sm text-center bg-red-50 p-3 rounded-lg">{error}</p>}

            {/* 병원 정보 */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-800 border-b pb-2">병원 정보</h3>
              <div className="grid gap-2">
                <Label htmlFor="hospitalName">병원명</Label>
                <Input
                  id="hospitalName"
                  placeholder="예: 조은의원"
                  required
                  value={hospitalName}
                  onChange={(e) => setHospitalName(e.target.value)}
                  disabled={isLoading}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="hospitalAddress">병원 주소</Label>
                <Input
                  id="hospitalAddress"
                  placeholder="예: 서울시 강남구 역삼동"
                  required
                  value={hospitalAddress}
                  onChange={(e) => setHospitalAddress(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </div>

            {/* 관리자 정보 */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-800 border-b pb-2">최고 관리자 정보</h3>
              <div className="grid gap-2">
                <Label htmlFor="adminName">관리자 이름</Label>
                <Input
                  id="adminName"
                  placeholder="예: 김실장"
                  required
                  value={adminName}
                  onChange={(e) => setAdminName(e.target.value)}
                  disabled={isLoading}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="adminEmail">관리자 이메일</Label>
                <Input
                  id="adminEmail"
                  type="email"
                  placeholder="admin@example.com"
                  required
                  value={adminEmail}
                  onChange={(e) => setAdminEmail(e.target.value)}
                  disabled={isLoading}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="adminPassword">비밀번호</Label>
                <Input
                  id="adminPassword"
                  type="password"
                  placeholder="최소 6자 이상"
                  required
                  value={adminPassword}
                  onChange={(e) => setAdminPassword(e.target.value)}
                  disabled={isLoading}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="confirmPassword">비밀번호 확인</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="비밀번호를 다시 입력하세요"
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={isLoading}
                />
              </div>
            </div>
          </CardContent>

          <CardFooter className="flex flex-col space-y-4 px-8">
            <Button className="w-full" type="submit" disabled={isLoading}>
              {isLoading ? '병원 생성 중...' : '병원 등록하기'}
            </Button>
            <div className="text-center text-sm text-gray-500">
              이미 병원이 등록되어 있나요?{' '}
              <button
                type="button"
                onClick={() => navigate('/admin/login')}
                className="text-green-600 hover:underline font-medium"
              >
                관리자 로그인
              </button>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
