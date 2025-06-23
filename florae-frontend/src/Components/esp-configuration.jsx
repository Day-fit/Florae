import Input from './input.jsx';
import Button from './button.jsx';
import { useEffect, useRef, useState } from 'react';
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';

export default function EspConfiguration({ onClose }) {
  const ssidRef = useRef(null);
  const passwordRef = useRef(null);
  const [plants, setPlants] = useState([]);
  const [selectedPlant, setSelectedPlant] = useState('');

  const ESP_SERVICE_UUID = '53020f00-319c-4d97-a2b1-9e706baba77a';
  const WIFI_CREDENTIALS_CHAR_UUID = 'f87709b3-63a7-4605-9bb5-73c383462296';

  async function sendWiFiCredentials(service, wifi_ssid, wifi_password, api_key) {
    const characteristic = await service.getCharacteristic(WIFI_CREDENTIALS_CHAR_UUID);
    const payload = JSON.stringify({ wifi_ssid, wifi_password, api_key });
    const encoder = new TextEncoder();
    await characteristic.writeValue(encoder.encode(payload));
  }

  const connectToEsp = async (wifi_ssid, wifi_password) => {
    let apiKey = null;
    try {
      // Generate API key first
      const csrfToken = await getCsrfToken();
      const response = await axios.post(
        '/api/v1/generate-key',
        { plantId: selectedPlant },
        {
          withCredentials: true,
          headers: {
            'X-XSRF-TOKEN': csrfToken,
          },
        }
      );
      apiKey = response.data.apiKey;
    // eslint-disable-next-line no-unused-vars
    } catch (e) {
      console.log('Error generating API key');
      return;
    }

    try {
      const device = await navigator.bluetooth.requestDevice({
        filters: [{ name: 'FloraLink' }],
        optionalServices: [ESP_SERVICE_UUID],
      });

      const server = await device.gatt.connect();
      const service = await server.getPrimaryService(ESP_SERVICE_UUID);

      await sendWiFiCredentials(service, wifi_ssid, wifi_password, apiKey);

      // Optionally, you can add a delay here if the ESP needs time to process
      setTimeout(() => {}, 10000);

      // Optionally, connect API after BLE config
      try {
        const csrfToken2 = await getCsrfToken();
        await axios.post(
          '/api/v1/connect-api',
          {},
          {
            withCredentials: true,
            headers: {
              'X-XSRF-TOKEN': csrfToken2,
              'X-API-KEY': apiKey,
            },
          }
        );
      // eslint-disable-next-line no-unused-vars
      } catch (e) {
        console.log('Error connecting API after BLE config');
      }
    // eslint-disable-next-line no-unused-vars
    } catch (error) {
      console.error('Error connecting to FloraLink via BLE:');
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const wifi_ssid = ssidRef.current.value;
    const wifi_password = passwordRef.current.value;
    connectToEsp(wifi_ssid, wifi_password);
  };

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

  return (
    <div className="z-10 bg-white rounded-xl p-10 max-w-lg w-full flex flex-col items-center shadow-lg mx-2">
      <h2 className="mb-6 text-2xl font-bold text-green-700">Konfiguracja ESP</h2>
      <form onSubmit={handleSubmit} className="w-full">
        <div className="mb-4">
          <Input
            ref={ssidRef}
            label="WIFI_SSID"
            type="text"
            placeholder="Type your WiFi name..."
            required
            className={`${baseInputClass} ${noErrorClass}`}
            autoComplete="off"
          />
        </div>
        <div className="mb-4">
          <Input
            ref={passwordRef}
            label="WIFI_PASS"
            type="password"
            placeholder="Type your WiFi password..."
            required
            className={`${baseInputClass} ${noErrorClass}`}
            autoComplete="off"
          />
        </div>
        <div className="mb-4">
          <label htmlFor="plant-select" className="text-left mb-1 font-bold text-">
            Roślina
          </label>
          <select
            className={`${baseInputClass} ${noErrorClass}`}
            value={selectedPlant}
            onChange={(e) => setSelectedPlant(e.target.value)}
          >
            <option value="" disabled className={`${baseInputClass} ${noErrorClass}`}>
              Select a plant
            </option>
            {plants.map((plant) => (
              <option key={plant.id} value={plant.id} className={`${baseInputClass} ${noErrorClass}`}>
                {plant.name}
              </option>
            ))}
          </select>
        </div>
        <div className="flex flex-row justify-between mt-4 w-full">
          <div className="flex justify-start">
            <Button
              buttonText="Sign in"
              type="submit"
              className="max-w-lg text-white bg-green-700 text-center rounded-lg pt-2 pb-2 px-20"
            />
          </div>
          <div className="flex justify-end">
            <CloseButton onClick={onClose} />
          </div>
        </div>
      </form>
    </div>
  );
}
