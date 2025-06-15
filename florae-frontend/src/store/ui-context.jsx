import { createContext } from 'react';

export const UiContext = createContext({
  viewMode: '',
  setView: () => {},
  modal: null,
  setModal: () => {},
});
