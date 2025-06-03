import Header from './Components/header.jsx';
import { UserContext } from './store/user-context.jsx';
import MainSection from './Components/main-section.jsx';
import { useState, useEffect } from 'react';
import { refreshToken } from './util/refresh-function.js';
import Footer from './Components/footer.jsx';

function App() {
  const [user, setUser] = useState({
    isLogged: false,
    userData: {},
  });
  const [modal, setModal] = useState(null);
  const [viewMode, setViewMode] = useState('home');

  const contextValue = {
    isLogged: user.isLogged,
    userData: user.userData,
    logIn: handleLogIn,
    logOut: handleLogout,
  };
  function handleChangePage(selectedPage) {
    setViewMode(selectedPage);
    if (modal) setModal(null);
  }
  function handleLogout() {
    setUser((prev) => ({
      isLogged: !prev.isLogged,
      userData: {},
    }));
  }
  function handleLogIn(userData) {
    setUser((prev) => ({
      userData: userData,
      isLogged: !prev.isLogged,
    }));
  }

  useEffect(() => {
    if (user.isLogged) {
      // or however you check login
      const refreshInterval = setInterval(
        () => {
          refreshToken()
            .then((response) => console.log('Token refreshed:', response))
            .catch((error) => console.error('Failed to refresh:', error));
        },
        1000 * 60 * 13.5
      );

      return () => clearInterval(refreshInterval);
    }
  }, [user.isLogged]);

  return (
    <UserContext value={contextValue}>
      <div className="sticky bg-white top-0 z-50">
        <Header modal={modal} setModal={setModal} changePage={handleChangePage} />
      </div>
      <MainSection viewMode={viewMode} setModal={setModal} />
      <div className="sticky bg-white z-50">
        <Footer/>
      </div>
    </UserContext>
  );
}

export default App;
