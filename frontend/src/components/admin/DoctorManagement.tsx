import { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogClose,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { getDoctorsByHospital, createDoctor, updateDoctor, deleteDoctor, recoverDoctor } from '@/api/doctor';
import { toast } from 'sonner';
import type { Doctor } from '@/types';

interface Props {
  hospitalId: string | null;
}
export default function DoctorManagement({ hospitalId }: Props) {
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isDoctorModalOpen, setIsDoctorModalOpen] = useState(false);
  const [newDoctorName, setNewDoctorName] = useState('');
  const [isEditDoctorModalOpen, setIsEditDoctorModalOpen] = useState(false);
  const [editingDoctor, setEditingDoctor] = useState<Doctor | null>(null);
  const [editDoctorName, setEditDoctorName] = useState('');
  const [isAddingDoctor, setIsAddingDoctor] = useState(false);
  const [isUpdatingDoctor, setIsUpdatingDoctor] = useState(false);
  const [deletingDoctorId, setDeletingDoctorId] = useState<string | null>(null);
  const [recoveringDoctorId, setRecoveringDoctorId] = useState<string | null>(null);

  const fetchDoctors = async () => {
    if (!hospitalId) return;

    setIsLoading(true);
    try {
      const doctorsResponse = await getDoctorsByHospital(hospitalId);
      setDoctors(doctorsResponse.body || []);
    } catch (err: any) {
      console.error('Failed to fetch doctors:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchDoctors();
  }, [hospitalId]);

  const handleAddDoctor = async () => {
    if (!newDoctorName.trim() || !hospitalId) return;

    setIsAddingDoctor(true);
    try {
      await createDoctor({
        name: newDoctorName,
        hospitalId: hospitalId,
      });

      setNewDoctorName('');
      setIsDoctorModalOpen(false);
      await fetchDoctors(); // 의사 목록 새로고침
    } catch (error) {
      console.error('Failed to add doctor:', error);
      alert('의사 추가에 실패했습니다.');
    } finally {
      setIsAddingDoctor(false);
    }
  };

  const handleDelete = async (doctorId: string, doctorName: string) => {
    if (window.confirm('정말로 삭제하시겠습니까?')) {
      setDeletingDoctorId(doctorId);
      try {
        await deleteDoctor(doctorId);
        await fetchDoctors(); // 의사 목록 새로고침
        toast.success('의사 삭제 성공', {
          description: doctorName + ' 의사를 삭제했습니다',
        });
      } catch (err: any) {
        console.log(err);
        toast.error('의사 삭제 실패', {
          description: '서버 오류입니다',
        });
      } finally {
        setDeletingDoctorId(null);
      }
    }
  };
  const handleRecover = async (doctorId: string, doctorName: string) => {
    if (window.confirm('정말로 복구하시겠습니까?')) {
      setRecoveringDoctorId(doctorId);
      try {
        await recoverDoctor(doctorId);
        await fetchDoctors(); // 의사 목록 새로고침
        toast.success('의사 복구 성공', {
          description: doctorName + ' 의사를 복구했습니다',
        });
      } catch (err: any) {
        console.log(err);
        toast.error('의사 복구 실패', {
          description: '서버 오류입니다',
        });
      } finally {
        setRecoveringDoctorId(null);
      }
    }
  };
  const handleEditDoctor = (doctor: Doctor) => {
    setEditingDoctor(doctor);
    setEditDoctorName(doctor.name);
    setIsEditDoctorModalOpen(true);
  };

  const handleUpdateDoctor = async () => {
    if (!editDoctorName.trim() || !editingDoctor) return;

    setIsUpdatingDoctor(true);
    try {
      await updateDoctor(editingDoctor.id, {
        name: editDoctorName,
      });

      setEditDoctorName('');
      setEditingDoctor(null);
      setIsEditDoctorModalOpen(false);

      toast.success('의사 정보 수정 성공', {
        description: '의사 정보를 성공적으로 수정했습니다',
      });
      await fetchDoctors(); // 의사 목록 새로고침
    } catch (err: any) {
      console.log(err);
      toast.error('의사 정보 수정 실패', {
        description: '서버 오류입니다',
      });
    } finally {
      setIsUpdatingDoctor(false);
    }
  };
  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>의사 관리</CardTitle>
              <CardDescription>병원에 소속된 의사를 관리하세요.</CardDescription>
            </div>
            <Dialog open={isDoctorModalOpen} onOpenChange={setIsDoctorModalOpen}>
              <DialogTrigger asChild>
                <Button>의사 추가</Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[400px]">
                <DialogHeader>
                  <DialogTitle>새 의사 추가</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                  <div className="space-y-2">
                    <Label htmlFor="doctorName">의사 이름</Label>
                    <Input
                      id="doctorName"
                      value={newDoctorName}
                      onChange={(e) => setNewDoctorName(e.target.value)}
                      placeholder="의사 이름을 입력하세요"
                    />
                  </div>
                </div>
                <DialogFooter>
                  <DialogClose asChild>
                    <Button variant="outline">취소</Button>
                  </DialogClose>
                  <Button onClick={handleAddDoctor} disabled={isAddingDoctor}>
                    {isAddingDoctor ? '추가중...' : '추가'}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
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
                  <TableHead>등록일</TableHead>
                  <TableHead>삭제일</TableHead>
                  <TableHead className="text-right">작업</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {doctors.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={2} className="text-center text-muted-foreground">
                      등록된 의사가 없습니다.
                    </TableCell>
                  </TableRow>
                ) : (
                  doctors.map((doctor) => (
                    <TableRow key={doctor.id}>
                      <TableCell>{doctor.name}</TableCell>
                      <TableCell>{doctor.createdAt}</TableCell>
                      <TableCell>{doctor.deletedAt}</TableCell>
                      <TableCell className="text-right space-x-2">
                        <Button variant="outline" size="sm" onClick={() => handleEditDoctor(doctor)}>
                          수정
                        </Button>
                        {doctor.deletedAt ? (
                          <Button
                            variant="secondary"
                            size="sm"
                            onClick={() => handleRecover(doctor.id, doctor.name)}
                            disabled={recoveringDoctorId === doctor.id}
                          >
                            {recoveringDoctorId === doctor.id ? '복구중...' : '복구'}
                          </Button>
                        ) : (
                          <Button
                            variant="destructive"
                            size="sm"
                            onClick={() => handleDelete(doctor.id, doctor.name)}
                            disabled={deletingDoctorId === doctor.id}
                          >
                            {deletingDoctorId === doctor.id ? '삭제중...' : '삭제'}
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
      {/* 의사 수정 모달 */}
      <Dialog open={isEditDoctorModalOpen} onOpenChange={setIsEditDoctorModalOpen}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>의사 정보 수정</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="editDoctorName">의사 이름</Label>
              <Input
                id="editDoctorName"
                value={editDoctorName}
                onChange={(e) => setEditDoctorName(e.target.value)}
                placeholder="의사 이름을 입력하세요"
              />
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">취소</Button>
            </DialogClose>
            <Button onClick={handleUpdateDoctor} disabled={isUpdatingDoctor}>
              {isUpdatingDoctor ? '수정중...' : '수정'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
