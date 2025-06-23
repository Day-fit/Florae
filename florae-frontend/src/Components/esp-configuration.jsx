import Input from './input.jsx';
import Button from './button.jsx';
import { useEffect, useRef, useState } from 'react';
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';
import { espConfigSchema } from '../util/form-validiation.js';

export default function EspConfiguration({ onClose }) {
  const ssidRef = useRef(null);
  const passwordRef = useRef(null);
  const [plants, setPlants] = useState([]);
  const [selectedPlant, setSelectedPlant] = useState('');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

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
        filters: [{ namePrefix: 'FloraLink' }],
        optionalServices: [ESP_SERVICE_UUID],
      });

      const server = await device.gatt.connect();
      const service = await server.getPrimaryService(ESP_SERVICE_UUID);

      await sendWiFiCredentials(service, wifi_ssid, wifi_password, apiKey);

      // Optionally, you can add a delay here if the ESP needs time to process
      setTimeout(() => {}, 10000);

      // Optionally, connect API after BLE config
      try {
        let csrfToken = await getCsrfToken();

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

        csrfToken = await getCsrfToken();

        await axios.post(
          '/api/v1/connect-api',
          {},
          {
            withCredentials: true,
            headers: {
              'X-XSRF-TOKEN': csrfToken,
              'X-API-KEY': response.data.apiKey,
            },
          }
        );
        // eslint-disable-next-line no-unused-vars
      } catch (e) {
        console.error("Error generating API key");
      }
      // eslint-disable-next-line no-unused-vars
    } catch (error) {
      console.error('Error connecting to FloraLink via BLE:');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);

    const wifi_ssid = ssidRef.current.value;
    const wifi_password = passwordRef.current.value;

    // Validate using Yup schema
    try {
      await espConfigSchema.validate({
        wifiSsid: wifi_ssid,
        wifiPassword: wifi_password,
        selectedPlant: selectedPlant
      }, { abortEarly: false });
    } catch (validationError) {
      const newErrors = {};
      validationError.inner.forEach((error) => {
        newErrors[error.path] = error.message;
      });
      setErrors(newErrors);
      setSubmitting(false);
      return;
    }

    await connectToEsp(wifi_ssid, wifi_password);
    setSubmitting(false);
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
    <div className="z-10 bg-white/90 rounded-xl p-10 max-w-lg w-full flex flex-col items-center shadow-lg mx-2">
      <h2 className="mb-6 text-2xl font-bold text-green-700">FloraLink Configuration</h2>
      <form onSubmit={handleSubmit} className="w-full">
        <div className="mb-4">
          <Input
            ref={ssidRef}
            label="WIFI_SSID"
            type="text"
            placeholder="Type your WiFi name..."
            required
            className={`${baseInputClass} ${errors.wifiSsid ? 'border-red-500' : noErrorClass}`}
            autoComplete="off"
            errorMsg={errors.wifiSsid}
          />
        </div>
        <div className="mb-4">
          <Input
            ref={passwordRef}
            label="WIFI_PASS"
            type="password"
            placeholder="Type your WiFi password..."
            required
            className={`${baseInputClass} ${errors.wifiPassword ? 'border-red-500' : noErrorClass}`}
            autoComplete="off"
            errorMsg={errors.wifiPassword}
          />
        </div>
        <div className="mb-4">
          <label
            htmlFor="plant-select"
            className="text-left mb-1 font-bold text-"
          >
            Plant
          </label>
          <select
            className={`${baseInputClass} ${errors.selectedPlant ? 'border-red-500' : noErrorClass}`}
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
          {errors.selectedPlant && (
            <p className="text-red-800 text-sm mt-1">{errors.selectedPlant}</p>
          )}
        </div>
        <div className="flex flex-row justify-between mt-4 w-full">
          <div className="flex justify-start">
            <Button
              buttonText={submitting ? "Connecting..." : "Sign in"}
              type="submit"
              className="max-w-lg text-white bg-green-700 text-center rounded-lg pt-2 pb-2 px-20"
              disabled={submitting}
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