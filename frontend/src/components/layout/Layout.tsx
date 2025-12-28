import { Outlet, Navigate } from 'react-router-dom';
import Header from './Header';
import { BottomNav } from './BottomNav';
import { useAuthStore } from '@/store/authStore';

export default function Layout() {
  const { isAuthenticated } = useAuthStore();

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <Header />

      {/* Main content - with padding for header and bottom nav */}
      <main className="pt-16 pb-20">
        <Outlet />
      </main>

      {/* Bottom Navigation */}
      <BottomNav />
    </div>
  );
}