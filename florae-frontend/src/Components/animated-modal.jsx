import { useState, useEffect } from 'react';

// `children` is your modal content with all your own classes
export default function AnimatedModal({ children }) {
  const [show, setShow] = useState(false);

  useEffect(() => {
    setShow(true);
  }, []);

  return (
    <div
      className={`transition-all duration-300 
                  ${show ? 'scale-100 opacity-100' : 'scale-95 opacity-0'}`}
      style={{ willChange: 'opacity, transform' }}
    >
      {children}
    </div>
  );
}
