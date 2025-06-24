import Button from './button.jsx';

export default function CloseButton({ onClick, buttonText = 'Cancel', className = '' }) {
  return (
    <Button
      buttonText={buttonText}
      className={`w-full text-green-700 bg-gray-200 text-center max-w-112 rounded-lg pt-3 pb-3 px-15 justify-center ${className}`}
      onClick={onClick}
    />
  );
}

export const errorClass =
  'border-red-500 bg-red-50 text-red-700 placeholder-red-400 focus:border-red-600';
export const noErrorClass =
  'bg-white border-stone-200 text-stone-700 placeholder-stone-400 focus:border-green-600';
export const baseInputClass =
  'block w-full border rounded-lg px-4 py-2 focus:outline-none  transition';
