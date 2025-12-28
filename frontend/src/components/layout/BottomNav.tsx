import { useNavigate, useLocation } from 'react-router-dom';
import { Home, Search, PlusCircle, Heart, User } from 'lucide-react';

export const BottomNav = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    if (path === '/profile') {
      return location.pathname === '/profile' || location.pathname.startsWith('/profile/');
    }
    return location.pathname.startsWith(path);
  };

  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.05)] z-50">
      <div className="max-w-md mx-auto flex justify-around items-center h-16 px-4">
        <button
          onClick={() => navigate('/')}
          className={`flex flex-col items-center gap-1 py-2 transition-colors ${
            isActive('/') ? 'text-primary-500' : 'text-gray-400'
          }`}
        >
          <Home size={24} strokeWidth={isActive('/') ? 2.5 : 2} />
          <span className="text-[10px] font-medium">Home</span>
        </button>

        <button
          onClick={() => navigate('/explore')}
          className={`flex flex-col items-center gap-1 py-2 transition-colors ${
            isActive('/explore') ? 'text-primary-500' : 'text-gray-400'
          }`}
        >
          <Search size={24} strokeWidth={isActive('/explore') ? 2.5 : 2} />
          <span className="text-[10px] font-medium">Explore</span>
        </button>

        <button
          onClick={() => navigate('/create')}
          className={`flex flex-col items-center gap-1 py-2 transition-colors ${
            isActive('/create') ? 'text-primary-500' : 'text-gray-400'
          }`}
        >
          <PlusCircle size={28} strokeWidth={isActive('/create') ? 2.5 : 2} />
          <span className="text-[10px] font-medium">Create</span>
        </button>

        <button
          onClick={() => navigate('/feed')}
          className={`flex flex-col items-center gap-1 py-2 transition-colors ${
            isActive('/feed') ? 'text-primary-500' : 'text-gray-400'
          }`}
        >
          <Heart size={24} strokeWidth={isActive('/feed') ? 2.5 : 2} />
          <span className="text-[10px] font-medium">Feed</span>
        </button>

        <button
          onClick={() => navigate('/profile')}
          className={`flex flex-col items-center gap-1 py-2 transition-colors ${
            isActive('/profile') ? 'text-primary-500' : 'text-gray-400'
          }`}
        >
          <User size={24} strokeWidth={isActive('/profile') ? 2.5 : 2} />
          <span className="text-[10px] font-medium">Profile</span>
        </button>
      </div>
    </nav>
  );
};