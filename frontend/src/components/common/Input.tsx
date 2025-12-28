import type { InputHTMLAttributes, ReactNode } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  icon?: ReactNode;
}

export const Input = ({ 
  label, 
  icon, 
  className = '',
  ...props 
}: InputProps) => (
  <div className="flex flex-col gap-1 w-full relative">
    {label && (
      <label className="text-sm font-medium text-gray-700">
        {label}
      </label>
    )}
    <input
      className={`w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-500 transition-all bg-gray-50 ${className}`}
      {...props}
    />
    {icon && (
      <div className="absolute right-3 top-3.5 text-gray-400">
        {icon}
      </div>
    )}
  </div>
);