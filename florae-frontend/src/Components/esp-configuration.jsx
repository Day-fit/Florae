import Input from './input.jsx';
import Button from './button.jsx';
import { useRef } from 'react';
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx';

export default function EspConfiguration({ onClose }) {
  const ssidRef = useRef(null);
  const passwordRef = useRef(null);

  const connectToEsp = async (ssid, password) => {
    try {
      console.log(1)
      const device = await navigator.bluetooth.requestDevice({
        filters: [{ namePrefix: 'ESP32' }],
      });
      console.log(1)

      // Brakuje definicji ESP_SERVICE_UUID i sendWiFiCredentials, dodaj je jeśli są potrzebne
      const server = await device.gatt.connect(ESP_SERVICE_UUID);
      console.log(1)

      await sendWiFiCredentials(service, ssid, password);
      console.log(1)

    } catch (error) {
      console.error("Error connecting to ESP:", error);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const ssid = ssidRef.current.value;
    const password = passwordRef.current.value;
    connectToEsp(ssid, password);
  };

  return (
    <div className="z-10 bg-white/90 rounded-xl p-10 max-w-lg w-full flex flex-col items-center shadow-lg mx-2">
      <h2 className="mb-6 text-2xl font-bold text-green-700">Konfiguracja ESP</h2>

      <form onSubmit={handleSubmit} className="w-full">
        <div className="mb-4">
          <Input
            ref={ssidRef}
            label="Sieć Wi-Fi"
            type="text"
            placeholder="Wpisz nazwę sieci Wi-Fi"
            required
            className={`${baseInputClass} ${noErrorClass}`}
          />
        </div>
        <div className="mb-4">
          <Input
            ref={passwordRef}
            label="Hasło do Wi-Fi"
            type="password"
            placeholder="Wpisz hasło do Wi-Fi"
            required
            className={`${baseInputClass} ${noErrorClass}`}
            autoComplete="current-password"
          />
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
            <CloseButton onClick={onClose}/>
          </div>
        </div>
      </form>
    </div>
  );
}