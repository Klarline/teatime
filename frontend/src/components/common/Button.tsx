import type { ButtonHTMLAttributes, ReactNode } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: 'primary' | 'outline' | 'ghost';
  className?: string;
}

export const Button = ({ 
  children, 
  variant = 'primary', 
  className = '', 
  disabled,
  type = 'button',
  ...props 
}: ButtonProps) => {
  const baseStyle = "px-4 py-2 rounded-lg font-medium transition-all active:scale-95 disabled:opacity-50 disabled:scale-100";
  
  const variants = {
    primary: "bg-primary-500 text-white shadow-md hover:shadow-lg hover:bg-primary-600",
    outline: "border-2 border-primary-500 text-primary-500 bg-transparent hover:bg-primary-50",
    ghost: "bg-transparent text-gray-600 hover:bg-gray-50"
  };

  return (
    <button 
      type={type}
      className={`${baseStyle} ${variants[variant]} ${className}`}
      disabled={disabled}
      {...props}
    >
      {children}
    </button>
  );
};