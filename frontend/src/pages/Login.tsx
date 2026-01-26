import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Coffee } from 'lucide-react';
import toast from 'react-hot-toast';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { useAuthStore } from '@/store/authStore';
import { authApi } from '@/api/auth.api';

export const Login = () => {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [codeSent, setCodeSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [countdown]);

  const handleSendCode = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!phone) {
      toast.error('Please enter a phone number');
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await authApi.sendCode(phone);
      
      if (response.success) {
        setCodeSent(true);
        setCountdown(60);
        toast.success('Verification code sent!');
      } else {
        toast.error(response.errorMsg || 'Failed to send code');
      }
    } catch (error) {
      console.error('Failed to send code:', error);
      const errorMessage = error instanceof Error ? error.message : 'Failed to send verification code';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!phone || !code) {
      toast.error('Please enter phone number and code');
      return;
    }
    
    setLoading(true);
    
    try {
      await login(phone, code);
      toast.success('Login successful!');
      navigate('/', { replace: true });
    } catch (error) {
      console.error('Login failed:', error);
      const errorMessage = error instanceof Error ? error.message : 'Invalid verification code';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white flex flex-col items-center justify-center p-8">
      {/* Logo */}
      <div className="w-20 h-20 rounded-2xl bg-primary-500 bg-opacity-10 flex items-center justify-center mb-6">
        <Coffee size={40} className="text-primary-500" />
      </div>

      {/* Title */}
      <h1 className="text-3xl font-serif font-bold text-gray-800 mb-2">TeaTime</h1>
      <p className="text-gray-500 mb-10 text-center max-w-xs">
        Enter your mobile number to login or sign up automatically.
      </p>

      {/* Form */}
      <form 
        onSubmit={(e) => e.preventDefault()}
        className="w-full max-w-sm space-y-5"
      >
        <Input 
          placeholder="Phone Number" 
          value={phone} 
          onChange={(e) => setPhone(e.target.value)}
          type="tel"
        />

        <div className="flex gap-2">
          <div className="flex-1">
            <Input 
              placeholder="Verification Code" 
              value={code} 
              onChange={(e) => setCode(e.target.value)}
              type="text"
            />
          </div>
          <button 
            type="button"
            disabled={countdown > 0 || loading || !phone}
            onClick={handleSendCode}
            className="px-4 rounded-xl border border-primary-500 text-primary-500 font-medium text-sm disabled:opacity-50 disabled:border-gray-300 disabled:text-gray-400 whitespace-nowrap transition-colors hover:bg-green-50 shadow-sm"
          >
            {loading ? 'Sending...' : countdown > 0 ? `${countdown}s` : codeSent ? 'Resend' : 'Send Code'}
          </button>
        </div>

        <Button 
          type="button"
          className="w-full py-3.5 mt-4 shadow-lg shadow-green-900/10" 
          onClick={handleLogin} 
          disabled={loading || !code || !phone}
        >
          {loading ? 'Logging in...' : 'Login / Sign Up'}
        </Button>
        
        <p className="text-center text-xs text-gray-400 mt-4 leading-relaxed">
          By continuing, you agree to our{' '}
          <span className="underline cursor-pointer hover:text-primary-500">Terms of Service</span>
          {' '}and{' '}
          <span className="underline cursor-pointer hover:text-primary-500">Privacy Policy</span>.
        </p>
      </form>
    </div>
  );
};