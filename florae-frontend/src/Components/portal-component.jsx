import { createPortal } from 'react-dom';

export default function PortalComponent({ children, element }) {
  const target = document.querySelector(element);
  if (!target) return null; // Defensive: only render if present
  return createPortal(children, target);
}
