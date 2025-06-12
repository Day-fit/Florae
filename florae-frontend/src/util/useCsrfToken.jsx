import { useState } from 'react';
import axios from './axiosClient.js';

let cachedToken = null;
let tokenPromise = null;

export async function initializeCsrfToken() {
    if (!cachedToken && !tokenPromise) {
        console.log("Getting token...");
        try {
            const response = await axios.get('/csrf', {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });
            console.log(response);
            cachedToken = response.data.token;
            return cachedToken;
        } catch(e) {
            console.log(e);
            return null;
        }
    }
    return cachedToken;
}

export function useCsrfToken() {
    const [csrfToken, setCsrfToken] = useState(cachedToken);
    const [isReady, setIsReady] = useState(false);

    if (!csrfToken && !tokenPromise) {
        const fetchToken = async () => {
            try {
                const token = await initializeCsrfToken();
                if (token) {
                    setCsrfToken(token);
                }
                setIsReady(true);
                return token;
            } finally {
                tokenPromise = null;
            }
        };

        tokenPromise = fetchToken();
    } else if (cachedToken && !csrfToken) {
        setCsrfToken(cachedToken);
        setIsReady(true);
    }

    return { csrfToken, isReady };
}