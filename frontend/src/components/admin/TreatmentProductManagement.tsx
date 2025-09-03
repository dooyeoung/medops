import { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from '@/components/ui/dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import {
  getTreatmentProductsByHospital,
  createTreatmentProduct,
  updateTreatmentProduct,
  deleteTreatmentProduct,
  recoverTreatmentProduct,
} from '@/api/treatmentProduct';
import { Toaster, toast } from 'sonner';

function TreatmentProductForm({
  treatmentProduct,
  onSave,
}: {
  treatmentProduct?: TreatmentProduct | null;
  onSave: (data: Omit<TreatmentProduct, 'id'> | TreatmentProduct) => void;
}) {
  const [formData, setFormData] = useState({
    name: treatmentProduct?.name || '',
    description: treatmentProduct?.description || '',
    maxCapacity: treatmentProduct?.maxCapacity || 1,
    price: treatmentProduct?.price || 0,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'number' ? parseInt(value, 10) || 0 : value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const finalData = treatmentProduct ? { ...treatmentProduct, ...formData } : formData;
    onSave(finalData);
  };

  return (
    <form id="product-form" onSubmit={handleSubmit} className="grid gap-4 py-4">
      <div className="grid grid-cols-4 items-center gap-4">
        <Label htmlFor="name" className="text-right">
          상품명
        </Label>
        <Input id="name" name="name" value={formData.name} onChange={handleChange} className="col-span-3" />
      </div>
      <div className="grid grid-cols-4 items-center gap-4">
        <Label htmlFor="description" className="text-right">
          설명
        </Label>
        <Input
          id="description"
          name="description"
          value={formData.description}
          onChange={handleChange}
          className="col-span-3"
        />
      </div>
      <div className="grid grid-cols-4 items-center gap-4">
        <Label htmlFor="maxCapacity" className="text-right">
          시간당 인원
        </Label>
        <Input
          id="maxCapacity"
          name="maxCapacity"
          type="number"
          value={formData.maxCapacity}
          onChange={handleChange}
          className="col-span-3"
        />
      </div>
      <div className="grid grid-cols-4 items-center gap-4">
        <Label htmlFor="price" className="text-right">
          가격
        </Label>
        <Input
          id="price"
          name="price"
          type="number"
          value={formData.price}
          onChange={handleChange}
          className="col-span-3"
        />
      </div>
    </form>
  );
}

interface Props {
  hospitalId;
}

export default function TreatmentProductManagement({ hospitalId }: Props) {
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [treatmentProducts, setTreatmentProducts] = useState<TreatmentProduct[]>([]);
  const [editingTreatmentProduct, setEditingTreatmentProduct] = useState<TreatmentProduct | null>(null);

  useEffect(() => {
    fetchTreatmentProducts();
  }, [hospitalId]);

  const fetchTreatmentProducts = async () => {
    try {
      const productsResponse = await getTreatmentProductsByHospital(hospitalId);
      setTreatmentProducts(productsResponse.body || []);
    } catch (err: any) {
      console.error('Failed to fetch treatment products:', err);
    } finally {
    }
  };

  const openNewProductDialog = () => {
    setEditingTreatmentProduct(null);
    setIsDialogOpen(true);
  };

  const openEditProductDialog = (product: TreatmentProduct) => {
    setEditingTreatmentProduct(product);
    setIsDialogOpen(true);
  };

  const handleRecover = async (productId: string) => {
    try {
      await recoverTreatmentProduct(productId);
      fetchTreatmentProducts();
      toast.success('상품 복구 성공', {
        description: '상품을 성공적으로 복구했습니다',
      });
    } catch (err: any) {
      toast.error('상품 복구 실패', {
        description: '서버 오류입니다',
      });
    }
  };

  const handleDelete = async (productId: string) => {
    if (window.confirm('정말로 이 상품을 삭제하시겠습니까?')) {
      try {
        await deleteTreatmentProduct(productId);
        fetchTreatmentProducts();
        toast.success('상품 삭제 성공', {
          description: '상품을 성공적으로 삭제했습니다',
        });
      } catch (err: any) {
        toast.error('상품 삭제 실패', {
          description: '서버 오류입니다',
        });
      }
    }
  };

  const handleSave = async (data: Omit<TreatmentProduct, 'id'> | TreatmentProduct) => {
    try {
      if ('id' in data && data.id) {
        await updateTreatmentProduct(data.id, data);
      } else {
        if (!hospitalId) throw new Error('Hospital ID not available for creating product.');
        await createTreatmentProduct({ ...data, hospitalId });
      }
      fetchTreatmentProducts();
      setIsDialogOpen(false);
      toast.success('상품 정보 변경 성공', {
        description: '상품 정보를 성공적으로 변경하였습니다',
      });
    } catch (err: any) {
      toast.error('상품 정보 변경 실패', {
        description: '서버 오류입니다',
      });
    }
  };

  return (
    <>
      <Toaster expand={true} richColors position="top-center" />

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>병원 상품 관리</CardTitle>
              <CardDescription>요일별 영업시간과 점심시간을 설정하세요.</CardDescription>
            </div>
            <div className="flex justify-end pt-4">
              <Button onClick={openNewProductDialog}>상품 추가</Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>상품명</TableHead>
                <TableHead>설명</TableHead>
                <TableHead>시간당 인원</TableHead>
                <TableHead>가격</TableHead>
                <TableHead>등록일</TableHead>
                <TableHead>삭제일</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {treatmentProducts.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center">
                    등록된 상품이 없습니다.
                  </TableCell>
                </TableRow>
              ) : (
                treatmentProducts.map((treatmentProduct) => (
                  <TableRow key={treatmentProduct.id}>
                    <TableCell>{treatmentProduct.name}</TableCell>
                    <TableCell>{treatmentProduct.description}</TableCell>
                    <TableCell>{treatmentProduct.maxCapacity}명</TableCell>
                    <TableCell>{treatmentProduct.price}</TableCell>
                    <TableCell>{treatmentProduct.createdAt}</TableCell>
                    <TableCell>{treatmentProduct.deletedAt}</TableCell>
                    <TableCell className="text-right space-x-2">
                      <Button variant="outline" size="sm" onClick={() => openEditProductDialog(treatmentProduct)}>
                        수정
                      </Button>
                      {treatmentProduct.deletedAt ? (
                        <Button variant="secondary" size="sm" onClick={() => handleRecover(treatmentProduct.id)}>
                          복구
                        </Button>
                      ) : (
                        <Button variant="destructive" size="sm" onClick={() => handleDelete(treatmentProduct.id)}>
                          삭제
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingTreatmentProduct ? '상품 수정' : '새 상품 추가'}</DialogTitle>
          </DialogHeader>
          <TreatmentProductForm treatmentProduct={editingTreatmentProduct} onSave={handleSave} />
          <DialogFooter>
            <DialogClose asChild>
              <Button type="button" variant="secondary">
                취소
              </Button>
            </DialogClose>
            <Button type="submit" form="product-form">
              저장
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
