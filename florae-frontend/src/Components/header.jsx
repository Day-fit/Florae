import Button from './button.jsx';
import { GiHamburgerMenu } from 'react-icons/gi';
import temp from '../assets/temp.png';
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

export default function Header({ user, handleLogIn }) {
  return (
    <header className="flex flex-row border-b-stone-200 border-b-2 font-bold">
      <div className="flex flex-row items-center w-full ml-5 ">
        {/* Left: Logo/brand image */}
        <img src={temp} alt="" className="mt-5 mb-5 h-10 w-10" />
      </div>
      <div className="flex flex-row items-center justify-center w-full">
        {/* Center: Main navigation/actions */}
        <Button
          buttonText="Home"
          className="mt-5 mb-5  text-green-700 hover:text-green-900 active:text-green-900 rounded transition-colors duration-150"
        />
        <Button
          buttonText="Plants"
          className="mt-5 mb-5 ml-5 mr-5 text-green-700 hover:text-green-900 active:text-green-900 rounded transition-colors duration-150"
        />
        <Button
          buttonText="Devices"
          className="mt-5 mb-5 text-green-700 hover:text-green-900 active:text-green-900 rounded transition-colors duration-150"
        />
      </div>
      <div className="flex flex-row items-center w-full justify-end mr-5">
        {/* Right: Utility/profile buttons */}
        {user.isLogged ? (
          <Button icon={<GiHamburgerMenu className="w-6 h-6 mt-5 mb-5" />} />
        ) : (
          <>
            <Button
              buttonText="Sign in"
              className="mt-5 mb-5 rounded-full bg-gray-200 text-green-700 pl-4 pr-4 pb-2 pt-2"
              handleLogIn={handleLogIn}
            />
            <Button
              buttonText="Register"
              className="ml-5 mt-5 mb-5 rounded-full bg-green-700 text-white pl-4 pr-4 pb-2 pt-2"
              handleLogIn={handleLogIn}
            />
          </>
          //i need to add something
        )}
      </div>
    </header>
  );
}

/*
Next Steps / ToDos:
- Implement responsive header with hamburger menu for mobile.
- Add a logo or relevant image to the left section.
- Integrate login/register forms as modals (potentially with React portals).
- Decide between useState (local) or useContext (global) for authentication state management, adapting as app grows.
- Add appropriate labels/props for all Button components.
- Prioritize accessibility improvements.
*/
