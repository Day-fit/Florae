import { useRef, useState } from 'react';
import axios from 'axios';
import getCsrfToken from '../util/getCsrfToken.js';

export default function useCreatePlant({ onClose }) {
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
                    const nameRes = await axios.post(
                        '/api/v1/plant-set-name',
                        { plantId: plantId, name: nameRef.current.value },
                        {
                            headers: {
                                'X-XSRF-TOKEN': csrfToken,
                            },
                            withCredentials: true,
                        }
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
    return({nameRef, fileRef, errors, submitting, handleSubmit})
}