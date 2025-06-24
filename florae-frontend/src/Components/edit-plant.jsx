import { useState, useEffect } from 'react';
import AnimatedModal from './animated-modal.jsx';
import PlantCard from './plant-card.jsx';
import Input from './input.jsx';
import Button from './button.jsx';
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx';
import { MdEdit } from 'react-icons/md';
import { FiSave } from 'react-icons/fi';
import { MdDelete } from 'react-icons/md';
import useCreatePlant from './useCreatePlant.jsx';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';

export default function EditPlant({ plant, onClose, currentSelection = 'optimal', onSelectionChange }) {
  const [editingName, setEditingName] = useState(false);
  const [editingVolume, setEditingVolume] = useState(false);
  const [name, setName] = useState(plant.name);
  const [volume, setVolume] = useState(plant.volume || '');
  const [nameError, setNameError] = useState('');
  const [volumeError, setVolumeError] = useState('');
  const [savingName, setSavingName] = useState(false);
  const [savingVolume, setSavingVolume] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [selection, setSelection] = useState(currentSelection);

  // Use the shared logic from useCreatePlant
  const { setPlantName, setPlantVolume } = useCreatePlant({ onClose: () => {} });

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, []);

  function handleSelectionChange(newSelection) {
    setSelection(newSelection);
    if (onSelectionChange) {
      onSelectionChange(newSelection);
    }
  }

  async function handleSaveName() {
    setSavingName(true);
    setNameError('');
    try {
      await setPlantName(plant.id, name);
      setEditingName(false);
      // eslint-disable-next-line no-unused-vars
    } catch (e) {
      setNameError('Failed to save name.');
    } finally {
      setSavingName(false);
    }
  }

  async function handleSaveVolume() {
    setSavingVolume(true);
    setVolumeError('');
    try {
      await setPlantVolume(plant.id, volume);
      setEditingVolume(false);
      // eslint-disable-next-line no-unused-vars
    } catch (e) {
      setVolumeError('Failed to save volume.');
    } finally {
      setSavingVolume(false);
    }
  }

  async function handleDeletePlant() {
    setDeleting(true);
    try {
      const csrfToken = await getCsrfToken();
      await axios.delete(`/api/v1/delete-plant/${plant.id}`, {
        headers: {
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      });
      onClose();
    } catch (error) {
      console.error('Failed to delete plant:', error);
      alert('Failed to delete plant. Please try again.');
    } finally {
      setDeleting(false);
      setShowDeleteConfirm(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <div className="absolute inset-0 bg-black/50" style={{ pointerEvents: 'auto' }} />
      <AnimatedModal>
        <div className="z-10 bg-white/90 rounded-xl p-4 md:p-10 max-w-2xl w-full flex flex-col items-center shadow-lg mx-2 relative"
             style={{ maxWidth: '95vw', width: '100%', minWidth: 0 }}>
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-2xl text-gray-500 hover:text-gray-700"
            aria-label="Close"
          >
            ×
          </button>
          <h2 className="mb-6 text-2xl font-bold text-green-700">Edit Plant</h2>
          <div className="flex flex-col md:flex-row gap-8 w-full items-start justify-center">
            <PlantCard
              guestName={name}
              primaryPhoto={plant.primaryPhoto}
              speciesName={plant.speciesName}
              requirements={plant.requirements}
              selection={selection}
              onSelectionChange={handleSelectionChange}
            />
            <div className="flex flex-col gap-6 w-full max-w-xs mt-4 md:mt-0">
              {/* Name Edit */}
              <div>
                <div className="flex items-center gap-2">
                  {editingName ? (
                    <Input
                      label="Plant Name"
                      value={name}
                      onChange={e => setName(e.target.value)}
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
                {nameError && <p className="text-red-700 text-sm mt-1">{nameError}</p>}
              </div>
              {/* Volume Edit */}
              <div>
                <div className="flex items-center gap-2">
                  {editingVolume ? (
                    <Input
                      label="Pot Volume (L)"
                      value={volume}
                      onChange={e => setVolume(e.target.value)}
                      errorMsg={volumeError}
                      type="number"
                      min="0.01"
                      step="0.01"
                      className={`${baseInputClass} ${noErrorClass}`}
                      autoFocus
                    />
                  ) : (
                    <span className="text-lg text-green-900">Pot Volume: <b>{volume}</b> L</span>
                  )}
                  <Button
                    icon={editingVolume ? <FiSave /> : <MdEdit />}
                    buttonText={editingVolume ? 'Save' : ''}
                    className={`ml-2 px-2 py-1 rounded ${editingVolume ? 'bg-green-600 text-white' : 'bg-gray-200 text-green-700'}`}
                    onClick={editingVolume ? handleSaveVolume : () => setEditingVolume(true)}
                    disabled={savingVolume}
                  />
                </div>
                {volumeError && <p className="text-red-700 text-sm mt-1">{volumeError}</p>}
              </div>
              {/* Static details */}
              <div className="flex flex-col gap-2 mt-4">
                <div>
                  <span className="block text-gray-700 font-semibold mb-1">Photo</span>
                  <div className="w-40 h-40 bg-stone-200 rounded-2xl overflow-hidden flex items-center justify-center">
                    <img
                      src={`data:image/png;base64,${plant.primaryPhoto}`}
                      alt={plant.speciesName}
                      className="object-cover w-full h-full"
                    />
                  </div>
                </div>
                <div>
                  <span className="block text-gray-700 font-semibold mb-1">Species</span>
                  <span className="text-green-900">{plant.speciesName}</span>
                </div>
                <div>
                  <span className="block text-gray-700 font-semibold mb-1">Requirements</span>
                  <ul className="text-sm text-gray-800 list-disc ml-5">
                    <li>Env. Humidity: {plant.requirements.min_env_humid} - {plant.requirements.max_env_humid}%</li>
                    <li>Light: {plant.requirements.min_light_lux} - {plant.requirements.max_light_lux} lux</li>
                    <li>Soil Moisture: {plant.requirements.min_soil_moist} - {plant.requirements.max_soil_moist}%</li>
                    <li>Temperature: {plant.requirements.min_temp} - {plant.requirements.max_temp}°C</li>
                  </ul>
                </div>
              </div>
              {/* Delete Plant Button */}
              <div className="mt-4">
                <Button
                  icon={<MdDelete />}
                  buttonText="Delete Plant"
                  className="w-full bg-red-600 text-white hover:bg-red-700 px-4 py-2 rounded-lg font-medium"
                  onClick={() => setShowDeleteConfirm(true)}
                  disabled={deleting}
                />
              </div>
            </div>
          </div>
        </div>
      </AnimatedModal>

      {/* Delete Confirmation Modal */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 z-60 flex items-center justify-center bg-black/80">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-xl font-bold text-red-700 mb-4">Delete Plant</h3>
            <p className="text-gray-700 mb-6">
              Are you sure you want to delete "{name}"? This action cannot be undone.
            </p>
            <div className="flex gap-3 justify-end">
              <Button
                buttonText="Cancel"
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400"
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleting}
              />
              <Button
                buttonText={deleting ? "Deleting..." : "Delete"}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                onClick={handleDeletePlant}
                disabled={deleting}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}