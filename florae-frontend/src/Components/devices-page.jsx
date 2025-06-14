/**
 * DevicesPage is the main page component for viewing and managing all devices.
 *
 * Behavior:
 * - Lists all available/connected devices and provides management options.
 *
 * Usage:
 * ```
 * <DevicesPage />
 * ```
 *
 * Note:
 * - This is a high-level page component, typically routed in your app.
 */

import { use, useState, useEffect } from 'react';
import { UserContext } from '../store/user-context.jsx';
import InformationComponent from './information-component.jsx';
import { devicesGuestContent, devicesVisitorContent } from '../util/devices-page-data.js';
import ApiKeyProducer from './api-key-producer.jsx';
import axios from 'axios';
import DevicesCard from './devices-card.jsx';

export default function DevicesPage({ setModal }) {
  const { isLogged } = use(UserContext);
  const [devices, setDevices] = useState([]);

  useEffect(() => {
    console.log('useEffect ran');
    const fetchDevices = async () => {
      try {
        const response = await axios.get('/api/v1/get-floralinks', { withCredentials: true });
        console.log(response.data);
        setDevices(response.data);
      } catch (error) {
        console.error('Failed to fetch devices:', error);
        setDevices([]);
      }
    };

    fetchDevices();
  }, [isLogged]);

  return (
    <>
      {isLogged && (
        <div className="bg-gray-50 m-[4vw] rounded-lg shadow-lg min-h-250">
          <div>
            <ApiKeyProducer guestContent={devicesGuestContent} />
          </div>
          <div className="flex flex-wrap justify-center gap-8 mt-4 mb-4">
            {devices.map((device) => (
              <DevicesCard key={device.floraLinkId} name={device.name} id={device.floraLinkId} />
            ))}
          </div>
          <div className="h-8"></div>
        </div>
      )}
      {!isLogged && (
        <div>
          <InformationComponent
            setModal={setModal}
            showFor="not-logged-in"
            visitorContent={devicesVisitorContent}
          />
        </div>
      )}
    </>
  );
}
