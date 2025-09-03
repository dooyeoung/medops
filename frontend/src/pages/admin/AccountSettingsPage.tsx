import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { updateAdminPassword } from '@/api/admin';
import { Toaster, toast } from 'sonner';

export default function AccountSettingsPage() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    if (newPassword !== confirmNewPassword) {
      setIsLoading(false);
      return;
    }

    try {
      const response = await updateAdminPassword({
        currentPassword,
        newPassword,
      });
      if (response.result.resultCode === 200) {
        toast.success('비밀번호 변경 성공', {
          description: '비밀번호를 변경하였습니다',
        });

        setCurrentPassword('');
        setNewPassword('');
        setConfirmNewPassword('');
      } else {
        setError(response.message || 'Failed to update password.');
      }
    } catch (err) {
      toast.error('비밀번호 변경 실패', {
        description: '비밀번호 입력을 확인하세요',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center w-lg mx-auto" style={{ height: '90vh' }}>
      <Toaster expand={true} richColors position="top-center" />
      <Card className="w-full">
        <form onSubmit={handleSubmit}>
          <CardHeader className="text-center py-4 px-8">
            <CardTitle className="text-2xl">관리자 계정 설정</CardTitle>
            <CardDescription>이름과 사용할 비밀번호를 설정해주세요.</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-6 px-8 py-4">
            <div className="grid gap-2">
              <Label htmlFor="currentPassword">Current Password</Label>
              <Input
                id="currentPassword"
                type="password"
                required
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="newPassword">New Password</Label>
              <Input
                id="newPassword"
                type="password"
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                disabled={isLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="confirmNewPassword">Confirm New Password</Label>
              <Input
                id="confirmNewPassword"
                type="password"
                required
                value={confirmNewPassword}
                onChange={(e) => setConfirmNewPassword(e.target.value)}
                disabled={isLoading}
              />
            </div>
          </CardContent>
          <CardContent>
            <Button className="w-full" type="submit" disabled={isLoading}>
              {isLoading ? 'Updating...' : 'Update Password'}
            </Button>
          </CardContent>
        </form>
      </Card>
    </div>
  );
}
