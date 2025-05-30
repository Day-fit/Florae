import Header from './Components/header.jsx';
import { UserContext } from './store/user-context.jsx';
import MainSection from './Components/main-section.jsx';
import { useState } from 'react';

function App() {
  const [user, setUser] = useState({
    isLogged: false,
    userData: {},
  });
  const [modal, setModal] = useState(null);
  // const [viewMode, setViewMode] = useState("home");

  const contextValue = {
    isLogged: user.isLogged,
    userData: user.userData,
    logIn: handleLogIn,
  };
  function handleLogIn(userData) {
    setUser((prev) => ({
      userData: userData,
      isLogged: !prev.isLogged,
    }));
  }
  return (
    <UserContext value={contextValue}>
      <div className="sticky bg-white top-0 z-50">
        <Header modal={modal} setModal={setModal} />
      </div>
      <MainSection setModal={setModal}/>
    </UserContext>
  );
}

export default App;
