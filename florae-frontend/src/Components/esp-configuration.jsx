import { useEffect, useRef, useState } from 'react'
import Input from './input.jsx'
import Button from './button.jsx'
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx'
import axios from 'axios'
import getCsrfToken from '../util/getCsrfToken.js'

export default function EspConfiguration({ onClose }) {
  const ssidRef = useRef(null)
  const passwordRef = useRef(null)
  const [plants, setPlants] = useState([])
  const [selectedPlant, setSelectedPlant] = useState('')

  const ESP_SERVICE_UUID = '53020f00-319c-4d97-a2b1-9e706baba77a'
  const BLE_CHARACTERISTIC_UUID = 'f87709b3-63a7-4605-9bb5-73c383462296'

  const connectToEsp = async (ssid, password) => {
    if (!selectedPlant) return

    let apiKey

    try {
      const csrfToken = await getCsrfToken()
      const { data } = await axios.post(
          '/api/v1/generate-key',
          { plantId: selectedPlant },
          {
            withCredentials: true,
            headers: { 'X-XSRF-TOKEN': csrfToken },
          }
      )

      apiKey = data.apiKey

      await axios.post(
          '/api/v1/connect-api',
          {},
          {
            withCredentials: true,
            headers: {
              'X-XSRF-TOKEN': await getCsrfToken(),
              'X-API-KEY': apiKey,
            },
          }
      )
    } catch (err) {
      console.error('API key error:', err)
      return
    }

    try {
      const device = await navigator.bluetooth.requestDevice({
        filters: [{ namePrefix: 'FloraLink' }],
        optionalServices: [ESP_SERVICE_UUID],
      })

      const server = await device.gatt.connect()
      const service = await server.getPrimaryService(ESP_SERVICE_UUID)
      const characteristic = await service.getCharacteristic(BLE_CHARACTERISTIC_UUID)

      const payload = {
        wifi_ssid: ssid,
        wifi_password: password,
        api_key: apiKey,
      }

      const encoded = new TextEncoder().encode(JSON.stringify(payload))
      await characteristic.writeValue(encoded)
    } catch (err) {
      console.error('BLE error:', err)
    }
  }

  const handleSubmit = (e) => {
    e.preventDefault()

    const ssid = ssidRef.current?.value?.trim()
    const password = passwordRef.current?.value?.trim()

    if (!ssid || !password || !selectedPlant) return

    connectToEsp(ssid, password)
  }

  useEffect(() => {
    const load = async () => {
      try {
        const { data } = await axios.get('/api/v1/plants', { withCredentials: true })
        setPlants(data)
      } catch (err) {
        console.error('Plant list error:', err)
      }
    }

    load()
  }, [])

  return (
      <div className="z-100 bg-white/90 rounded-xl p-10 max-w-lg w-full flex flex-col items-center shadow-lg mx-2">
        <h2 className="mb-6 text-2xl font-bold text-green-700">FloraLink Configuration</h2>
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
            <label htmlFor="plant-select" className="block mb-1 font-bold">
              Plant
            </label>
            <select
                id="plant-select"
                className={`${baseInputClass} ${noErrorClass}`}
                value={selectedPlant}
                onChange={(e) => setSelectedPlant(e.target.value)}
                required
            >
              <option value="" disabled>
                Select a plant
              </option>
              {plants.map((plant) => (
                  <option key={plant.id} value={plant.id}>
                    {plant.name}
                  </option>
              ))}
            </select>
          </div>
          <div className="flex flex-row justify-between mt-4 w-full">
            <Button
                buttonText="Connect"
                type="submit"
                className="max-w-lg text-white bg-green-700 text-center rounded-lg pt-2 pb-2 px-20"
            />
            <CloseButton onClick={onClose} />
          </div>
        </form>
      </div>
  )
}
