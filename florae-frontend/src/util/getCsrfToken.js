import axios from './axios-client.js';

let cachedToken = null;

export default async function getCsrfToken() {
    if (!cachedToken) {
        console.log("Getting token...");
        try {
            const response = await axios.get('/csrf', {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });
            console.log(response.data.token);
            cachedToken = response.data.token;
            return cachedToken;
        } catch(e) {
            console.log(e);
            return null;
        }
    }
    return cachedToken;
}
