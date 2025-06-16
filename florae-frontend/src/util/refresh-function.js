import axios from './axios-client.js';
import getCsrfToken from './getCsrfToken.js';

export async function refreshToken() {
  try {
    console.log('Attempting to refresh token...');

    const csrfToken = await getCsrfToken();

    console.log('CSRF Token:', csrfToken);

    if (!csrfToken) {
      console.log('CSRF token fetch failed after retries');
      return null;
    }

    console.log('Sending token refresh request...');

    const response = await axios.post(
      '/auth/refresh',
      {},
      {
        headers: {
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      }
    );
    console.log('Token refresh successful');
    return response.data;
  // eslint-disable-next-line no-unused-vars
  } catch (error) {
    console.log('Token refresh failed:');
    return null;
  }
}
