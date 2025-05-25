import * as Yup from 'yup';

/**
 * validateForm - Validates the input values against a Yup schema.
 * @param {Object} values - The form values to validate (e.g., { email, password }).
 * @returns {Promise<Object>} - Resolves to an object with errors (empty if valid).
 */
export default async function validateForm(values, mode = 'login') {
  let schema;

  if (mode === 'register') {
    schema = Yup.object().shape({
      email: Yup.string().email('Invalid email address').required('Email is required'),
      login: Yup.string()
        .matches(/^[a-zA-Z0-9_]{3,20}$/, 'Username must be 3-20 characters, letters, numbers, or _')
        .required('Username is required'),
      password: Yup.string()
        .min(6, 'Password should be at least 6 characters')
        .required('Password is required'),
    });
  } else {
    // login: allow email or username in one field
    schema = Yup.object().shape({
      email: Yup.string()
        .test(
          'email-or-username',
          'Enter a valid email or username',
          (value) =>
            !!value &&
            (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) || // email pattern
              /^[a-zA-Z0-9_]{3,20}$/.test(value)) // username pattern
        )
        .required('Email or login is required'),
      password: Yup.string()
        .min(6, 'Password should be at least 6 characters')
        .required('Password is required'),
    });
  }

  try {
    await schema.validate(values, { abortEarly: false });
    return {}; // No errors
  } catch (err) {
    const errors = {};
    if (err.inner) {
      err.inner.forEach((e) => {
        errors[e.path] = e.message;
      });
    }
    return errors;
  }
}
