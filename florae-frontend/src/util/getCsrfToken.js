import axios from './axios-client.js';

export default async function getCsrfToken() {
  console.log('Getting token...');
  try {
    const response = await axios.get('/csrf', {
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true,
    });
    return response.data.token;
  // eslint-disable-next-line no-unused-vars
  } catch (e) {
    return null;
  }
}
