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
  const [viewMode, setViewMode] = useState("home");

  const contextValue = {
    isLogged: user.isLogged,
    userData: user.userData,
    logIn: handleLogIn,
    logOut: handleLogout,
  };
  function handleChangePage(selectedPage){
    setViewMode(selectedPage);
    if(modal)setModal(null);
  }
  function handleLogout() {
    setUser((prev)=>({
      isLogged: !prev.isLogged,
      userData: {},
    }))
  }
  function handleLogIn(userData) {
    setUser((prev) => ({
      userData: userData,
      isLogged: !prev.isLogged,
    }));
  }
  return (
    <UserContext value={contextValue}>
      <div className="sticky bg-white top-0 z-50">
        <Header modal={modal} setModal={setModal} changePage={handleChangePage} />
      </div>
      <MainSection viewMode={viewMode} setModal={setModal}/>
    </UserContext>
  );
}

export default App;
