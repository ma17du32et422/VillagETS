
import { useEffect, useState } from 'react';
import { getBaseUrl } from '../API';

/** Usernameform */
function UsernameForm({ currentUsername = '', onSuccess }) {
    const [newUsername, setNewUsername] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        return () => {
            setNewUsername('');
            setError('');
            setSuccess('');
        };
    }, []);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');
        setSuccess('');

        const pseudo = newUsername.trim();
        if (!pseudo) {
            setError('Username is required.');
            return;
        }

        if (pseudo.length < 3) {
            setError('Username must be at least 3 characters.');
            return;
        }

        if (pseudo === currentUsername) {
            setSuccess('Username is already up to date.');
            return;
        }

        try {
            setSubmitting(true);
            const response = await fetch(`${getBaseUrl()}/user/pseudo`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({ pseudo }),
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'Failed to update username.');
            }

            setSuccess('Username updated successfully.');
            setNewUsername('');
            if (onSuccess) {
                try {
                    await onSuccess();
                } catch (refreshError) {
                    console.error('Profile refresh failed:', refreshError);
                }
            }
        } catch (err) {
            console.error('Username update failed:', err);
            setError(err.message ?? 'Failed to update username.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>Modify Username</h3>

            <div>
                <label htmlFor="newUsername">New Username: </label>
                <input
                    type="text"
                    id="newUsername"
                    name="newUsername"
                    value={newUsername}
                    onChange={(event) => setNewUsername(event.target.value)}
                    autoComplete="off"
                />
            </div>

            {error && <p style={{ color: 'red' }}>{error}</p>}
            {success && <p style={{ color: 'green' }}>{success}</p>}

            <button type="button" onClick={handleSubmit} disabled={submitting}>
                {submitting ? 'Applying...' : 'Apply New Username'}
            </button>
        </form>
    );
}

export default UsernameForm;
