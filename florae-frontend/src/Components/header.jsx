import { use } from 'react';
import Button from './button.jsx';
import AuthModal from './auth-modal.jsx';
import UserMenu from './user-menu.jsx';
import { UserContext } from '../store/user-context.jsx';
import PortalComponent from './portal-component.jsx';
import { navButtons } from '../util/data.js';
import { GiHamburgerMenu } from 'react-icons/gi';
import smallerLogo from '../assets/smaller-logo.png'
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

export default function Header({setModal, modal}) {
  const { isLogged } = use(UserContext);

  function handleAuth(action) {
    setModal(action);
  }
  function handleClose() {
    setModal(null);
  }

  function handleUserMenu(){
    setModal(true);
  }

  return (
    <header className="flex flex-row border-b-stone-200 border-b-2 font-bold">
      <div className="flex flex-row items-center w-full ml-5 ">
        {/* Left: Logo/brand image */}
        <img src={smallerLogo} alt="" className="mt-5 mb-5 h-10 w-10" />
      </div>
      <div className="flex flex-row items-center justify-center w-full gap-6">
        {/* Center: Main navigation/actions */}
        {navButtons.map(({ name, buttonText }) => (
          <Button
            key={name}
            buttonText={buttonText}
            className="mt-5 mb-5  text-green-700 hover:text-green-900 active:text-green-900 rounded transition-colors duration-150"
          />
        ))}
      </div>
      <div className="flex flex-row items-center w-full justify-end mr-5">
        {/* Right: Utility/profile buttons */}
        {isLogged ? (
          <Button icon={<GiHamburgerMenu className="w-6 h-6 mt-5 mb-5" onClick={handleUserMenu} />} />
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
