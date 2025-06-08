import axios from 'axios';

let csrfToken = '';
export function setCsrfToken(token) {
  csrfToken = token;
}

axios.interceptors.request.use(
  config => {
    const unsafeMethods = ['post', 'put', 'patch', 'delete'];
    if (csrfToken && unsafeMethods.includes(config.method)) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
    config.withCredentials = true;
    return config;
  },
  error => Promise.reject(error)
);
