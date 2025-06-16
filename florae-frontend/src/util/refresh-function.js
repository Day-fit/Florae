import axios from './axios-client.js';
import getCsrfToken from './getCsrfToken.js';

export async function refreshToken() {
  try {
    const csrfToken = await getCsrfToken();

    if (!csrfToken) {
      console.log('CSRF token fetch failed after retries');
      return null;
    }

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
    return response.data;
  // eslint-disable-next-line no-unused-vars
  } catch (error) {
    console.log('Token refresh failed:');
    return null;
  }
}
