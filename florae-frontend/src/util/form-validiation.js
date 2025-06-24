import * as Yup from 'yup';
import zxcvbn from 'zxcvbn';
import DOMPurify from 'dompurify';

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

const sanitizeString = (value) => {
  if (typeof value !== 'string') return value;
  return DOMPurify.sanitize(value).trim();
};

// Plant name validation - alphanumeric, spaces, hyphens, apostrophes only
const plantNameRegex = /^[a-zA-Z0-9\s\-'()]+$/;

// WiFi SSID validation - alphanumeric, spaces, hyphens, underscores only
const wifiSsidRegex = /^[a-zA-Z0-9\s\-_]+$/;

// Volume validation - positive number with up to 2 decimal places
const volumeRegex = /^\d+(\.\d{1,2})?$/;

export default async function validateForm(values, mode = 'signIn') {
  let schema;

  if (mode === 'register') {
    schema = Yup.object().shape({
      email: Yup.string()
        .transform(sanitizeString)
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
        .transform(sanitizeString)
        .matches(/^[a-zA-Z0-9_]{3,20}$/, 'Username must be 3-20 characters, letters, numbers, or _')
        .required('Username is required'),
      password: Yup.string()
        .transform(sanitizeString)
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
  } else if (mode === 'editPlant') {
    schema = Yup.object().shape({
      name: Yup.string()
        .transform(sanitizeString)
        .min(1, 'Plant name is required')
        .max(50, 'Plant name must be 50 characters or less')
        .matches(
          plantNameRegex,
          'Plant name can only contain letters, numbers, spaces, hyphens, and apostrophes'
        )
        .required('Plant name is required'),
      volume: Yup.string()
        .transform(sanitizeString)
        .matches(volumeRegex, 'Volume must be a positive number with up to 2 decimal places')
        .test('volume-range', 'Volume must be between 0.01 and 1000 L', (value) => {
          if (!value) return false;
          const num = parseFloat(value);
          return num >= 0.01 && num <= 1000;
        })
        .required('Volume is required'),
    });
  } else if (mode === 'espConfig') {
    schema = Yup.object().shape({
      wifiSsid: Yup.string()
        .transform(sanitizeString)
        .min(1, 'WiFi SSID is required')
        .max(32, 'WiFi SSID must be 32 characters or less')
        .matches(
          wifiSsidRegex,
          'WiFi SSID can only contain letters, numbers, spaces, hyphens, and underscores'
        )
        .required('WiFi SSID is required'),
      wifiPassword: Yup.string()
        .transform(sanitizeString)
        .min(8, 'WiFi password must be at least 8 characters')
        .max(64, 'WiFi password must be 64 characters or less')
        .required('WiFi password is required'),
      selectedPlant: Yup.string()
        .transform(sanitizeString)
        .min(1, 'Please select a plant')
        .required('Please select a plant'),
    });
  } else if (mode === 'createPlant') {
    schema = Yup.object().shape({
      name: Yup.string()
        .transform(sanitizeString)
        .min(1, 'Plant name is required')
        .max(50, 'Plant name must be 50 characters or less')
        .matches(
          plantNameRegex,
          'Plant name can only contain letters, numbers, spaces, hyphens, and apostrophes'
        )
        .required('Plant name is required'),
      volume: Yup.string()
        .transform(sanitizeString)
        .matches(volumeRegex, 'Volume must be a positive number with up to 2 decimal places')
        .test('volume-range', 'Volume must be between 0.01 and 1000 L', (value) => {
          if (!value) return false;
          const num = parseFloat(value);
          return num >= 0.01 && num <= 1000;
        })
        .required('Volume is required'),
      files: Yup.mixed()
        .test('file-count', 'You must select between 1 and 5 photos', (value) => {
          if (!value || !value.files) return false;
          return value.files.length >= 1 && value.files.length <= 5;
        })
        .test('file-type', 'Only image files are allowed', (value) => {
          if (!value || !value.files) return false;
          return Array.from(value.files).every((file) => file.type.startsWith('image/'));
        })
        .test('file-size', 'Each image must be less than 10MB', (value) => {
          if (!value || !value.files) return false;
          return Array.from(value.files).every(
            (file) => file.size <= 10 * 1024 * 1024 // 10MB
          );
        })
        .required('Photos are required'),
    });
  } else {
    // Default sign-in schema
    schema = Yup.object().shape({
      username: Yup.string()
        .transform(sanitizeString)
        .test('email-or-username', 'Enter a valid email or username', function (value) {
          const { email } = this.parent;
          if (!email && !value) return false;
          if (!value) return true;
          return /^[a-zA-Z0-9_]{3,20}$/.test(value);
        }),
      email: Yup.string()
        .transform(sanitizeString)
        .test('email-or-username', 'Enter a valid email or username', function (value) {
          const { username } = this.parent;
          if (!value && !username) return false;
          if (!value) return true;
          return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
        }),
      password: Yup.string().transform(sanitizeString).required('Password is required'),
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

// Export individual schemas for direct use
export const authSchema = Yup.object().shape({
  email: Yup.string()
    .transform(sanitizeString)
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
    .transform(sanitizeString)
    .matches(/^[a-zA-Z0-9_]{3,20}$/, 'Username must be 3-20 characters, letters, numbers, or _')
    .required('Username is required'),
  password: Yup.string()
    .transform(sanitizeString)
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

export const editPlantSchema = Yup.object().shape({
  name: Yup.string()
    .transform(sanitizeString)
    .min(1, 'Plant name is required')
    .max(50, 'Plant name must be 50 characters or less')
    .matches(
      plantNameRegex,
      'Plant name can only contain letters, numbers, spaces, hyphens, and apostrophes'
    )
    .required('Plant name is required'),
  volume: Yup.string()
    .transform(sanitizeString)
    .matches(volumeRegex, 'Volume must be a positive number with up to 2 decimal places')
    .test('volume-range', 'Volume must be between 0.01 and 1000 L', (value) => {
      if (!value) return false;
      const num = parseFloat(value);
      return num >= 0.01 && num <= 1000;
    })
    .required('Volume is required'),
});

export const espConfigSchema = Yup.object().shape({
  wifiSsid: Yup.string()
    .transform(sanitizeString)
    .min(1, 'WiFi SSID is required')
    .max(32, 'WiFi SSID must be 32 characters or less')
    .matches(
      wifiSsidRegex,
      'WiFi SSID can only contain letters, numbers, spaces, hyphens, and underscores'
    )
    .required('WiFi SSID is required'),
  wifiPassword: Yup.string()
    .transform(sanitizeString)
    .min(8, 'WiFi password must be at least 8 characters')
    .max(64, 'WiFi password must be 64 characters or less')
    .required('WiFi password is required'),
  selectedPlant: Yup.string()
    .transform(sanitizeString)
    .min(1, 'Please select a plant')
    .required('Please select a plant'),
});

export const createPlantSchema = Yup.object().shape({
  name: Yup.string()
    .transform(sanitizeString)
    .min(1, 'Plant name is required')
    .max(50, 'Plant name must be 50 characters or less')
    .matches(
      plantNameRegex,
      'Plant name can only contain letters, numbers, spaces, hyphens, and apostrophes'
    )
    .required('Plant name is required'),
  volume: Yup.string()
    .transform(sanitizeString)
    .matches(volumeRegex, 'Volume must be a positive number with up to 2 decimal places')
    .test('volume-range', 'Volume must be between 0.01 and 1000 L', (value) => {
      if (!value) return false;
      const num = parseFloat(value);
      return num >= 0.01 && num <= 1000;
    })
    .required('Volume is required'),
  files: Yup.mixed()
    .test('file-count', 'You must select between 1 and 5 photos', (value) => {
      if (!value || !value.files) return false;
      return value.files.length >= 1 && value.files.length <= 5;
    })
    .test('file-type', 'Only image files are allowed', (value) => {
      if (!value || !value.files) return false;
      return Array.from(value.files).every((file) => file.type.startsWith('image/'));
    })
    .test('file-size', 'Each image must be less than 10MB', (value) => {
      if (!value || !value.files) return false;
      return Array.from(value.files).every(
        (file) => file.size <= 10 * 1024 * 1024 // 10MB
      );
    })
    .required('Photos are required'),
});
