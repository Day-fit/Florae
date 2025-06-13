/**
 * CreatePlant is a React component for adding a new plant.
 * It presents a form for users to input plant details and handles the submit logic.
 *
 * Props:
 * - onCreate (function): Callback for when plant creation succeeds.
 *
 * Usage:
 *
 * <CreatePlant onCreate={refreshPlantsList} />
 * ```
 */

import React, { useRef, useState } from 'react';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';

export default function CreatePlant({ onClose }) {
  const nameRef = useRef('');
  const fileRef = useRef('');

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    const files = fileRef.current.files;

    setErrors({});
    if (files.length < 1 || files.length > 5) {
      setErrors((prev) => ({
        ...prev,
        file: 'You must select between 1 and 5 photos.',
      }));
      return;
    }

    setSubmitting(true);
    const formData = new FormData();
    Array.from(files).forEach((file) => {
      formData.append('photos', file);
    });

    try {
      const csrfToken = await getCsrfToken();

      console.log('CSRF Token:', csrfToken);

      const response = await axios.post('/api/v1/add-plant', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      });

      const plantId = response.data?.id;

      let customName = '';
      if (plantId) {
        try {
          const csrfToken = await getCsrfToken();

          const config = {
            headers: {
              'X-XSRF-TOKEN': csrfToken,
            },
            withCredentials: true,
          };
          const nameRes = await axios.post(
            '/api/v1/plant-set-name',
            { plantId: plantId, name: nameRef.current.value },
            config
          );
          customName = nameRes.data?.customName || '';
          console.log(customName);
        } catch (e) {
          console.log(e);
          customName = '';
          console.log(customName);
        }
      }
      onClose();
    } catch (error) {
      setErrors((prev) => ({
        ...prev,
        submit: 'Failed to create plant. Try again later.',
      }));
      console.error(error);
    } finally {
      setSubmitting(false);
    }
  };

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
