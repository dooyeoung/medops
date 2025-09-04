import { Link } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function HomePage() {
  return (
    <div className="">
      <div className="container mx-auto px-4 py-16">
        <div className="text-center mb-16">
          <h1 className="text-2xl font-bold text-foreground mb-4">MedOps</h1>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            병원 예약과 관리가 쉬워지는 통합 의료 CRM 시스템
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          <Card className="text-center hover:shadow-lg transition-shadow">
            <CardHeader>
              <CardTitle className="text-2xl mb-2">🏥</CardTitle>
              <CardTitle>병원 검색</CardTitle>
            </CardHeader>
            <CardContent>
              <CardDescription className="text-base">지역별, 진료과목별로 병원을 쉽게 찾아보세요</CardDescription>
            </CardContent>
          </Card>

          <Card className="text-center hover:shadow-lg transition-shadow">
            <CardHeader>
              <CardTitle className="text-2xl mb-2">📅</CardTitle>
              <CardTitle>간편 예약</CardTitle>
            </CardHeader>
            <CardContent>
              <CardDescription className="text-base">온라인으로 빠르고 간편하게 진료 예약을 신청하세요</CardDescription>
            </CardContent>
          </Card>

          <Card className="text-center hover:shadow-lg transition-shadow">
            <CardHeader>
              <CardTitle className="text-2xl mb-2">👨‍⚕️</CardTitle>
              <CardTitle>관리 시스템</CardTitle>
            </CardHeader>
            <CardContent>
              <CardDescription className="text-base">병원 관리자를 위한 통합 예약 및 환자 관리 시스템</CardDescription>
            </CardContent>
          </Card>
        </div>

        <div className="text-center mt-16">
          <p className="text-sm text-gray-500">
            이미 계정이 있으신가요?{' '}
            <Link to="/login" className="text-blue-600 hover:underline">
              로그인
            </Link>{' '}
            또는{' '}
            <Link to="/admin/login" className="text-green-600 hover:underline">
              관리자 로그인
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
