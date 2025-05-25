import Input from './input.jsx';
import Button from './button.jsx';
import { UserContext } from '../store/user-context.jsx';
import AnimatedModal from "./animated-modal.jsx";
import validateForm from '../util/form-validiation.js';
import { registerFields, loginFields } from "../util/data.js";
import rainyNature from '../assets/rainyNature.mp4';
import axios from "axios";
import { useState, use } from 'react';
/**
 * =====================================
 *        TODOs FOR AUTH COMPONENTS
 * =====================================
 * - [ ] Add input validation for all forms:
 *       • Password strength (min length, etc.)
 *       • Matching passwords on registration
 *       • Display helpful error messages
 *
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
 */

export default function AuthOverlay({ register, onClose }) {
  const { logIn } = use(UserContext);

  const [errors, setErrors] = useState({
    email: '',
    login: '',
    password: '',
  });
  const [formData, setFormData] = useState({
    email: '',
    login: '',
    password: '',
  });

  const errorClass = 'border-red-500 bg-red-50 text-red-700 placeholder-red-400 focus:border-red-600';
  const noErrorClass = "bg-white border-stone-200 text-stone-700 placeholder-stone-400 focus:border-green-600"
  const baseInputClass =
    "block w-full border rounded-lg px-4 py-2 focus:outline-none  transition";

  const closeButton = (
    <Button
      buttonText="Cancel"
      className="w-full text-green-700 bg-gray-200 text-center max-w-md rounded-lg pt-3 pb-3 px-15"
      onClick={onClose}
    />
  );


  //I need to fix this function later when Dayfit tells me every detail about CORS errors that occurs rn

  async function handleSubmit(e) {
    e.preventDefault();
    // 1. Validate form and update errors state
    const validationErrors = await validateForm(formData, register ? 'register' : 'login');
    setErrors(validationErrors);

    // 2. If any errors, don't submit
    // (validationErrors is object of error messages; if any value is non-empty, it's invalid)
    const hasErrors = Object.values(validationErrors).some((v) => v);
    if (hasErrors) return;

    // 3. Submit form using axios
    try {
      const response = await axios.post(
          "https://florae.dayfit.pl",
          {
            email: formData.email,
            login: formData.login,
            password: formData.password,
          }
      );
      console.log("Submission successful:", response.data);
      logIn(formData);
      onClose();
      setFormData({email: '', login: '', password: ''})
    } catch (err) {
      // Show a generic error or display err.response?.data?.message if your backend returns nice errors
      setErrors((prev) => ({
        ...prev,
        form: "Something went wrong. Please try again.",
      }));
      console.error("Submission error:", err);
    }
  }

  function handleFormInput(e, name){
    setFormData((prev) => ({
      ...prev,
      [name]: e.target.value,
    }))
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      {/* Video background */}
      <video
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
          {register === 'register' ? 'Sign up' : 'Sign in'}
        </h2>
        {register ? (
          <form onSubmit={handleSubmit}>
            {registerFields.map((field) => (
                <Input
                    key={field.name}
                    label={field.label}
                    type={field.type}
                    value={formData[field.name] || ''}
                    onChange={e => handleFormInput(e, field.name)}
                    className={`${baseInputClass} ${errors[field.name] ? errorClass : noErrorClass}`}
                />
            ))}
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
          <form onSubmit={handleSubmit}>
            {loginFields.map((field) => (
                <Input
                    key={field.name}
                    label={field.label}
                    type={field.type}
                    value={formData[field.name] || ''}
                    onChange={(e) => handleFormInput(e, field.name)}
                    className={`${baseInputClass} ${errors[field.name] ? errorClass : noErrorClass}`}
                />
            ))}
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
        )}
      </div>
      </AnimatedModal>
    </div>
  );
}
