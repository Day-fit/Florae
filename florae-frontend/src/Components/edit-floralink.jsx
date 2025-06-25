/**
 * EditFloraLink displays and allows editing of FloraLink device information.
 *
 * Props:
 * - floralink (object): The FloraLink device data to display and edit.
 * - onClose (function): Callback to close the modal.
 *
 * Usage:
 * ```
 * <EditFloraLink floralink={floralinkData} onClose={handleClose} />
 * ```
 */

import { useState, useEffect } from 'react';
import AnimatedModal from './animated-modal.jsx';
import Input from './input.jsx';
import Button from './button.jsx';
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx';
import { MdEdit } from 'react-icons/md';
import { FiSave } from 'react-icons/fi';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';
import DevicesCard from './devices-card.jsx';
import { useFloraWebSocket } from './useWebSockets.jsx';
import { reqUnits, reqMeta } from '../util/snippets-values.jsx';

export default function EditFloraLink({ floralink, onClose }) {
  const [editingName, setEditingName] = useState(false);
  const [name, setName] = useState(floralink.name || 'Unnamed Device');
  const [nameError, setNameError] = useState('');
  const [savingName, setSavingName] = useState(false);
  const [selection, setSelection] = useState('current');
  const [dailyData, setDailyData] = useState(null);
  const [currentData, setCurrentData] = useState({
    humidity: floralink.humidity,
    temperature: floralink.temperature,
    soilMoisture: floralink.soilMoisture,
    lightLux: floralink.lightLux,
  });
  const [loadingDaily, setLoadingDaily] = useState(false);

  // WebSocket connection for real-time data
  useFloraWebSocket({
    onMessage: (data) => {
      if (data.sensorId === floralink.floraLinkId && data.sensorData) {
        const updatedData = {
          humidity: data.sensorData.find((d) => d.type === 'ENV_HUMIDITY')?.value,
          temperature: data.sensorData.find((d) => d.type === 'ENV_TEMPERATURE')?.value,
          soilMoisture: data.sensorData.find((d) => d.type === 'SOIL_MOISTURE')?.value,
          lightLux: data.sensorData.find((d) => d.type === 'LIGHT_LUX')?.value,
        };
        setCurrentData(updatedData);
      }
    },
  });

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, []);

  useEffect(() => {
    fetchDailyData();
  }, []);

  async function fetchDailyData() {
    setLoadingDaily(true);
    try {
      const response = await axios.get('/api/v1/floralink/get-all-daily-data');
      const deviceData = response.data.find((data) => data.sensorId === floralink.floraLinkId);
      setDailyData(deviceData || null);
    } catch (error) {
      console.error('Failed to fetch daily data:', error);
      setDailyData(null);
    } finally {
      setLoadingDaily(false);
    }
  }

  async function validateName() {
    if (!name.trim()) {
      setNameError('Device name cannot be empty');
      return false;
    }
    if (name.length > 50) {
      setNameError('Device name must be less than 50 characters');
      return false;
    }
    setNameError('');
    return true;
  }

  async function handleSaveName() {
    const isValid = await validateName();
    if (!isValid) return;

    setSavingName(true);
    try {
      const csrfToken = await getCsrfToken();
      await axios.put(
        '/api/v1/floralink/set-name',
        {
          id: floralink.floraLinkId,
          name: name
        },
        {
          headers: {
            'X-XSRF-TOKEN': csrfToken,
          },
          withCredentials: true,
        }
      );
      setEditingName(false);
    } catch (error) {
      console.error('Failed to save name:', error);
      setNameError('Failed to save name.');
    } finally {
      setSavingName(false);
    }
  }

  function handleSelectionChange(newSelection) {
    setSelection(newSelection);
  }

  function renderDailyData() {
    if (loadingDaily) {
      return <div className="text-green-700 text-center">Loading daily report...</div>;
    }

    if (!dailyData || !dailyData.sensorData) {
      return <div className="text-stone-400 text-center">No daily data available</div>;
    }

    const sensorTypeMap = {
      ENV_HUMIDITY: { label: 'Env. Humidity', metaIdx: 0 },
      LIGHT_LUX: { label: 'Light (lux)', metaIdx: 1 },
      SOIL_MOISTURE: { label: 'Soil Moisture', metaIdx: 2 },
      ENV_TEMPERATURE: { label: 'Temperature', metaIdx: 3 },
    };

    function formatTimestamp(timestamp) {
      if (!timestamp) return 'N/A';
      return new Date(timestamp).toLocaleTimeString();
    }

    return dailyData.sensorData.map((data) => {
      const typeInfo = sensorTypeMap[data.type];
      if (!typeInfo) return null;

      return (
        <div key={data.type} className="py-2 border-b border-gray-200 last:border-b-0">
          <div className="flex items-center justify-between mb-1">
            <div className="flex items-center gap-2">
              <span className="text-blue-500">{reqMeta[typeInfo.metaIdx]?.icon}</span>
              <span className="text-sm font-medium text-gray-700">{typeInfo.label}</span>
            </div>
            <div className="text-right">
              <div className="text-sm font-bold text-blue-600">
                {data.averageValue?.toFixed(1)}{reqUnits[typeInfo.metaIdx]}
              </div>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-2 text-xs text-gray-500">
            <div className="text-center">
              <span className="font-medium">Min: {data.minValue?.toFixed(1)}</span>
              <div>{formatTimestamp(data.minValueTimestamp)}</div>
            </div>
            <div className="text-center">
              <span className="font-medium">Max: {data.maxValue?.toFixed(1)}</span>
              <div>{formatTimestamp(data.maxValueTimestamp)}</div>
            </div>
          </div>
        </div>
      );
    }).filter(Boolean);
  }

  function renderCurrentData() {
    const sensorData = [
      { key: 'humidity', label: 'Env. Humidity', value: currentData.humidity, metaIdx: 0 },
      { key: 'light', label: 'Light (lux)', value: currentData.lightLux, metaIdx: 1 },
      { key: 'moisture', label: 'Soil Moisture', value: currentData.soilMoisture, metaIdx: 2 },
      { key: 'temperature', label: 'Temperature', value: currentData.temperature, metaIdx: 3 },
    ];

    return sensorData.map((sensor) => (
      <div key={sensor.key} className="flex items-center justify-between py-2 border-b border-gray-200 last:border-b-0">
        <div className="flex items-center gap-2">
          <span className="text-green-500">{reqMeta[sensor.metaIdx]?.icon}</span>
          <span className="text-sm font-medium text-gray-700">{sensor.label}</span>
        </div>
        <div className="text-right">
          <div className="text-sm font-bold text-green-600">
            {sensor.value !== null && sensor.value !== undefined
              ? sensor.value?.toFixed(1)
              : '--'}{reqUnits[sensor.metaIdx]}
          </div>
        </div>
      </div>
    ));
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <div className="absolute inset-0 bg-black/50" style={{ pointerEvents: 'auto' }} />
      <AnimatedModal>
        <div className="z-10 bg-white/90 rounded-xl p-3 sm:p-6 md:p-8 max-w-xl w-full flex flex-col items-center shadow-lg mx-2 overflow-y-auto max-h-[95vh] relative">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-2xl text-gray-500 hover:text-gray-700"
            aria-label="Close"
          >
            ×
          </button>
          <h2 className="mb-6 text-xl sm:text-2xl font-bold text-green-700">Edit FloraLink</h2>
          <div className="flex flex-col md:flex-row gap-8 w-full items-start justify-center">
            <DevicesCard
              id={floralink.floraLinkId}
              humidity={currentData.humidity}
              temperature={currentData.temperature}
              soilMoisture={currentData.soilMoisture}
              lightLux={currentData.lightLux}
              selection={selection}
              onSelectionChange={handleSelectionChange}
            />
            <div className="flex flex-col gap-6 w-full max-w-xs mt-4 md:mt-0">
              {/* Name Edit */}
              <div>
                <div className="flex items-center gap-2">
                  {editingName ? (
                    <Input
                      label="Device Name"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      errorMsg={nameError}
                      className={`${baseInputClass} ${noErrorClass}`}
                      autoFocus
                    />
                  ) : (
                    <span className="text-xl font-bold text-green-800">{name}</span>
                  )}
                  <Button
                    icon={editingName ? <FiSave /> : <MdEdit />}
                    buttonText={editingName ? 'Save' : ''}
                    className={`ml-2 px-2 py-1 rounded ${editingName ? 'bg-green-600 text-white' : 'bg-gray-200 text-green-700'}`}
                    onClick={editingName ? handleSaveName : () => setEditingName(true)}
                    disabled={savingName}
                  />
                </div>
              </div>

              {/* Static details */}
              <div className="flex flex-col gap-2 mt-4">
                <div>
                  <span className="block text-gray-700 font-semibold mb-1">Device ID</span>
                  <span className="text-green-900 font-mono">{floralink.floraLinkId}</span>
                </div>
                <div>
                  <span className="block text-gray-700 font-semibold mb-1">Connection Status</span>
                  <span className={`text-sm font-medium ${floralink.isOnline ? 'text-green-600' : 'text-red-600'}`}>
                    {floralink.isOnline ? 'Online' : 'Online'}
                  </span>
                </div>

              </div>

              {/* Daily Report Data */}
              <div className="mt-4">
                <span className="block text-gray-700 font-semibold mb-1">24h Report Data</span>
                <div className="bg-blue-50 rounded-lg p-3 border border-blue-200">
                  {renderDailyData()}
                </div>
              </div>

              {/* Current Real-time Data */}
              <div className="mt-4">
                <span className="block text-gray-700 font-semibold mb-1">Current Data (Live)</span>
                <div className="bg-green-50 rounded-lg p-3 border border-green-200">
                  {renderCurrentData()}
                </div>
              </div>
            </div>
          </div>
        </div>
      </AnimatedModal>
    </div>
  );
}