/**
 * AnimatedModal is a React component that provides a simple modal
 * container with an entrance animation. When the component mounts,
 * it smoothly transitions from a slightly scaled-down, transparent
 * state to its full size and opacity.
 *
 * Props:
 * - children (React.Node): Elements to render inside the modal.
 *
 * Behavior:
 * - On mount, the modal animates from scale-95 and opacity-0 to
 *   scale-100 and opacity-100 using Tailwind CSS utility classes.
 * - The "transition-all" and "duration-300" classes control the
 *   smoothness and speed of the animation.
 *
 * Usage:
 * ```
 * <AnimatedModal>
 *   <div>Your modal content here.</div>
 * </AnimatedModal>
 * ```
 *
 * Note:
 * - This modal does not include logic for closing or backdrop
 *   management. It is intended for use as a wrapper for animated
 *   modal presentations.
 */

import { useState, useEffect } from 'react';

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
