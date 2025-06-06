import * as Yup from 'yup';
import zxcvbn from 'zxcvbn';

/**
 * validateForm - Validates the input values against a Yup schema.
 * @param {Object} values - The form values to validate (e.g., { email, password }).
 * @param {string} [mode='login'] - The validation mode, defaults to 'login'.
 * @returns {Promise<Object>} - Resolves to an object with errors (empty if valid).
 */

/**
 * Auth (Sign-In) Schema - Yup validation
 *
 * Validates that EITHER 'email' OR 'username' is required and valid.
 * - Email: Valid email format, but not single-letter TLDs or nested single-letter TLDs.
 * - Username: 3-20 letters, numbers, or underscores.
 *
 * Each field is mutually optional, but at least one of them must be filled.
 * Password is always required.
 *
 * Usage:
 *   import { authSchema } from '../util/form-validation';
 *   authSchema.validate(formData);
 */

export function isEmail(value) {
  // Basic RFC5322 email regex (customize as needed)
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

export default async function validateForm(values, mode = 'signIn') {
  let schema;

  if (mode === 'register') {
    schema = Yup.object().shape({
      email: Yup.string()
        .test(
          'email',
          'Enter a valid email',
          (value) =>
            !!value &&
            /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) &&
            !/\.[a-zA-Z]{1}\.[a-zA-Z]+$/.test(value) &&
            !/\.[a-zA-Z]{1}$/.test(value)
        )
        .required('Email or login is required'),
      username: Yup.string()
        .matches(/^[a-zA-Z0-9_]{3,20}$/, 'Username must be 3-20 characters, letters, numbers, or _')
        .required('Username is required'),
      password: Yup.string()
        .min(8, 'Password must be at least 8 characters')
        .matches(/[A-Z]/, 'Password must contain at least one uppercase letter')
        .matches(/[a-z]/, 'Password must contain at least one lowercase letter')
        .matches(/[0-9]/, 'Password must contain at least one digit')
        .matches(/[!@#$%^&*(),.?":{}|<>]/, 'Password must contain at least one special character')
        .test('password-strength', 'Password is too weak', (value) => {
          if (!value) return false;
          return zxcvbn(value).score >= 3;
        })
        .required('Password is required'),
    });
  } else {
    console.log(values)
    schema = Yup.object().shape({
      username: Yup.string().test(
        'email-or-username',
        'Enter a valid email or username',
        function (value) {
          const { email } = this.parent;
          if (!email && !value) return false;
          if (!value) return true;
          return /^[a-zA-Z0-9_]{3,20}$/.test(value);
        }
      ),
      email: Yup.string().test(
        'email-or-username',
        'Enter a valid email or username',
        function (value) {
          const { username } = this.parent;
          if (!value && !username) return false;
          if (!value) return true;
          return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
        }
      ),
      password: Yup.string().required('Password is required'),
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
