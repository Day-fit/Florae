import { useEffect, useRef, use } from 'react';
import { userMenuButtons } from '../util/data.js';
import Button from './button.jsx';
import { FiSettings } from 'react-icons/fi';
import { UserContext } from '../store/user-context.jsx';
import useAuthHandlers from './logging-functions.jsx';

export default function UserMenu({ onClose, open = true }) {
  const { handleLogout } = useAuthHandlers({ onClose });
  const { logOut } = use(UserContext);
  const menuRef = useRef(null);

  useEffect(() => {
    function handleClick(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        onClose?.();
      }
    }
    function handleKey(e) {
      if (e.key === 'Escape') {
        onClose?.();
      }
    }
    if (open) {
      document.addEventListener('mousedown', handleClick);
      document.addEventListener('keydown', handleKey);
    }
    return () => {
      document.removeEventListener('mousedown', handleClick);
      document.removeEventListener('keydown', handleKey);
    };
  }, [onClose, open]);

  return (
    <div className="fixed inset-0 z-100" aria-modal="true" tabIndex={-1} role="dialog">
      {/* Overlay for click-outside */}
      <div className="absolute inset-0 bg-transparent" />
      <div
        ref={menuRef}
        className="fixed top-16 right-6 w-62 bg-stone-200 rounded-2xl shadow-lg border border-stone-100 pt-2 pb-2 px-0 flex flex-col space-y-2 animate-fade-in"
        tabIndex={0}
        role="menu"
        aria-label="Settings"
      >
        <div className="flex flex-col w-full">
          {userMenuButtons.map(({ buttonsText, name, icon }) => (
            <Button
              buttonText={buttonsText}
              key={name}
              icon={
                icon ? (
                  <FiSettings
                    className="mr-2 w-5 h-5"
                    aria-hidden="true"
                    style={{ strokeWidth: 3 }}
                  />
                ) : undefined
              }
              className="w-full pt-5 pb-5 text-left text-[#3B1F0B] font-bold rounded-none px-6 transition-colors group
                hover:bg-green-100 hover:rounded-2xl focus:bg-green-100 focus:rounded-2xl
                outline-none"
              tabIndex={0}
              onClick={name === 'logout' ? () => handleLogout() : undefined}
              role="menuitem"
            />
          ))}
        </div>
      </div>
    </div>
  );
}
