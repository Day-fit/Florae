import Input from './input.jsx';
import Button from './button.jsx';
import { UserContext } from '../store/user-context.jsx';
import AnimatedModal from './animated-modal.jsx';
import useAuthHandlers from './logging-functions.jsx';
import { registerFields, loginFields } from '../util/data.js';
import rainyNature from '../assets/rainyNature.mp4';
import { use, useEffect } from 'react';
import { UiContext } from '../store/ui-context.jsx';
import CloseButton, { errorClass, noErrorClass, baseInputClass } from './close-button.jsx';
/**
 * =====================================
 *        TODOs FOR AUTH COMPONENTS
 * =====================================
 *
 * - [ ] Optional: Add remember-me and forgot-password functionality
 *
 */

/**
 * AuthModal provides an authentication interface for users to log in or register.
 * It includes animated transitions, field validation, and contextual switching between login and registration forms.
 *
 * @component
 * @example
 * return (
 *   <AuthModal
 *     register={true}
 *     onClose={() => console.log('Modal closed')}
 *   />
 * )
 *
 * @param {Object} props - Component props
 * @param {boolean} props.register - Determines whether to show the registration or login form
 * @param {Function} props.onClose - Callback to close the modal
 * @returns {JSX.Element} A modal for user authentication (sign-in or sign-up)
 */

export default function AuthModal({ register, onClose }) {
  const { setModal, modal } = use(UiContext);
  const { logIn } = use(UserContext);

  const {
    formData,
    errors,
    handleSignIn,
    handleRegister,
    handleFormInput,
    clearErrors,
    clearFormData,
  } = useAuthHandlers({ logIn, onClose, setModal });

  useEffect(() => {
    if (modal) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [modal]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <video
        disablePictureInPicture
        src={rainyNature}
        className="absolute inset-0 w-full h-full object-cover z-0"
        autoPlay
        loop
        muted
        playsInline
        aria-hidden
      />
      <div className="absolute inset-0 bg-black/50" />
      <AnimatedModal>
        <div className="z-10 bg-white/90 rounded-xl p-4 sm:p-6 md:p-8 w-full max-w-lg flex flex-col items-center shadow-lg mx-2 overflow-y-auto max-h-[95vh]">
          <h2 className="mb-4 text-lg sm:text-xl md:text-2xl font-bold text-green-700 text-center w-full">
            {register ? 'Sign up' : 'Sign in'}
          </h2>
          {register ? (
            <form onSubmit={handleRegister} className="w-full space-y-4">
              {registerFields.map((field) => (
                <Input
                  key={field.name}
                  label={field.label}
                  type={field.type}
                  placeholder={field.label}
                  errorMsg={errors[field.name] || ''}
                  value={formData[field.name] || ''}
                  onChange={(e) => handleFormInput(e, field.name)}
                  className={`${baseInputClass} ${errors[field.name] ? errorClass : noErrorClass}`}
                  autoComplete={
                    field.name === 'username'
                      ? 'username'
                      : field.name === 'password'
                        ? 'new-password'
                        : 'email'
                  }
                />
              ))}
              <div className="w-full text-center mt-1">
                <Button
                  type="button"
                  className="text-green-700 hover:text-green-800 text-sm"
                  onClick={() => setModal('login')}
                  buttonText="Already have an account? Sign in"
                />
              </div>
              <div className="flex flex-col sm:flex-row gap-4 mt-6 w-full">
                <div className="flex-1 flex justify-center sm:justify-start">
                  <Button
                    buttonText="Login"
                    type="submit"
                    className="min-w-[200px] text-white bg-green-700 text-center flex items-center justify-center rounded-lg py-2.5"
                  />
                </div>
                <div className="flex justify-center sm:justify-end">
                  <CloseButton onClick={onClose} className="min-w-[100px]" />
                </div>
              </div>
            </form>
          ) : (
            <form onSubmit={handleSignIn} className="w-full space-y-4">
              {loginFields.map((field) => (
                <Input
                  key={field.name}
                  label={field.label}
                  type={field.type}
                  placeholder={field.label}
                  errorMsg={errors[field.name] || ''}
                  value={formData[field.name] || ''}
                  onChange={(e) => handleFormInput(e, field.name)}
                  className={`${baseInputClass} ${errors[field.name] ? errorClass : noErrorClass}`}
                  autoComplete={
                    field.name === 'username'
                      ? 'username'
                      : field.name === 'password'
                        ? 'current-password'
                        : 'email'
                  }
                />
              ))}
              <div className="w-full text-center mt-1">
                <Button
                  type="button"
                  className="text-green-700 hover:text-green-800 text-sm"
                  onClick={() => {
                    setModal('register');
                    clearErrors();
                    clearFormData();
                  }}
                  buttonText="Don't have an account? Sign up"
                />
              </div>
              <div className="flex flex-col sm:flex-row gap-4 mt-6 w-full">
                <div className="flex-1 flex justify-center sm:justify-start">
                  <Button
                    buttonText="Sign in"
                    type="submit"
                    className="min-w-[200px] text-white bg-green-700 text-center flex items-center justify-center rounded-lg py-2.5"
                  />
                </div>
                <div className="flex justify-center sm:justify-end">
                  <CloseButton onClick={onClose} className="min-w-[100px]" />
                </div>
              </div>
            </form>
          )}
        </div>
      </AnimatedModal>
    </div>
  );
}
