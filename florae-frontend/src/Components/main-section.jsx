import rainyNature from '../assets/rainyNature.mp4';
import HomePage from './home-page.jsx';
export default function MainSection({setModal}){
    return(
        <div className="relative min-h-screen w-full">
    {/* Video Background */}
  <video
    className="fixed top-0 left-0 w-full h-full object-cover z-0"
    autoPlay
    loop
    muted
    playsInline
  >
    <source src={rainyNature} type="video/mp4" />
    Your browser does not support the video tag.
  </video>
  {/* Optional: semi-transparent overlay for readability */}
  <div className="fixed inset-0 bg-black/40 z-10 pointer-events-none"></div>
  {/* Content Overlay */}
  <div className="relative z-20">
    <HomePage setModal={setModal}/>
  </div>
</div>

);
}