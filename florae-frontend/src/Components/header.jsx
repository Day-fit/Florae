import { use, useState } from 'react';
import Button from './button.jsx';
import AuthModal from './auth-modal.jsx';
import UserMenu from './user-menu.jsx';
import { UserContext } from '../store/user-context.jsx';
import PortalComponent from './portal-component.jsx';
import { navButtons } from '../util/data.js';
import { GiHamburgerMenu } from 'react-icons/gi';
import smallerLogo from '../assets/smaller-logo.png';
import { RxCross1 } from 'react-icons/rx';

/**
 * Header Component
 *
 * This component renders the application's main header:
 *  - Left: Logo/brand image
 *  - Center: Navigation/action buttons
 *  - Right: Utility or profile buttons
 *
 * Responsive Design (ToDo):
 *  - Convert button groups to a hamburger menu on small screens.
 *  - Reveal all buttons in a vertical stack when the menu is opened.
 *
 * Authentication TODOs and Feature Notes:
 *  - Plan to include user login/registration features.
 *    - Consider whether to handle authentication state using useState (for local state)
 *      or useContext (for global/shared state across components). Make decision based on
 *      app complexity and requirements as features develop.
 *    - Add form components to handle user login and registration processes.
 *    - For modals/dialogs, consider using React portals to render authentication forms
 *      outside the normal component hierarchy for better UX/flexibility.
 *    - Review best practices for security and form validation.
 *
 * Accessibility & Usability:
 *  - Add aria-labels and improve semantic structure.
 *  - Use meaningful props/labels for each Button.
 *  - Ensure responsive and keyboard-friendly navigation.
 */

export default function Header({ setModal, modal, changePage }) {
  const { isLogged } = use(UserContext);
  const [menuOpen, setMenuOpen] = useState(false);
  
  function handleAuth(action) {
    setModal(action);
    setMenuOpen(false);
  }
  function handleClose() {
    setModal(null);
    setMenuOpen(false);
  }

  function handleUserMenu() {
    setModal(true);
    setMenuOpen(false);
  }

  return (
    <header className="flex flex-row border-b-stone-200 border-b-2 font-bold">
      <div className="flex flex-row md:items-center w-full ml-5 ">
        {/* Left: Logo/brand image */}
        <img
          src={smallerLogo}
          alt=""
          className="mt-5 mb-5 h-10 w-10"
          onClick={() => changePage('home')}
        />
      </div>
      <div className="hidden md:flex flex flex-row items-center justify-center w-full gap-6">
        {/* Center: Main navigation/actions on PC */}
        {navButtons.map(({ name, buttonText }) => (
          <Button
            key={name}
            buttonText={buttonText}
            onClick={() => changePage(name)}
            className="mt-5 mb-5  text-green-700 hover:text-green-900 active:text-green-900 rounded transition-colors duration-150"
          />
        ))}
      </div>
      {menuOpen && (
        <div className="flex flex-col items-center gap-4 pb-4 md:hidden">
          {/* Center: Main navigation/actions and utility/profile buttons on mobile */}
          {navButtons.map(({ name, buttonText }) => (
            <Button
              key={name}
              buttonText={buttonText}
              onClick={() => {
                changePage(name);
                setMenuOpen(false);
              }}
              className="text-green-700 hover:text-green-900"
            />
          ))}

          {!isLogged ? (
            <>
              <Button
                buttonText="Sign in"
                className="text-green-700 hover:text-green-900"
                onClick={() => handleAuth('login')}
              />
              <Button
                buttonText="Register"
                className="text-green-700 hover:text-green-900"
                onClick={() => handleAuth('register')}
              />
            </>
          ) : (
            <>
              <Button
                buttonText="Settings"
                onClick={() => changePage('settings')}
                className="text-green-700 hover:text-green-900"
              />
              <Button
                buttonText="Log out"
                onClick={() => changePage('logout')}
                className="text-green-700 hover:text-green-900"
              />
            </>
          )}
        </div>
      )}
      <div className="hidden md:flex flex-row items-center w-full justify-end mr-5">
        {/* Right: Utility/profile buttons on PC */}
        {isLogged ? (
          <Button
            icon={<GiHamburgerMenu className="w-6 h-6 mt-5 mb-5" onClick={handleUserMenu} />}
          />
        ) : (
          <>
            <Button
              buttonText="Sign in"
              className="mt-5 mb-5 rounded-full bg-gray-200 text-green-700 pl-4 pr-4 pb-2 pt-2"
              onClick={() => handleAuth('login')}
            />
            <Button
              buttonText="Sign up"
              className="ml-5 mt-5 mb-5 rounded-full bg-green-700 text-white pl-4 pr-4 pb-2 pt-2"
              onClick={() => handleAuth('register')}
            />
          </>
        )}
        {modal && !isLogged && (
          <PortalComponent element="#auth-modal">
            <AuthModal register={modal === 'register'} setModal={setModal} onClose={handleClose} />
          </PortalComponent>
        )}
        {modal && isLogged && (
          <PortalComponent element="#auth-modal">
            <UserMenu onClose={handleClose} />
          </PortalComponent>
        )}
      </div>
      <div className="flex w-full justify-end items-start mt-5 mr-5 md:hidden">
        {/* Right: Button to open and close navigational menu on mobile */}
        <button onClick={() => setMenuOpen(!menuOpen)}>
          {menuOpen ? <RxCross1 className="w-6 h-6" /> : <GiHamburgerMenu className="w-6 h-6" />}
        </button>
      </div>
    </header>
  );
}

/*
Next Steps / ToDos:
- Implement responsive header with hamburger 'menu' for mobile.
- Add a logo or relevant image to the left section.
- Integrate login/register forms as modals (potentially with React portals).
- Decide between useState (local) or useContext (global) for authentication state management, adapting as app grows.
- Add appropriate labels/props for all Button components.
- Prioritize accessibility improvements.
*/
