import { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from '@/components/ui/dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { getHospitalAdmins } from '@/api/hospital';
import { inviteAdmin } from '@/api/admin';
import { toast } from 'sonner';

interface Admin {
  id: string;
  name: string;
  email: string;
  role: string;
  status: string;
  createdAt: string;
}

interface AdminInviteFormData {
  email: string;
}

function AdminInviteForm({ onSave }: { onSave: (data: AdminInviteFormData) => void }) {
  const [formData, setFormData] = useState<AdminInviteFormData>({
    email: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <form id="admin-invite-form" onSubmit={handleSubmit} className="grid gap-4 py-4">
      <div className="grid grid-cols-4 items-center gap-4">
        <Label htmlFor="email" className="text-right">
          이메일
        </Label>
        <Input
          id="email"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          className="col-span-3"
          required
        />
      </div>
    </form>
  );
}

function AdminManagement({ hospitalId }: { hospitalId: string | null }) {
  const [admins, setAdmins] = useState<Admin[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isInviteDialogOpen, setIsInviteDialogOpen] = useState(false);
  const [isInviting, setIsInviting] = useState(false);

  useEffect(() => {
    const fetchAdmins = async () => {
      if (!hospitalId) return;

      setIsLoading(true);
      try {
        const response = await getHospitalAdmins(hospitalId);
        setAdmins(response.body);
      } catch (error) {
        toast.error('관리자 목록을 불러오는데 실패했습니다.');
        console.error('Failed to fetch admins:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchAdmins();
  }, [hospitalId]);

  const handleInviteAdmin = async (formData: AdminInviteFormData) => {
    setIsInviting(true);
    try {
      await inviteAdmin(formData);
      toast.success('관리자 초대가 완료되었습니다.');
      setIsInviteDialogOpen(false);
      // Note: Admin will appear in list after they complete registration
    } catch (error) {
      toast.error('관리자 초대에 실패했습니다.');
      console.error('Failed to invite admin:', error);
    } finally {
      setIsInviting(false);
    }
  };

  const getRoleBadge = (role: string) => {
    switch (role) {
      case 'SUPER_ADMIN':
        return <Badge variant="destructive">슈퍼 관리자</Badge>;
      case 'ADMIN':
        return <Badge variant="default">관리자</Badge>;
      default:
        return <Badge variant="outline">{role}</Badge>;
    }
  };

  return (
    <>
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-4">
          <div>
            <CardTitle>관리자 관리</CardTitle>
            <CardDescription>병원 관리자를 초대하고 관리하세요</CardDescription>
          </div>
          <Button onClick={() => setIsInviteDialogOpen(true)}>관리자 초대</Button>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <span className="ml-2 text-sm text-gray-500">정보를 불러오는 중...</span>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>이름</TableHead>
                  <TableHead>이메일</TableHead>
                  <TableHead>역할</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead>가입일</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {admins.map((admin) => (
                  <TableRow key={admin.id}>
                    <TableCell>{admin.name || '이름 미설정'}</TableCell>
                    <TableCell>{admin.email}</TableCell>
                    <TableCell>{getRoleBadge(admin.role)}</TableCell>
                    <TableCell>{admin.status}</TableCell>
                    <TableCell>{admin.createdAt}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Admin Invite Dialog */}
      <Dialog open={isInviteDialogOpen} onOpenChange={setIsInviteDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>관리자 초대</DialogTitle>
          </DialogHeader>
          <AdminInviteForm onSave={handleInviteAdmin} />
          <DialogFooter>
            <DialogClose asChild>
              <Button type="button" variant="secondary">
                취소
              </Button>
            </DialogClose>
            <Button type="submit" form="admin-invite-form" disabled={isInviting}>
              {isInviting ? '초대중...' : '초대'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

export default AdminManagement;
