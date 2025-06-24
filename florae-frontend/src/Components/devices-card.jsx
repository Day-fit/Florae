/**
 * DevicesCard displays information and controls for an individual device.
 *
 * Props:
 * - device (object): The device data to display.
 *
 * Usage:
 * ```
 * <DevicesCard device={deviceData} />
 * ```
 *
 * Note:
 * - Can be used as part of a list/grid of devices on a devices overview page.
 */

import { useEffect, useState } from 'react';
import axios from 'axios';
import { reqUnits, reqMeta } from '../util/snippets-values.jsx';

function getRequirementRows(requirements) {
  if (!requirements) return [];

  const attrs = [
    {
      label: 'Env. Humidity',
      minKey: 'min_env_humid',
      maxKey: 'max_env_humid',
      key: 'humidity',
      metaIdx: 0,
    },
    {
      label: 'Light (lux)',
      minKey: 'min_light_lux',
      maxKey: 'max_light_lux',
      key: 'light',
      metaIdx: 1,
    },
    {
      label: 'Soil Moisture',
      minKey: 'min_soil_moist',
      maxKey: 'max_soil_moist',
      key: 'moisture',
      metaIdx: 2,
    },
    {
      label: 'Temperature',
      minKey: 'min_temp',
      maxKey: 'max_temp',
      key: 'temperature',
      metaIdx: 3,
    },
  ];

  return attrs.map((attr) => {
    const minVal = requirements[attr.minKey];
    const maxVal = requirements[attr.maxKey];
    return {
      label: attr.label,
      key: attr.key,
      min: minVal,
      max: maxVal,
      unit: reqUnits[attr.metaIdx] || '',
      metaIdx: attr.metaIdx,
    };
  });
}

export default function DevicesCard({ id, humidity, lightLux, temperature, soilMoisture }) {
  const [plant, setPlant] = useState(null);

  const sensorData = {
    humidity,
    temperature,
    light: lightLux,
    moisture: soilMoisture,
  };

  useEffect(() => {
    const fetchPlant = async () => {
      try {
        const response = await axios.get('/api/v1/plants');
        const connectedPlant = response.data.find((p) => p.linkedFloraLink.floraLinkId === id);
        setPlant(connectedPlant || null);
        // eslint-disable-next-line no-unused-vars
      } catch (error) {
        setPlant(null);
      }
    };
    fetchPlant();
  }, [id]);

  function getImgSrc(plant) {
    if (!plant || !plant.primaryPhoto) return null;
    if (plant.primaryPhoto.startsWith('http') || plant.primaryPhoto.startsWith('data:')) {
      return plant.primaryPhoto;
    } else {
      return `data:image/jpeg;base64,${plant.primaryPhoto}`;
    }
  }

  function isValueGood(val, min, max) {
    if (val === '--' || val === null || val === undefined) return false;
    return val >= min && val <= max;
  }

  // Only use min/max for thresholds
  const thresholds = getRequirementRows(plant?.requirements);

  return (
    <div className="rounded-3xl bg-white w-[350px] shadow-lg flex flex-col items-center border-2 border-stone-400 transition-transform duration-200 hover:scale-[1.025] hover:shadow-2xl min-h-[460px]">
      <div className="flex flex-col items-center px-5 pt-7 pb-4 w-full h-full">
        <h2 className="font-bold text-2xl text-center truncate w-full">
          {plant ? plant.name : 'No plant connected'}
        </h2>
        <div className="w-40 h-40 bg-stone-200 rounded-2xl mb-5 overflow-hidden flex items-center justify-center">
          {plant && plant.primaryPhoto ? (
            <img src={getImgSrc(plant)} alt={plant.name} className="object-cover w-full h-full" />
          ) : (
            <span className="text-stone-400 text-5xl">🌱</span>
          )}
        </div>
        {plant && (
          <p className="text-stone-400 font-medium text-center truncate w-full mt-1 mb-2">
            {plant.speciesName}
          </p>
        )}

        <div className="flex flex-col items-center gap-2 mt-4 w-full">
          {(() => {
            const rows = [];
            for (let i = 0; i < thresholds.length; i += 2) {
              rows.push(thresholds.slice(i, i + 2));
            }
            return rows.map((row, rowIdx) => (
              <div className="flex flex-row justify-center gap-1 w-full" key={rowIdx}>
                {row.map((req) => {
                  const sensorValue =
                    sensorData && sensorData[req.key] != null ? sensorData[req.key] : '--';
                  const isGood = isValueGood(sensorValue, req.min, req.max);

                  return (
                    <div
                      key={req.key}
                      className={`flex flex-col items-center justify-center h-[10vh] w-[9vw] rounded-xl shadow-lg border-2 mx-auto my-2 min-w-28 min-h-28
                              ${isGood ? 'border-green-500 bg-green-50' : 'border-red-500 bg-red-400'}
                            `}
                    >
                      <div className="flex items-center justify-center pt-4">
                        <span className={isGood ? 'text-green-500' : 'text-red-800'}>
                          {reqMeta[req.metaIdx]?.icon}
                        </span>
                      </div>
                      <div className="flex items-center justify-center">
                        <span className="text-2xl font-bold">
                          {sensorValue}
                          {req.unit}
                        </span>
                      </div>
                      <div
                        className={`pb-3 pt-2 text-xs font-medium ${isGood ? 'text-green-600' : 'text-red-800'}`}
                      >
                        {req.label}
                      </div>
                    </div>
                  );
                })}
              </div>
            ));
          })()}
        </div>
      </div>
    </div>
  );
}
