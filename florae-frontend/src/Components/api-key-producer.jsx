import { useState, useEffect, useRef } from 'react';
import Button from './button.jsx';
import { useTimer, formatTime } from '../util/timer.jsx';
import { FiCopy, FiCheck } from 'react-icons/fi';
import axios from 'axios';

export default function ApiKeyProducer({ guestContent }) {
  const [showed, setShowed] = useState(false);
  const [apiKey, setApiKey] = useState('');
  const [copied, setCopied] = useState(false);
  const [plants, setPlants] = useState([]);
  const [selectedPlant, setSelectedPlant] = useState('');

  const [timeLeft, running, start, reset] = useTimer(900, false);

  useEffect(() => {
    const fetchPlants = async () => {
      try {
        const response = await axios.get('/api/v1/plants', { withCredentials: true });
        setPlants(response.data);
      } catch (error) {
        console.error('Failed to fetch plants:', error);
      }
    };
    fetchPlants();
  }, []);

  async function getApikey() {
    if (!selectedPlant) return null; // nothing selected!
    try {
      const response = await axios.post(
        '/api/v1/generate-key',
        { plantId: selectedPlant },
        { withCredentials: true }
      );
      return response.data.apiKey; // or whatever your backend sends
    } catch (e) {
      console.log(e);
      return null;
    }
  }

  const handleShowApiKey = async () => {
    const key = await getApikey();
    if (key) {
      setApiKey(key);
      setShowed(true);
      start();
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(apiKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  useEffect(() => {
    if (running && timeLeft === 0) {
      setShowed(false);
      setApiKey('');
      reset();
    }
  }, [running, timeLeft, reset]);

  return (
    <div className="flex flex-col items-start justify-center p-8 pt-28 pb-28 mt-8 mb-8 bg-gray-200 bg-opacity-90 rounded-lg shadow-lg">
      <h1>{guestContent.title}</h1>
      {!showed ? (
        <div className="flex flex-col">
          <Button
            buttonText="Show API Key"
            className="text-base bg-stone-500 text-white rounded-xl px-6 py-2 font-bold mb-3"
            onClick={handleShowApiKey}
            disabled={!selectedPlant}
          />
          <select
            className="w-full p-2 mb-4 bg-gray-700 rounded text-white"
            value={selectedPlant}
            onChange={(e) => setSelectedPlant(e.target.value)}
          >
            <option value="" disabled>
              Select a plant
            </option>
            {plants.map((plant) => (
              <option key={plant.id} value={plant.id}>
                {plant.name}
              </option>
            ))}
          </select>
        </div>
      ) : (
        <div className="relative bg-gray-600/85 text-white w-full rounded p-4 mt-2 select-all transition-colors duration-200">
          <p className="break-all font-mono text-lg flex items-center">
            API Key: <span className="ml-2">{apiKey}</span>
            <button
              type="button"
              className="ml-auto flex items-center bg-transparent hover:text-green-300 transition-colors p-1"
              onClick={handleCopy}
              aria-label="Copy API Key"
            >
              {copied ? <FiCheck size={20} /> : <FiCopy size={20} />}
            </button>
          </p>
          <p>Will hide in: {formatTime(timeLeft)} seconds</p>
        </div>
      )}
      <span>{guestContent.paragraph}</span>
    </div>
  );
}

