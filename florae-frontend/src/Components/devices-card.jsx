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
  const [selection, setSelection] = useState('current');
  const [dailyReport, setDailyReport] = useState(null);
  const [loading, setLoading] = useState(false);

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

  useEffect(() => {
    const fetchDailyReport = async () => {
      if (selection === 'daily' && plant) {
        setLoading(true);
        try {
          const response = await axios.get('/api/v1/floralink/get-all-daily-data');
          console.log(response.data);
          const deviceReport = response.data.find((report) => report.sensorId === id);
          setDailyReport(deviceReport || null);
        } catch (error) {
          console.error('Failed to fetch daily report:', error);
          setDailyReport(null);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchDailyReport();
  }, [selection, plant, id]);

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

  function handleOptionChange(e) {
    e.stopPropagation();
    setSelection(e.target.value);
  }

  function renderSensorData() {
    if (selection === 'current') {
      return [
        {
          label: 'Env. Humidity',
          key: 'humidity',
          value: sensorData.humidity,
          metaIdx: 0,
        },
        {
          label: 'Light (lux)',
          key: 'light',
          value: sensorData.light,
          metaIdx: 1,
        },
        {
          label: 'Soil Moisture',
          key: 'moisture',
          value: sensorData.moisture,
          metaIdx: 2,
        },
        {
          label: 'Temperature',
          key: 'temperature',
          value: sensorData.temperature,
          metaIdx: 3,
        },
      ];
    } else {
      // daily report
      if (!dailyReport || !dailyReport.sensorData) {
        return [];
      }

      const sensorTypeMap = {
        ENV_HUMIDITY: { label: 'Env. Humidity', metaIdx: 0 },
        LIGHT_LUX: { label: 'Light (lux)', metaIdx: 1 },
        SOIL_MOISTURE: { label: 'Soil Moisture', metaIdx: 2 },
        ENV_TEMPERATURE: { label: 'Temperature', metaIdx: 3 },
      };

      return dailyReport.sensorData
        .map((data) => {
          const typeInfo = sensorTypeMap[data.type];
          if (!typeInfo) return null;

          return {
            label: typeInfo.label,
            key: data.type.toLowerCase(),
            value: data.averageValue,
            min: data.minValue,
            max: data.maxValue,
            metaIdx: typeInfo.metaIdx,
            isDaily: true,
          };
        })
        .filter(Boolean);
    }
  }

  // Only use min/max for thresholds
  const thresholds = getRequirementRows(plant?.requirements);
  const sensorDataToRender = renderSensorData();

  return (
    <div className="w-full max-w-[340px] min-w-[220px] sm:w-[22vw] sm:max-w-[320px] sm:min-w-[220px] border-stone-400 rounded-2xl border-2 flex flex-col items-center justify-center min-h-150 bg-white shadow-lg mx-auto my-4 p-4 transition-transform duration-200 hover:scale-105">
      {/* Plant name centered */}
      <div className="flex justify-center mt-3 mb-2 w-full">
        <h1 className="text-green-700 text-xl sm:text-2xl font-bold text-center w-full break-words">
          {plant ? plant.name : 'No plant connected'}
        </h1>
      </div>
      {/* Image centered */}
      <div className="w-full h-36 sm:h-40 bg-stone-200 rounded-2xl mb-4 overflow-hidden flex items-center justify-center">
        {plant && plant.primaryPhoto ? (
          <img src={getImgSrc(plant)} alt={plant.name} className="object-cover w-full h-full" />
        ) : (
          <span className="text-stone-400 text-5xl">🌱</span>
        )}
      </div>
      {/* Species name centered */}
      {plant && (
        <div className="flex justify-center font-bold w-full mb-2">
          <h1 className="text-center w-full break-words text-base sm:text-lg">
            {plant.speciesName}
          </h1>
        </div>
      )}
      {/* Select centered */}
      <div className="flex justify-center w-full mb-2">
        <select
          className="border w-[70%] sm:w-[60%] border-stone-400 rounded-lg px-2 py-2 text-base font-medium focus:outline-none focus:ring-2 focus:ring-green-400 bg-green-50 text-green-700 transition hover:bg-green-10"
          value={selection}
          onChange={handleOptionChange}
          onClick={(e) => e.stopPropagation()}
        >
          <option value="current">Current Report</option>
          <option value="daily">Daily Report</option>
        </select>
      </div>
      {/* Sensor data grid, centered */}
      <div className="flex flex-col items-center gap-2 w-full">
        {(() => {
          if (loading) {
            return (
              <div className="flex justify-center items-center h-32">
                <div className="text-green-700">Loading daily report...</div>
              </div>
            );
          }

          if (sensorDataToRender.length === 0) {
            return (
              <div className="flex justify-center items-center h-32">
                <div className="text-stone-400">No data available</div>
              </div>
            );
          }

          const rows = [];
          for (let i = 0; i < sensorDataToRender.length; i += 2) {
            rows.push(sensorDataToRender.slice(i, i + 2));
          }
          return rows.map((row, rowIdx) => (
            <div className="flex flex-row justify-center gap-2 sm:gap-4 w-full" key={rowIdx}>
              {row.map((sensor) => {
                const threshold = thresholds.find((t) => t.key === sensor.key);
                const isGood = threshold
                  ? isValueGood(sensor.value, threshold.min, threshold.max)
                  : true;
                const isDaily = sensor.isDaily;

                return (
                  <div
                    key={sensor.key}
                    className={`flex flex-col items-center justify-center h-[120px] sm:h-[140px] w-[45%] sm:w-[40%] md:w-[35%] rounded-xl shadow-lg border-2 mx-auto my-2 ${
                      isDaily
                        ? 'border-blue-500 bg-blue-50'
                        : isGood
                          ? 'border-green-500 bg-green-50'
                          : 'border-red-500 bg-red-50'
                    }`}
                  >
                    <div className="flex items-center justify-center pt-4">
                      <span
                        className={
                          isDaily ? 'text-blue-500' : isGood ? 'text-green-500' : 'text-red-500'
                        }
                      >
                        {reqMeta[sensor.metaIdx]?.icon}
                      </span>
                    </div>
                    <div className="flex flex-col xs:flex-row items-center justify-center break-words w-full px-2">
                      <span className="text-base sm:text-lg md:text-xl font-bold text-center truncate max-w-full">
                        {sensor.value !== null && sensor.value !== undefined
                          ? sensor.value?.toFixed(1)
                          : '--'}
                        {sensor.value !== null &&
                        sensor.value !== undefined &&
                        String(sensor.value?.toFixed(1)).length <= 5
                          ? reqUnits[sensor.metaIdx]
                          : ''}
                      </span>
                      {sensor.value !== null &&
                        sensor.value !== undefined &&
                        String(sensor.value?.toFixed(1)).length > 5 && (
                          <span className="text-base sm:text-lg md:text-xl font-bold text-center ml-0 xs:ml-1 truncate max-w-full">
                            {reqUnits[sensor.metaIdx]}
                          </span>
                        )}
                    </div>
                    <div
                      className={`pb-3 pt-2 text-xs font-medium ${
                        isDaily ? 'text-blue-600' : isGood ? 'text-green-600' : 'text-red-600'
                      }`}
                    >
                      {sensor.label}
                    </div>
                  </div>
                );
              })}
            </div>
          ));
        })()}
      </div>
    </div>
  );
}
