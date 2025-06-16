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

export default function CreatePlant({ onClose }) {
  const { nameRef, fileRef, errors, submitting, handleSubmit } = useCreatePlant({ onClose });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <div className="bg-white rounded-lg shadow-lg w-full max-w-md p-6 relative">
        <button
          onClick={onClose}
          className="absolute top-2 right-2 p-1 text-gray-500 hover:text-gray-700"
        >
          ×
        </button>
        <h2 className="text-xl font-semibold mb-4">Create Plant</h2>
        <form onSubmit={handleSubmit} className="space-y-4" autoComplete="off">
          <div>
            <label className="block font-medium">Name</label>
            <input
              type="text"
              ref={nameRef}
              className="w-full px-3 py-2 border rounded"
              autoFocus
              required
            />
            {errors.name && <p className="text-red-500 text-xs">{errors.name}</p>}
          </div>
          <div>
            <label className="block font-medium">Photo(s)</label>
            <input
              type="file"
              ref={fileRef}
              accept="image/*"
              capture="environment"
              className="block"
              required
              multiple
            />
            {errors.file && <p className="text-red-500 text-xs">{errors.file}</p>}
          </div>
          {errors.submit && <p className="text-red-500 text-xs">{errors.submit}</p>}
          <button
            type="submit"
            className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700 transition"
            disabled={submitting}
          >
            {submitting ? 'Creating...' : 'Create'}
          </button>
        </form>
      </div>
    </div>
  );
}
