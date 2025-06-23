import { useState, useEffect } from 'react';

/**
 * AnimatedModal is a simple wrapper component that fades and scales
 * its children in when the component mounts. It uses Tailwind CSS transitions.
 *
 * @component
 * @example
 * return (
 *   <AnimatedModal>
 *     <div className="bg-white p-4 rounded-lg shadow-md">
 *       <h2>Hello!</h2>
 *       <p>This modal animates in on mount.</p>
 *     </div>
 *   </AnimatedModal>
 * )
 *
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - The content to display inside the modal
 * @returns {JSX.Element} Animated modal wrapper
 */

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
