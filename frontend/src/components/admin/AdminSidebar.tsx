import { Link, useLocation } from 'react-router-dom';
import { Home, Hospital, User, Calendar, CalendarDays, UserPlus, Package, BarChart3 } from 'lucide-react';
import { cn } from '@/lib/utils';

const navLinks = [
  { href: '/admin/dashboard', label: '대시보드', icon: BarChart3 },
  { href: '/admin/schedule', label: '스케줄 관리', icon: Calendar },
  { href: '/admin/hospital-settings', label: '병원 관리', icon: Hospital },
  { href: '/admin/invite', label: '관리자 초대', icon: UserPlus },
];

export default function AdminSidebar() {
  const location = useLocation();

  return (
    <aside className="w-48 flex-shrink-0 border-r bg-gray-100/40 p-4">
      <div className="flex h-full max-h-screen flex-col gap-2">
        <div className="flex-1">
          <nav className="grid items-start px-2 text-sm font-medium lg:px-4">
            {navLinks.map(({ href, label, icon: Icon }) => (
              <Link
                key={label}
                to={href}
                className={cn(
                  'flex items-center gap-3 rounded-lg px-3 py-2 text-muted-foreground transition-all hover:text-primary',
                  location.pathname === href && 'bg-muted text-primary',
                )}
              >
                <Icon className="h-4 w-4" />
                {label}
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </aside>
  );
}
