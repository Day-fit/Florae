import { createContext } from 'react';

export const UserContext = createContext({
  isLogged: false,
  userData: {},
  logIn: () => {},
  logOut: () => {},
});
