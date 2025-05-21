import Header from "./Components/header.jsx";
import MainSection from "./Components/main-section.jsx";
import Footer from "./Components/footer.jsx";
import { useState } from 'react'

function App() {
  const [user, setUser] = useState({
    isLogged: false,
    userData: []
  });
  // const [viewMode, setViewMode] = useState("home");

  function handleLogIn() {
    setUser(prev => ({
      ...prev,
      isLogged: !prev.isLogged
    }));
  }
  return(
    <>
    <Header user={user} handleLogIn={handleLogIn}/>
    </>
  );
}

export default App
