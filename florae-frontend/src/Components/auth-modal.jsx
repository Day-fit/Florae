/**
 * AuthModal
 * Handles user registration and login forms.
 *
 * Considers splitting:
 *  - API request logic -> `util/auth-service.js`
 *  - Input change helpers -> `util/form-helpers.js`
 *  - Form field configs -> `util/data.js`
 *
 * This improves:
 *  - Readability: UI logic isn't mixed with API/functionality code
 *  - Maintainability: Helpers/services can be unit tested
 *  - Reusability: Helpers/services can be used in other auth flows
 */

import Input from './input.jsx';
import Button from './button.jsx';
import { UserContext } from '../store/user-context.jsx';
import AnimatedModal from './animated-modal.jsx';
import validateForm from '../util/form-validiation.js';
import { isEmail } from '../util/form-validiation.js';
import { registerFields, loginFields } from '../util/data.js';
import rainyNature from '../assets/rainyNature.mp4';
import axios from 'axios';
import { useState, use } from 'react';
/**
 * =====================================
 *        TODOs FOR AUTH COMPONENTS
 * =====================================
 * - [ ] On submit, send request to backend API:
 *       • Use fetch/axios to POST login or registration data
 *       • Show loading and handle errors (user exists, bad password, etc.)
 *       • Securely handle credentials (never store passwords in frontend)
 *
 * - [ ] Connect backend to a real database:
 *       • Use an API endpoint to create or fetch user data
 *       • Secure endpoints (e.g., JWT for sessions)
 *       • Receive response and store necessary info in app state/context
 *
 * - [ ] Carefully review all authentication flows for security and user experience!
 *
 * - [ ] Optional: Add remember-me and forgot-password functionality
 *
 *
 * - fix login
 */

export default function AuthModal({ register, onClose, setModal }) {
  const { logIn } = use(UserContext);

  const [errors, setErrors] = useState({
    email: '',
    username: '',
    password: '',
    form: '',
  });
  const [formData, setFormData] = useState({
    email: '',
    username: '',
    password: '',
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const userProp = !isEmail(formData.username)
    ? { username: formData.username }
    : { email: formData.email };

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

  async function handleSignIn(e) {
    e.preventDefault();
    console.log('is submitting:' + isSubmitting);

    if (isSubmitting) return;

    const validationErrors = await validateForm(formData, 'signIn');
    setErrors(validationErrors);
    const hasErrors = Object.values(validationErrors).some((v) => v);
    if (hasErrors) return;
    setIsSubmitting(true);

    try {
      const response = await axios.post(
        `/auth/login`,
        {
          ...userProp,
          password: formData.password,
          generateRefreshToken: true,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      console.log(response);
      console.log(response.data);
      logIn(formData); //later I need to change it because I can't store password in state
      setFormData({ email: '', username: '', password: '' });
      setModal(null);
      setIsSubmitting(false);
      onClose();
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        email: 'Wrong email or username',
        username: 'Wrong email or username',
        form: 'Wrong email or username',
      }));
      console.error('Submission error:', err);
      setIsSubmitting(false);
    }
  }

  async function handleRegister(e) {
    e.preventDefault();
    if (isSubmitting) return;

    const validationErrors = await validateForm(formData, 'register');
    setErrors(validationErrors);

    const hasErrors = Object.values(validationErrors).some((v) => v);
    if (hasErrors) return;
    setIsSubmitting(true);

    try {
      console.log('Submitting form: ', formData);
      const response = await axios.post(
        `/auth/register`,
        {
          username: formData.username,
          email: formData.email,
          password: formData.password,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      console.log(response);
      try {
        await handleSignIn(e);
      } catch (err) {
        console.log('error: ' + err);
      }
      setIsSubmitting(false);
      onClose();
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        email: 'Wrong email or username',
        username: 'Wrong email or username',
        form: 'Wrong email or username',
      }));
      console.error('Submission error:', err);
      setIsSubmitting(false);
    }
  }

  function handleFormInput(e, name) {
    const value = e.target.value;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

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
