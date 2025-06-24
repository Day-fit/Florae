import { useRef, useState } from 'react';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';
import { createPlantSchema } from '../util/form-validiation.js';

export default function useCreatePlant({ onClose }) {
  const nameRef = useRef('');
  const fileRef = useRef('');

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const setPlantName = async (plantId, name) => {
    const csrfToken = await getCsrfToken();
    await axios.put(
      '/api/v1/plant-set-name',
      { plantId, name },
      {
        headers: {
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      }
    );
  };

  // Shortened reusable setPlantVolume function
  const setPlantVolume = async (plantId, volume) => {
    const csrfToken = await getCsrfToken();
    await axios.put(
      '/api/v1/set-pot-volume',
      { plantId, volume: parseFloat(volume) },
      {
        headers: { 'X-XSRF-TOKEN': csrfToken },
        withCredentials: true,
      }
    );
  };

  const handleSubmit = async (e, volumeValue) => {
    e.preventDefault();

    const files = fileRef.current.files;
    const name = nameRef.current.value;

    setErrors({});

    // Validate using Yup schema
    try {
      await createPlantSchema.validate(
        {
          name,
          volume: volumeValue,
          files: fileRef.current,
        },
        { abortEarly: false }
      );
    } catch (validationError) {
      const newErrors = {};
      validationError.inner.forEach((error) => {
        newErrors[error.path] = error.message;
      });
      setErrors(newErrors);
      return;
    }

    setSubmitting(true);
    const formData = new FormData();
    Array.from(files).forEach((file) => {
      formData.append('photos', file);
    });

    try {
      const csrfToken = await getCsrfToken();
      const response = await axios.post('/api/v1/add-plant', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      });
      try {
        await setPlantName(response.data.id, name);
        await setPlantVolume(response.data.id, volumeValue);
        // eslint-disable-next-line no-unused-vars
      } catch (e) {
        console.error('Failed to set plant name or volume:');
      }

      onClose();
      // eslint-disable-next-line no-unused-vars
    } catch (err) {
      setErrors((prev) => ({
        ...prev,
        submit: 'Failed to create plant. Try again later. ',
      }));
    } finally {
      setSubmitting(false);
    }
  };
  return { nameRef, fileRef, errors, submitting, handleSubmit, setPlantName, setPlantVolume };
}
