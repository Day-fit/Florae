import axios from './axios-client.js';

export default async function getCsrfToken() {
  console.log("Getting token...");
  try {
      const response = await axios.get('/csrf', {
          headers: { "Content-Type": "application/json" },
          withCredentials: true,
      });
      console.log(response.data.token);
      return response.data.token;
  } catch(e) {
      console.log(e);
      return null;
  }
}
