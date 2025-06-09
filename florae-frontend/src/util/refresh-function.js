import axios from 'axios';

export async function refreshToken() {
  try {
    console.log('Attempting to refresh token...');
    const response = await axios.post('/auth/refresh', {}, { 
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    console.log('Token refresh successful');
    return response.data;
  } catch (error) {
    console.log('Token refresh failed:', error);
    return null;
  }
}
