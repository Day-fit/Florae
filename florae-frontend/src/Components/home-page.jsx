import TextComponent from './text-component.jsx';
import {
  bottomPart,
  bottomPartHeader,
  homeGuestContent,
  homeVisitorContent,
} from '../util/home-page-data.js';
import { UiContext } from '../store/ui-context.jsx';
import { use } from 'react';
import { HiOutlineInformationCircle } from 'react-icons/hi';
import InformationComponent from './information-component.jsx';
import plant1 from '../assets/home-img1.jpg';
import plant2 from '../assets/home-img2.jpg';

export default function HomePage() {
  const { setView, setModal } = use(UiContext);

  function handlePlants(action) {
    setView(action);
  }
  return (
    <div className="bg-gray-50 m-[4vw] rounded-lg shadow-lg overflow-hidden">
      <InformationComponent
        setModal={setModal}
        handleTask={() => handlePlants('plants')}
        visitorContent={homeVisitorContent}
        guestContent={homeGuestContent}
      />
      <div className="flex flex-col md:flex-row px-[4vw] justify-center gap-4 md:gap-8 lg:gap-[12vw] items-center">
        {/* Obrazy bez ramek */}
        <div className="w-full max-w-[480px] lg:max-w-[640px] rounded-2xl shadow-md hover:shadow-xl transition-shadow duration-300">
          <img
            alt="plant1"
            src={plant1}
            className="w-full h-auto aspect-[3/2] object-cover rounded-2xl"
          />
        </div>
        <div className="w-full max-w-[480px] lg:max-w-[640px] rounded-2xl shadow-md hover:shadow-xl transition-shadow duration-300">
          <img
            alt="plant2"
            src={plant2}
            className="w-full h-auto aspect-[3/2] object-cover rounded-2xl"
          />
        </div>
      </div>
      <div className="w-full py-8 bg-gray-50">
        <h1 className="text-3xl md:text-4xl font-bold text-green-800 mb-4 md:ml-22 text-center md:text-left">
          {bottomPartHeader.title}
        </h1>
        <p className="text-lg text-gray-700 mb-6 max-w-2xl md:ml-22 text-center md:text-left">
          {bottomPartHeader.paragraph}
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 justify-items-center">
          {bottomPart.map(({ name, title, paragraph }) => (
            <div
              key={name}
              className="bg-white/90 rounded-xl shadow-lg px-4 py-2 transition-transform duration-200 hover:scale-105 hover:shadow-2xl flex flex-col items-center w-full max-w-2xl min-w-[320px]"
            >
              <TextComponent
                title={title}
                paragraph={paragraph}
                icon={<HiOutlineInformationCircle className="text-green-700 text-3xl mb-2" />}
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
