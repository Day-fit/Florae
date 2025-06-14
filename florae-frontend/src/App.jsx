import Header from './Components/header.jsx';
import MainSection from './Components/main-section.jsx';
import { useState } from 'react';
import Footer from './Components/footer.jsx';
import { UiContext } from './store/ui-context.jsx';

function App() {
  const [modal, setModal] = useState(null);
  const [viewMode, setViewMode] = useState('home');

  function handleChangePage(selectedPage) {
    setViewMode(selectedPage);
    if (modal) setModal(null);
  }

  const UIContextValue = {
    modal,
    setModal,
    viewMode,
    setView: (action) => handleChangePage(action),
  };

  return (
    <UiContext value={UIContextValue}>
      <div className="sticky bg-white top-0 z-50">
        <Header />
      </div>
      <MainSection />
      <div className="sticky bg-white z-49">
        <Footer />
      </div>
    </UiContext>
  );
}

export default App;
