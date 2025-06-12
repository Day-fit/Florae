import axios from 'axios';
import { useEffect, useRef } from 'react';
import { useCsrfToken } from './useCsrfToken.jsx';

export function useRefreshToken() {
    const {csrfToken, isReady} = useCsrfToken();
    const resolveRef = useRef(null);

    useEffect(() => {
        if (isReady && resolveRef.current) {
            resolveRef.current();
            resolveRef.current = null;
        }
    }, [isReady]);

    return async () => {
        if (!isReady) {
            await new Promise((resolve) => {
                resolveRef.current = resolve;
            });
        }

        if (!csrfToken) {
            console.warn('Brak CSRF tokena przy odświeżaniu!');
            return null;
        }

        try {
            const response = await axios.post(
                '/auth/refresh',
                {},
                {
                    withCredentials: true,
                    headers: {
                        'X-CSRF-Token': csrfToken,
                    },
                }
            );
            return response.data;
        } catch (error) {
            console.log(error);
            return null;
        }
    };
}
