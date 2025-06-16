import { useState } from 'react';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';
import validateForm, { isEmail } from '../util/form-validiation.js';

export default function useAuthHandlers({ logIn, onClose, setModal }) {
  const [errors, setErrors] = useState({
    email: '',
    username: '',
    password: '',
  });
  const [formData, setFormData] = useState({
    email: '',
    username: '',
    password: '',
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

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
      const csrfToken = await getCsrfToken();

      const config = {
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken,
        },
      };

      await axios.post(
        `/auth/login`,
        {
          ...userProp,
          password: formData.password,
          generateRefreshToken: true,
        },
        config
      );

      const userRes = await axios.get('/api/v1/get-user-data', { withCredentials: true });
      logIn(userRes.data);

      setFormData({ email: '', username: '', password: '' });

      setModal(null);
      setIsSubmitting(false);
      onClose();
    // eslint-disable-next-line no-unused-vars
    } catch (err) {
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
      const csrfToken = await getCsrfToken();

      const config = {
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': csrfToken,
        },
      };

      await axios.post(
        `/auth/register`,
        {
          username: formData.username,
          email: formData.email,
          password: formData.password,
        },
        config
      );

      await handleSignIn(e);

      setIsSubmitting(false);
      onClose();
    // eslint-disable-next-line no-unused-vars
    } catch (err) {
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
