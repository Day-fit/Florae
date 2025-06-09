import { useEffect, useState } from 'react';
import axios from 'axios';
import { refreshToken } from './util/refresh-function.js';
import { UserContext } from './store/user-context.jsx';
import App from './App.jsx';

export default function RootApp() {
  const [user, setUser] = useState({
    isLogged: false,
    userData: {},
  });

  const [csrfToken, setCsrfToken] = useState('');

  useEffect(() => {
    const fetchToken = async () => {
      try {
        const res = await axios.get('/csrf', {
          headers: { "Content-Type": "application/json" },
          withCredentials: true,
        });
        setCsrfToken(res.data.token);
        console.log('CSRF token fetched:', res.data.token);
      } catch (err) {
        console.error('Failed to get CSRF token:', err);
      }
    };

    fetchToken();
  }, []);

  function handleLogIn(userData) {
    setUser({
      isLogged: true,
      userData: userData || {},
    });

  }

  function handleLogout() {
    setUser({
      isLogged: false,
      userData: {},
    });
  }

  useEffect(() => {
    async function initializeAuth() {
      try {
        const refreshData = await refreshToken();
        if (!refreshData) {
          console.log('No refresh data returned, user likely not logged in');
          return;
        }

        const userRes = await axios.get('/api/v1/get-user-data', { withCredentials: true });
        handleLogIn(userRes.data);

      } catch (error) {
        console.error('Auth initialization failed:', error);
        handleLogout();
      }
    }

    initializeAuth();
  }, []);

  useEffect(() => {
    if (!user.isLogged) return;

    const interval = setInterval(
      () => {
        refreshToken()
          .then((data) => {
            if (data) {
              console.log('Token refreshed');
            } else {
              console.warn('Token refresh: No data, may be session expired.');
            }
          })
          .catch((error) => console.error('Token refresh failed:', error));
      },
      1000 * 60 * 13.5
    );

    return () => clearInterval(interval);
  }, [user.isLogged]);

  const contextValue = {
    isLogged: user.isLogged,
    userData: user.userData,
    csrfToken,
    logIn: handleLogIn,
    logOut: handleLogout,
  };

  return (
    <UserContext.Provider value={contextValue}>
      <App />
    </UserContext.Provider>
  );
}
