/**
 * EspConfiguration component handles WiFi and ESP device setup.
 * @param {Object} props
 * @param {() => void} props.onClose - Callback to close the configuration modal.
 */
import { useEffect, useRef, useState } from 'react'
import Input from './input.jsx'
import Button from './button.jsx'
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx'
import axios from 'axios'
import getCsrfToken from '../util/getCsrfToken.js'
import { espConfigSchema } from '../util/form-validiation.js'

export default function EspConfiguration({ onClose }) {
  const ssidRef = useRef(null)
  const passwordRef = useRef(null)
  const [plants, setPlants] = useState([])
  const [selectedPlant, setSelectedPlant] = useState('')
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  const ESP_SERVICE_UUID = '53020f00-319c-4d97-a2b1-9e706baba77a'
  const WIFI_CREDENTIALS_CHAR_UUID = 'f87709b3-63a7-4605-9bb5-73c383462296'

  /**
   * Sends WiFi credentials and API key to the ESP device over BLE.
   * @param {BluetoothRemoteGATTService} service
   * @param {string} wifi_ssid
   * @param {string} wifi_password
   * @param {string} api_key
   */
  async function sendWiFiCredentials(service, wifi_ssid, wifi_password, api_key) {
    const characteristic = await service.getCharacteristic(WIFI_CREDENTIALS_CHAR_UUID)
    const payload = JSON.stringify({ wifi_ssid, wifi_password, api_key })
    await characteristic.writeValue(new TextEncoder().encode(payload))
  }

  /**
   * Connects to the ESP device, generates an API key, and sends credentials.
   * @param {string} ssid
   * @param {string} password
   */
  async function connectToEsp(ssid, password) {
    if (!selectedPlant) return

    let apiKey

    try {
      const csrfToken = await getCsrfToken()
      const { data } = await axios.post(
        '/api/v1/generate-key',
        { plantId: selectedPlant },
        { withCredentials: true, headers: { 'X-XSRF-TOKEN': csrfToken } }
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
      await sendWiFiCredentials(service, ssid, password, apiKey)
    } catch (err) {
      console.error('BLE error:', err)
    }
  }

  /**
   * Validates input, triggers ESP connection flow.
   * @param {Event} e
   */
  async function handleSubmit(e) {
    e.preventDefault()
    setErrors({})
    setSubmitting(true)

    const wifiSsid = ssidRef.current?.value?.trim()
    const wifiPassword = passwordRef.current?.value?.trim()

    try {
      await espConfigSchema.validate(
        { wifiSsid, wifiPassword, selectedPlant },
        { abortEarly: false }
      )
    } catch (validationError) {
      const fieldErrors = {}
      validationError.inner.forEach(({ path, message }) => {
        fieldErrors[path] = message
      })
      setErrors(fieldErrors)
      setSubmitting(false)
      return
    }

    await connectToEsp(wifiSsid, wifiPassword)
    setSubmitting(false)
  }

  useEffect(() => {
    async function loadPlants() {
      try {
        const { data } = await axios.get('/api/v1/plants', { withCredentials: true })
        setPlants(data)
      } catch (err) {
        console.error('Plant list error:', err)
      }
    }

    loadPlants()
  }, [])

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
            autoComplete="off"
            className={`${baseInputClass} ${errors.wifiSsid ? 'border-red-500' : noErrorClass}`}
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
            autoComplete="off"
            className={`${baseInputClass} ${errors.wifiPassword ? 'border-red-500' : noErrorClass}`}
            errorMsg={errors.wifiPassword}
          />
        </div>
        <div className="mb-4">
          <label htmlFor="plant-select" className="block mb-1 font-bold">Plant</label>
          <select
            id="plant-select"
            required
            value={selectedPlant}
            onChange={(e) => setSelectedPlant(e.target.value)}
            className={`${baseInputClass} ${errors.selectedPlant ? 'border-red-500' : noErrorClass}`}
          >
            <option value="" disabled>Select a plant</option>
            {plants.map(({ id, name }) => (
              <option key={id} value={id}>{name}</option>
            ))}
          </select>
          {errors.selectedPlant && <p className="text-red-800 text-sm mt-1">{errors.selectedPlant}</p>}
        </div>
        <div className="flex justify-between mt-4 w-full">
          <Button
            type="submit"
            buttonText={submitting ? 'Connecting...' : 'Connect'}
            disabled={submitting}
            className="max-w-lg text-white bg-green-700 rounded-lg py-2 px-20"
          />
          <CloseButton onClick={onClose} />
        </div>
      </form>
    </div>
  )
}
