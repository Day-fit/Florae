import rainyNature from '../assets/rainyNature.mp4';
import HomePage from './home-page.jsx';
import PlantsPage from './plants-page.jsx';
import DevicesPage from './devices-page.jsx';
export default function MainSection({ setModal, viewMode }) {
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
      <div className="fixed inset-0 bg-black/40 z-10 pointer-events-none"></div>
      {/* Content Overlay */}
      <div className="relative z-20">
        {viewMode === 'home' && <HomePage setModal={setModal} />}
        {viewMode === 'plants' && <PlantsPage setModal={setModal} />}
        {viewMode === 'devices' && <DevicesPage setModal={setModal} />}
      </div>
    </div>
  );
}
