import { useState } from 'react';
import { inviteAdmin } from '@/api/admin'; // Import the new function
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardFooter, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useNavigate } from 'react-router-dom';
import { Toaster, toast } from 'sonner';

export default function AdminInvitePage() {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const response = await inviteAdmin({ email });
      if (response.result.resultCode === 200) {
        setEmail(''); // Clear input on success
        toast.success('관리자 초대 성공', {
          description: email + ' 관리를 초대하였습니다',
        });
      } else {
      }
    } catch (err) {
      alert('관리자 초대 실패, 초대 중 오류가 발생했습니다. 다시 로그인을 시도하세요');
      navigate('/admin/login');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center w-lg mx-auto" style={{ height: '90vh' }}>
      <Toaster expand={true} richColors position="top-center" />
      <Card className="w-full">
        <form onSubmit={handleInvite}>
          <CardHeader className="text-center py-4 px-8">
            <CardTitle className="text-2xl">관리자 초대</CardTitle>
            <CardDescription className="text-gray-600">초대할 관리자의 이메일을 입력해주세요.</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-6 px-8 py-4">
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
          </CardContent>

          <CardFooter className="flex flex-col space-y-4 px-8">
            <Button className="w-full" type="submit" disabled={isLoading}>
              {isLoading ? '초대 이메일 발송 중...' : '관리자 초대하기'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
