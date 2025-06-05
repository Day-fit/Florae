import { useEffect, useState } from 'react';
import axios from 'axios';
import { reqUnits, reqMeta } from '../util/snippets-values.jsx';

export default function DevicesCard({ id }) {
  const [plant, setPlant] = useState(null);
  const [sensorData, setSensorData] = useState(null);

  useEffect(() => {
    const fetchPlant = async () => {
      try {
        const response = await axios.get('/api/v1/plants');
        const connectedPlant = response.data.find(
          p => p.linkedFloraLink.floraLinkId === id
        );
        setPlant(connectedPlant || null);
      } catch (error) {
        console.error('Failed to fetch plants:', error);
        setPlant(null);
      }
    };
    fetchPlant();
  }, [id]);

  useEffect(() => {
    let interval;
    const fetchSensorData = async () => {
      try {
        const response = await axios.get(`/api/v1/floralink/get-all-daily-data`, { withCredentials: true });
        setSensorData(response.data);
      } catch (error) {
        console.error('Failed to fetch sensor data:', error);
        setSensorData(null);
      }
    };

    fetchSensorData();
    interval = setInterval(fetchSensorData, 1000000);

    return () => clearInterval(interval);
  }, [id]);

  function getImgSrc(plant) {
    if (!plant || !plant.primaryPhoto) return null;
    if (plant.primaryPhoto.startsWith('http') || plant.primaryPhoto.startsWith('data:')) {
      return plant.primaryPhoto;
    } else {
      return `data:image/jpeg;base64,${plant.primaryPhoto}`;
    }
  }

  // Demo: sample min/max requirements per sensor
  const reqThresholds = [
    { key: "moisture", min: 30, max: 70 },
    { key: "temperature", min: 15, max: 30 },
    { key: "light", min: 200, max: 2000 },
    { key: "humidity", min: 30, max: 60 }
  ];

  function renderRequirements() {
    return reqThresholds.map((req, i) => {
      const value = sensorData ? sensorData[req.key] : null;
      const label = reqMeta[i].label;
      return {
        key: req.key,
        value: value !== undefined && value !== null ? value : '--',
        label,
        min: req.min,
        max: req.max,
        unit: reqUnits[i] || '',
        metaIdx: i
      };
    });
  }

  function isValueGood(val, min, max) {
    if (val === '--' || val === null || val === undefined) return false;
    return val >= min && val <= max;
  }

  return (
    <div className="rounded-3xl bg-white w-[350px] shadow-lg flex flex-col items-center border-2 border-stone-400 transition-transform duration-200 hover:scale-[1.025] hover:shadow-2xl min-h-[460px]">
      <div className="flex flex-col items-center px-5 pt-7 pb-4 w-full h-full">
        <h2 className="font-bold text-2xl text-center truncate w-full">
          {plant ? plant.name : 'No plant connected'}
        </h2>
        <div className="w-40 h-40 bg-stone-200 rounded-2xl mb-5 overflow-hidden flex items-center justify-center">
          {plant && plant.primaryPhoto ? (
            <img
              src={getImgSrc(plant)}
              alt={plant.name}
              className="object-cover w-full h-full"
            />
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
            const requirements = renderRequirements();
            const rows = [];
            for (let i = 0; i < requirements.length; i += 2) {
              rows.push(requirements.slice(i, i + 2));
            }
            return rows.map((row, rowIdx) => (
              <div className="flex flex-row justify-center gap-1 w-full" key={rowIdx}>
                {row.map((req, idx) => {
                  const realIdx = rowIdx * 2 + idx;
                  const isGood = isValueGood(req.value, req.min, req.max);
                  return (
                    <div
                      key={realIdx}
                      className={`flex flex-col items-center justify-center h-[10vh] w-[4vw] rounded-xl shadow-lg border-2 mx-auto my-2 min-w-24 min-h-28
                        ${isGood ? 'border-green-500 bg-green-50' : 'border-red-500 bg-red-400'}
                      `}
                    >
                      <div className="flex items-center justify-center pt-4">
                        <span className={isGood ? "text-green-500" : "text-red-800"}>
                          {reqMeta[req.metaIdx].icon}
                        </span>
                      </div>
                      <div className="flex items-center justify-center">
                        <span className="text-2xl font-bold">
                          {req.value}
                          {req.unit}
                        </span>
                      </div>
                      <div className={`pb-3 pt-2 text-xs font-medium ${reqMeta[req.metaIdx].labelColor}`}>
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
