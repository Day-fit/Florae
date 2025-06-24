import { FaSeedling, FaSun, FaTemperatureHigh, FaTint } from 'react-icons/fa';

export const reqMeta = [
  {
    icon: <FaTint size={36} />,
    color: 'bg-red-400 border-red-600',
    labelColor: 'text-red-700',
  }, // Env. Humidity - Red
  { icon: <FaSun size={36} />, color: 'bg-white border-gray-400', labelColor: 'text-gray-800' }, // Light - White/Gray
  {
    icon: <FaSeedling size={36} />,
    color: 'bg-green-700 border-green-900',
    labelColor: 'text-green-100',
  }, // Soil Moisture - Dark Green
  {
    icon: <FaTemperatureHigh size={36} />,
    color: 'bg-yellow-100 border-yellow-400',
    labelColor: 'text-yellow-700',
  }, // Temperature
];
export const reqUnits = [
  '%', // Env. Humidity (percent)
  'lux', // Light
  '%', // Soil Moisture (percent)
  '°C', // Temperature (Celsius)
];
