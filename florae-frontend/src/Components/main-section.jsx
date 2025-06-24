import rainyNature from '../assets/rainyNature.mp4';
import HomePage from './home-page.jsx';
import PlantsPage from './plants-page.jsx';
import DevicesPage from './devices-page.jsx';
import { use } from 'react';
import { UiContext } from '../store/ui-context.jsx';

export default function MainSection() {
  const { setModal, viewMode, setView } = use(UiContext);

  return (
    <div className="relative min-h-screen w-full">
      {/* Video Background */}
      <video
        className="fixed top-0 left-0 w-full h-full object-cover z-0"
        autoPlay
        loop
        muted
        playsInline
        controlsList="nodownload nofullscreen noremoteplayback"
      >
        <source src={rainyNature} type="video/mp4" />
        Your browser does not support the video tag.
      </video>
      <style>{`
    video::-webkit-media-controls-enclosure {
      display: none !important;
    }
  `}</style>
      {/* Optional: semi-transparent overlay for readability */}
      <div className="fixed inset-0 bg-black/40 pointer-events-none"></div>
      {/* Content Overlay */}
      <div className="relative">
        {viewMode === 'home' && <HomePage />}
        {viewMode === 'plants' && <PlantsPage />}
        {viewMode === 'devices' && <DevicesPage />}
      </div>
    </div>
  );
}
