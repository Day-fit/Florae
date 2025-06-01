import Button from './button.jsx';
import TextComponent from './text-component.jsx';
import { titlePart, bottomPart, bottomPartHeader } from '../util/home-page-data.js';
import { MdLocalFlorist } from 'react-icons/md';
import { HiOutlineInformationCircle } from "react-icons/hi";
import { UserContext } from '../store/user-context.jsx';
import { use } from 'react';

function handlePlants(){
  //when I add plants-page. I will add some logic to this function
}

export default function HomePage({ setModal }){
  const {isLogged} = use(UserContext)
  return(
    <div className="bg-gray-50 m-[4vw] rounded-lg shadow-lg">
      <div className="flex flex-col items-start justify-center p-8 pt-28 pb-28 mt-8 mb-8 bg-gray-200 bg-opacity-90 rounded-lg shadow-lg">
        {/* Top part with title etc. */}
        <h1 className="text-4xl font-bold text-green-900 mb-4">{titlePart.title}</h1>
        <p className="text-lg text-black mb-6">{titlePart.paragraph}</p>
        {isLogged ? (
          <Button
            buttonText={titlePart.btnLogged}
            icon={<MdLocalFlorist />}
            onClick={handlePlants}
            className="bg-green-700 text-white px-6 py-3 rounded-full font-semibold shadow hover:bg-green-800 transition"
          />
        ) : (
          <Button
            buttonText={titlePart.btnNotLogged}
            onClick={() => setModal("register")}
            className="bg-green-700 text-white px-6 py-3 rounded-full font-semibold shadow hover:bg-green-800 transition"
          />
        )}
      </div>
      <div className="flex pl-[2vw] pr-[2vw] justify-between items-center">
        {/* Images */}
        <img
          alt="temp1"
          src="https://placekitten.com/180/180"
          className="w-40 h-40 object-cover rounded-md"
        />
        <img
          alt="temp2"
          src="https://placekitten.com/181/181"
          className="w-40 h-40 object-cover rounded-md"
        />
      </div>
      <div className="w-full py-8 bg-gray-50">
        {/* Headers data about website */}
        <h1 className="text-3xl md:text-4xl font-bold text-green-800 mb-4 ml-22">
          {bottomPartHeader.title}
        </h1>
        <p className="text-lg text-gray-700 mb-6 max-w-2xl ml-22">
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
  )
}