import Header from './Components/header.jsx';
import MainSection from './Components/main-section.jsx';
import { useState } from 'react';
import Footer from './Components/footer.jsx';

function App() {
  const [modal, setModal] = useState(null);
  const [viewMode, setViewMode] = useState('home');

  function handleChangePage(selectedPage) {
    setViewMode(selectedPage);
    if (modal) setModal(null);
  }

  return (
    <>
      <div className="sticky bg-white top-0 z-50">
        <Header modal={modal} setModal={setModal} changePage={handleChangePage} />
      </div>
      <MainSection viewMode={viewMode} setModal={setModal} viewMode={viewMode} />
      <div className="sticky bg-white z-49">
        <Footer />
      </div>
    </>
  );
}

export default App;
