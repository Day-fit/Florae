import { use, useState, useEffect } from 'react';
import { UserContext } from '../store/user-context.jsx';
import InformationComponent from './information-component.jsx';
import { plantsVisitorContent } from '../util/plants-page-data.js';
import Button from './button.jsx';
import CreatePlant from './create-plant.jsx';
import axios from 'axios';

export default function PlantsPage({ setModal }) {
  const { isLogged } = use(UserContext);

  const [createPlant, setCreatePlant] = useState(false)
  const [ownedPlants, setOwnedPlants] = useState(null)

  function handleCreatePlant (){
    setCreatePlant(true);
  }
  function handleClosePlant(){
    setCreatePlant(false);
  getOwnedPlants().then(setOwnedPlants)
  }

  async function getOwnedPlants() {
    let ownedPlants;
    try {
      const response = await axios.get("/api/v1/plants");
      ownedPlants = response.data;
    } catch (error) {
      console.log(error);
      ownedPlants = "You don't have any plants.";
    }
    return ownedPlants;
  }



  useEffect(() => {
    if (isLogged) {
      getOwnedPlants().then(setOwnedPlants);
    }
  }, [isLogged]);

  useEffect(() => {
    if(ownedPlants){
      console.log(ownedPlants)
    }
  }, [ownedPlants]);

  return (
    <>
      {isLogged &&
        <div className="flex flex-col items-start justify-center p-8 pt-28 pb-28 mt-8 mb-8 bg-gray-200 bg-opacity-90 rounded-lg shadow-lg">
          {/*if I have some time, I will add a search bar*/}
          <div>
            {/* to the left side */}
            <Button buttonText="Add plant" onClick={handleCreatePlant}/>
          </div>
          {createPlant && <CreatePlant onClose={handleClosePlant}/>}
          {
            typeof ownedPlants === 'string' ? (
              <div>{ownedPlants}</div>
            ) : Array.isArray(ownedPlants) && ownedPlants.length > 0 ? (
              ownedPlants.map((plant) => (
                <div key={plant.id /* or another unique property */}>
                  {/* there will be plant card displayed */}
                  <span>{plant.speciesName}</span>
                </div>
              ))
            ) : (
              <div>No plants found.</div>
            )
          }
        </div>
      }
      {!isLogged && (
        <div>
          <InformationComponent
            setModal={setModal}
            showFor="not-logged-in"
            visitorContent={plantsVisitorContent}
          />
        </div>
      )}
    </>
  );
}
