import { useState } from 'react';
import axios from './axiosClient.js';
import validateForm, { isEmail } from './form-validiation.js';

export default function useAuthHandlers({ logIn, onClose, setModal }) {
  const [errors, setErrors] = useState({
    email: '',
    username: '',
    password: '',
  });
  const [formData, setFormData] = useState({
    email: '',
    username: '',
    password: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  async function getCsrfToken()
  {
    const response = await axios.get('/csrf', { withCredentials: true });
    return response.data.token;
  }

  async function handleSignIn(e) {
    e.preventDefault();
    if (isSubmitting) return;

    const input = formData.username;
    const signInData = isEmail(input)
      ? { email: input, username: '', password: formData.password }
      : { email: '', username: input, password: formData.password };

    const validationErrors = await validateForm(signInData, 'signIn');
    setErrors(validationErrors);

    const hasErrors = Object.values(validationErrors).some((v) => v);
    if (hasErrors) return;

    setIsSubmitting(true);

    const userProp = !isEmail(input) ? { username: input } : { email: input };

    try {
      await axios.post(
        `/auth/login`,
        {
          ...userProp,
          password: formData.password,
          generateRefreshToken: true,
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': await getCsrfToken(),
          },
        }
      );

      const userRes = await axios.get('/api/v1/get-user-data', { withCredentials: true });
      logIn(userRes.data);

      setFormData({ email: '', username: '', password: '' });

      setModal(null);
      setIsSubmitting(false);
      onClose();

    } catch (err) {
      console.log(err)

      setErrors((prev) => ({
        ...prev,
        email: 'Wrong email or username',
        username: 'Wrong email or username',
      }));
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
      await axios.post(
        `/auth/register`,
        {
          username: formData.username,
          email: formData.email,
          password: formData.password,
        },
        {
          headers: { 'Content-Type': 'application/json' },
        }
      );

      await handleSignIn(e);

      setIsSubmitting(false);
      onClose();

    } catch (err) {
      console.log(err)

      setErrors((prev) => ({
        ...prev,
        email: 'Wrong email or username',
        username: 'Wrong email or username',
        form: 'Wrong email or username',
      }));
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

  const clearErrors = () => setErrors({});
  const clearFormData = () => setFormData({});

  return {
    formData,
    setFormData,
    errors,
    setErrors,
    isSubmitting,
    setIsSubmitting,
    clearErrors,
    clearFormData,
    handleSignIn,
    handleRegister,
    handleFormInput,
  };
}
