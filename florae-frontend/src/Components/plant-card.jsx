import {useState} from "react";

export default function PlantCard({primaryPhoto, speciesName, requirements}){
    const imgSrc = `data:image/png;base64,${primaryPhoto}`;
    const [selection, setSelection] = useState("optimal");

    function handleOptionChange(e){
        setSelection(e)
    }
    function renderRequirements(){
        if(selection === "optimal"){
            const optimalvalues = {
                optimal_env_humid: (requirements.max_env_humid + requirements.min_env_humid)/2,

            }
        }
    }
    return(
      <div className="w-[20vw] h-[60vh] border-stone-400 rounded-2xl border-2 flex flex-col ">
          <div className="flex justify-center mt-5">
              <h1>{speciesName}</h1>
          </div>
          <div className="flex justify-center mt-5 mb-5">
              <img src={imgSrc} alt={speciesName} className="w-[80%]"/>
          </div>
          <div className="flex justify-center">
          <select
              className="
            border w-[40%] border-stone-400 rounded-lg px-4 py-2 text-lg font-medium
            focus:outline-none focus:ring-2 focus:ring-green-400 bg-green-50 text-green-700 transition
            hover:bg-green-100
          "
              value={selection}
              onChange={(e) => handleOptionChange(e.target.value)}
          >
              <option value="optimal">Optimal</option>
              <option value="max">Max</option>
              <option value="min">Min</option>
          </select>
          </div>
          <div>

          </div>
      </div>
    );
}