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

    const response = await axios.post('/auth/refresh', {},
      {
        headers: {
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      }
    );
    console.log('Token refresh successful');
    console.log(response.data)
    return response.data;
  } catch (error) {
    console.log('Token refresh failed:', error);
    return null;
  }
}