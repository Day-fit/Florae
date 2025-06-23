import { use, useState, useEffect } from 'react';
import { UserContext } from '../store/user-context.jsx';
import InformationComponent from './information-component.jsx';
import { plantsVisitorContent, plantsGuestContent } from '../util/plants-page-data.js';
import Button from './button.jsx';
import CreatePlant from './create-plant.jsx';
import axios from 'axios';
import PlantCard from './plant-card.jsx';
import EditPlant from './edit-plant.jsx';

export default function PlantsPage({ setModal }) {
  const { isLogged } = use(UserContext);

  const [createPlant, setCreatePlant] = useState(false);
  const [ownedPlants, setOwnedPlants] = useState(null);
  const [editingPlant, setEditingPlant] = useState(null);

  function handleCreatePlant() {
    setCreatePlant(true);
  }
  function handleClosePlant() {
    setCreatePlant(false);
    fetchOwnedPlants();
  }
  function handleCloseEditPlant() {
    setEditingPlant(null);
    fetchOwnedPlants();
  }

  async function fetchOwnedPlants() {
    try {
      const response = await axios.get('/api/v1/plants');
      setOwnedPlants(response.data);
    } catch (error) {
      console.log(error);
      setOwnedPlants([]);
    }
  }

  useEffect(() => {
    if (isLogged) {
      fetchOwnedPlants();
    } else {
      setOwnedPlants(null);
    }
  }, [isLogged]);

  return (
    <div className="bg-gray-50 m-[4vw] rounded-lg shadow-lg min-h-250">
      <InformationComponent
        setModal={setModal}
        withOutButton={isLogged}
        showFor={isLogged ? "logged-in" : "not-logged-in"}
        guestContent={isLogged ? plantsGuestContent : undefined}
        visitorContent={!isLogged ? plantsVisitorContent : undefined}
      />
      {isLogged && (
        <>
          <div className="flex justify-end mr-10 mb-5">
            <Button
              buttonText="Add plant"
              className="text-lg bg-stone-400 rounded-2xl pt-3 pb-3 pr-5 pl-5 font-bold"
              onClick={handleCreatePlant}
            />
          </div>
          {createPlant && <CreatePlant onClose={handleClosePlant} />}
          {editingPlant && (
            <EditPlant plant={editingPlant} onClose={handleCloseEditPlant} />
          )}
          {Array.isArray(ownedPlants) && ownedPlants.length > 0 ? (
            <div className="flex flex-row flex-wrap gap-5  justify-center">
              {ownedPlants.map((plant) => (
                <div
                  key={plant.id}
                  className="cursor-pointer hover:scale-105 transition-transform"
                  role="button"
                  tabIndex="0"
                  onClick={() => setEditingPlant(plant)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      setEditingPlant(plant);
                    }
                  }}
                >
                  <PlantCard
                    guestName={plant.name}
                    primaryPhoto={plant.primaryPhoto}
                    speciesName={plant.speciesName}
                    requirements={plant.requirements}
                  />
                </div>
              ))}
            </div>
          ) : (
            <div className="flex justify-center font-bold text-xl">No plants found.</div>
          )}
          <div className="h-2 mt-15" />
        </>
      )}
    </div>
  );
}

