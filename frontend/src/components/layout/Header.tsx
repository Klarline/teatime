import { Link } from 'react-router-dom';
import { Search, User } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';

export default function Header() {
  const { user } = useAuthStore();

  return (
    <header className="fixed top-0 left-0 right-0 bg-white border-b border-gray-200 z-50">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="text-2xl font-serif font-bold text-primary-500">
          TeaTime
        </Link>

        {/* Right side - Search and Profile */}
        <div className="flex items-center gap-4">
          <button className="p-2 hover:bg-gray-100 rounded-full">
            <Search className="w-5 h-5 text-gray-600" />
          </button>
          
          <Link to="/profile" className="flex items-center gap-2">
            {user?.icon ? (
              <img
                src={user.icon}
                alt={user.nickName}
                className="w-8 h-8 rounded-full"
              />
            ) : (
              <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                <User className="w-5 h-5 text-primary-600" />
              </div>
            )}
          </Link>
        </div>
      </div>
    </header>
  );
}