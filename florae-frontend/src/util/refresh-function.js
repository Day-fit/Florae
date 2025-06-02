import axios from 'axios';

export async function refreshToken() {
  try {
    const response = await axios.post('/auth/refresh', {}, { withCredentials: true });

    return response.data;
  } catch (error) {
    console.log(error);
  }
}
