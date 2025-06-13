/**
 * AuthModal is a React component that presents authentication forms
 * (login and registration) within a modal dialog.
 *
 * Props:
 * - onClose (function): Callback when modal is dismissed.
 * - logIn (function): Callback after successful login.
 *
 * Behavior:
 * - Provides forms with validation and feedback.
 * - Communicates user authentication state to the parent.
 *
 * Usage:
 * ```
 * <AuthModal onClose={handleClose} logIn={handleLogin} />
 * ```
 *
 * Note:
 * - Focuses on frontend authentication workflow only.
 * - Does not manage device connections or external integrations.
 */

import Input from './input.jsx';
import Button from './button.jsx';
import { UserContext } from '../store/user-context.jsx';
import AnimatedModal from './animated-modal.jsx';
import useAuthHandlers from '../util/logging-functions.jsx';
import { registerFields, loginFields } from '../util/data.js';
import rainyNature from '../assets/rainyNature.mp4';
import { use } from 'react';
import { UiContext } from '../store/ui-context.jsx';
/**
 * =====================================
 *        TODOs FOR AUTH COMPONENTS
 * =====================================
 *
 * - [ ] Optional: Add remember-me and forgot-password functionality
 *
 */

export default function AuthModal({ register, onClose }) {
  const { setModal } = use(UiContext);
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

  const errorClass =
    'border-red-500 bg-red-50 text-red-700 placeholder-red-400 focus:border-red-600';
  const noErrorClass =
    'bg-white border-stone-200 text-stone-700 placeholder-stone-400 focus:border-green-600';
  const baseInputClass = 'block w-full border rounded-lg px-4 py-2 focus:outline-none  transition';

  const closeButton = (
    <Button
      buttonText="Cancel"
      className="w-full text-green-700 bg-gray-200 text-center max-w-md rounded-lg pt-3 pb-3 px-15"
      onClick={onClose}
    />
  );

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
        <div className="z-10 bg-white/90 rounded-xl p-10 max-w-lg w-full flex flex-col items-center shadow-lg mx-2">
          <h2 className="mb-6 text-2xl font-bold text-green-700">
            {register ? 'Sign up' : 'Sign in'}
          </h2>
          {register ? (
            <form onSubmit={handleRegister}>
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
              <div className="w-full text-center mt-2 mb-2">
                <Button
                  type="button"
                  className="text-green-700 hover:text-green-800 text-sm"
                  onClick={() => setModal('login')}
                  buttonText="Already have an account? Sign in"
                />
              </div>
              <div className="w-full flex flex-col items-center mb-4 mt-4">
                {errors.form && (
                  <p className="text-red-600 text-lg font-bold text-center">{errors.form}</p>
                )}
              </div>
              <div className="flex flex-row justify-between mt-4 w-full">
                <div className="flex justify-start">
                  <Button
                    buttonText="Login"
                    type="submit"
                    className="max-w-lg text-white bg-green-700 text-center rounded-lg pt-2 pb-2 px-20"
                  />
                </div>
                <div className="flex justify-end">{closeButton}</div>
              </div>
            </form>
          ) : (
            <form onSubmit={handleSignIn}>
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
              <div className="w-full text-center mt-2 mb-2">
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
              <div className="w-full flex flex-col items-center mb-4 mt-4">
                {errors.form && (
                  <p className="text-red-600 text-lg font-bold text-center">{errors.form}</p>
                )}
              </div>
              <div className="flex flex-row justify-between mt-4 w-full">
                <div className="flex justify-start">
                  <Button
                    buttonText="Sign in"
                    type="submit"
                    className="max-w-lg text-white bg-green-700 text-center rounded-lg pt-2 pb-2 px-20"
                  />
                </div>
                <div className="flex justify-end">{closeButton}</div>
              </div>
            </form>
          )}
        </div>
      </AnimatedModal>
    </div>
  );
}
