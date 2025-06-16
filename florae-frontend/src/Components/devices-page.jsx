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
import axios from 'axios';
import DevicesCard from './devices-card.jsx';
import EspConfiguration from './esp-configuration.jsx';
import Button from './button.jsx';

export default function DevicesPage({ setModal }) {
  const { isLogged } = use(UserContext);
  const [devices, setDevices] = useState([]);
  const [showEspModal, setShowEspModal] = useState(false);

  useEffect(() => {
    if (showEspModal) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [showEspModal]);

  useEffect(() => {
    const fetchDevices = async () => {
      try {
        const response = await axios.get('/api/v1/get-floralinks', { withCredentials: true });
        setDevices(response.data);
      // eslint-disable-next-line no-unused-vars
      } catch (error) {
        console.error('Failed to fetch devices');
      }
    };

    fetchDevices();
  }, [isLogged]);

  function handleEspConfig() {
    setShowEspModal(true);
  }

  function closeEspModal() {
    setShowEspModal(false);
  }
  return (
    <>
      <div className="bg-gray-50 m-[4vw] rounded-lg shadow-lg min-h-250">
        <InformationComponent
          setModal={setModal}
          handleTask={ handleEspConfig }
          guestContent={ devicesGuestContent }
          visitorContent={ devicesVisitorContent }
          showFor="both"
        />
        {isLogged && (
          <>
            <div className="flex flex-wrap justify-center gap-8 mt-4 mb-4">
              {devices.map((device) => (
                <DevicesCard key={device.floraLinkId} name={device.name} id={device.floraLinkId} />
              ))}
            </div>
            <div className="h-8"></div>
          </>
        )}
      </div>
      {showEspModal && (
        <div className="fixed inset-0 flex items-center justify-center z-50 bg-black/30">
          <EspConfiguration onClose={closeEspModal} />
        </div>
      )}
    </>
  );
}
