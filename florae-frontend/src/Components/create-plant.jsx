/**
 * CreatePlant component renders a modal form to create a new plant entry with a name and photos.
 *
 * @component
 *
 * @param {Object} props
 * @param {Function} props.onClose - Callback function called to close the modal.
 *
 * @returns {JSX.Element} The CreatePlant modal form component.
 *
 * @example
 * <CreatePlant onClose={() => setShowCreatePlant(false)} />
 *
 * @description
 * - Allows user to input a plant name and select 1 to 5 photos.
 * - Validates photo count before submission.
 * - Sends photos and name to server endpoints with CSRF protection.
 * - Shows errors if validation or submission fails.
 * - Disables submit button while submitting.
 */

import useCreatePlant from "./useCreatePlant.jsx";
import AnimatedModal from './animated-modal.jsx';
import Input from './input.jsx';
import Button from './button.jsx';
import CloseButton, { baseInputClass, noErrorClass } from './close-button.jsx';
import { useRef, useEffect } from 'react';

export default function CreatePlant({ onClose }) {
  const { nameRef, fileRef, errors, submitting, handleSubmit } = useCreatePlant({ onClose });
  const volumeRef = useRef();

  // Wrap handleSubmit to include volume
  function handleSubmitWithVolume(e) {
    e.preventDefault();
    // Attach volume to the ref for useCreatePlant
    if (volumeRef.current) {
      // Volume is passed directly to handleSubmit
    }
    handleSubmit(e, volumeRef.current?.value);
  }

  useEffect(() => {
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = '';
    };
  }, []);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80">
      <div className="absolute inset-0 bg-black/50" style={{ pointerEvents: 'auto' }} />
      <AnimatedModal>
        <div className="z-10 bg-white/90 rounded-xl p-4 md:p-10 max-w-lg w-full flex flex-col items-center shadow-lg mx-2 relative"
             style={{ maxWidth: '95vw', width: '100%', minWidth: 0 }}>
          <button
            onClick={onClose}
            className="absolute top-4 right-4 text-2xl text-gray-500 hover:text-gray-700"
            aria-label="Close"
          >
            ×
          </button>
          <h2 className="mb-6 text-2xl font-bold text-green-700">Create Plant</h2>
          <form onSubmit={handleSubmitWithVolume} className="space-y-4 w-full" autoComplete="off">
            <div>
              <Input
                label="Name"
                type="text"
                ref={nameRef}
                errorMsg={errors.name}
                className={`${baseInputClass} ${noErrorClass}`}
                autoFocus
                required
              />
            </div>
            <div>
              <Input
                label="Pot Volume (L)"
                type="number"
                ref={volumeRef}
                min="0.01"
                step="0.01"
                errorMsg={errors.volume}
                className={`${baseInputClass} ${noErrorClass}`}
                required
              />
            </div>
            <div>
              <label className="block font-medium mb-1">Photo(s)</label>
              <input
                type="file"
                ref={fileRef}
                accept="image/*"
                capture="environment"
                className="block w-full"
                required
                multiple
              />
              {errors.file && <p className="text-red-500 text-xs mt-1">{errors.file}</p>}
            </div>
            {errors.submit && <p className="text-red-500 text-xs mt-1">{errors.submit}</p>}
            <div className="flex flex-row justify-between mt-4 w-full">
              <div className="flex justify-start">
                <Button
                  buttonText={submitting ? 'Creating...' : 'Create'}
                  type="submit"
                  className="max-w-lg text-white bg-green-700 text-center rounded-lg pt-2 pb-2 px-20"
                  disabled={submitting}
                />
              </div>
              <div className="flex justify-end">
                <CloseButton onClick={onClose} />
              </div>
            </div>
          </form>
        </div>
      </AnimatedModal>
    </div>
  );
}
