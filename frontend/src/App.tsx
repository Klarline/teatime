import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { Home } from './pages/Home';
import { Login } from './pages/Login';
import { Explore } from './pages/Explore';
import { ShopDetail } from './pages/ShopDetail';
import { BlogFeed } from './pages/BlogFeed';
import { BlogDetail } from './pages/BlogDetail';
import { CreatePost } from './pages/CreatePost';
import { Profile } from './pages/Profile';
import { BottomNav } from './components/layout/BottomNav';
import { useAuthStore } from './store/authStore';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

const MainLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <>
      <div className="max-w-md mx-auto bg-white min-h-screen pb-20">
        {children}
      </div>
      <BottomNav />
    </>
  );
};

const SimpleLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <div className="max-w-md mx-auto bg-white min-h-screen">
      {children}
    </div>
  );
};

function App() {
  return (
    <BrowserRouter>
      {/* Toast Notifications */}
      <Toaster 
        position="top-center"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#363636',
            color: '#fff',
            fontSize: '14px',
            padding: '12px 20px',
            borderRadius: '12px',
          },
          success: {
            iconTheme: {
              primary: '#4ade80',
              secondary: '#fff',
            },
          },
          error: {
            iconTheme: {
              primary: '#ef4444',
              secondary: '#fff',
            },
          },
        }}
      />

      <Routes>
        <Route path="/login" element={<SimpleLayout><Login /></SimpleLayout>} />
        <Route path="/" element={<MainLayout><Home /></MainLayout>} />
        <Route path="/explore" element={<MainLayout><Explore /></MainLayout>} />
        <Route path="/shop/:id" element={<SimpleLayout><ShopDetail /></SimpleLayout>} />
        <Route path="/feed" element={<MainLayout><BlogFeed /></MainLayout>} />
        
        <Route path="/blog/:id" element={
          <ProtectedRoute><SimpleLayout><BlogDetail /></SimpleLayout></ProtectedRoute>
        } />
        
        <Route path="/create" element={
          <ProtectedRoute><SimpleLayout><CreatePost /></SimpleLayout></ProtectedRoute>
        } />
        
        <Route path="/profile/:id" element={
          <ProtectedRoute><MainLayout><Profile /></MainLayout></ProtectedRoute>
        } />
        
        <Route path="/profile" element={
          <ProtectedRoute><MainLayout><Profile /></MainLayout></ProtectedRoute>
        } />
        
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;