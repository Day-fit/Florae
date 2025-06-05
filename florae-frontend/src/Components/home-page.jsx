import TextComponent from './text-component.jsx';
import {
  bottomPart,
  bottomPartHeader,
  homeGuestContent,
  homeVisitorContent,
} from '../util/home-page-data.js';
import { HiOutlineInformationCircle } from 'react-icons/hi';
import InformationComponent from './information-component.jsx';

function handlePlants() {
  //when I add plants-page. I will add some logic to this functio
}

export default function HomePage({ setModal }) {
  return (
    <div className="bg-gray-50 m-[4vw] rounded-lg shadow-lg">
      <InformationComponent
        setModal={setModal}
        handlePlants={handlePlants}
        visitorContent={homeVisitorContent}
        guestContent={homeGuestContent}
      />
      <div className="w-full py-8 bg-gray-50">
        {/* Headers data about website */}
        <h1 className="text-3xl md:text-4xl font-bold text-green-800 mb-4 ml-22">
          {bottomPartHeader.title}
        </h1>
        <p className="text-lg text-gray-700 mb-6 max-w-2xl ml-22">{bottomPartHeader.paragraph}</p>
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
