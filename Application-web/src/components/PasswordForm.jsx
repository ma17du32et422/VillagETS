
import { useState } from 'react';
import { getBaseUrl } from '../API';

/** PasswordForm */
function PasswordForm() {
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');
        setSuccess('');

        if (!oldPassword.trim() || !newPassword.trim()) {
            setError('Current password and new password are required.');
            return;
        }

        if (newPassword.length < 8) {
            setError('New password must be at least 8 characters.');
            return;
        }

        try {
            setSubmitting(true);
            const response = await fetch(`${getBaseUrl()}/user/password`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                    currentPassword: oldPassword,
                    newPassword,
                }),
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'Failed to update password.');
            }

            setOldPassword('');
            setNewPassword('');
            setSuccess('Password updated successfully.');
        } catch (err) {
            console.error('Password update failed:', err);
            setError(err.message ?? 'Failed to update password.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <h3>Modify Password</h3>

            <div>
                <label htmlFor="oldPassword">Old Password: </label>
                <input
                    type="password"
                    id="oldPassword"
                    name="oldPassword"
                    value={oldPassword}
                    onChange={(event) => setOldPassword(event.target.value)}
                />
            </div>

            <div>
                <label htmlFor="newPassword">New Password: </label>
                <input
                    type="password"
                    id="newPassword"
                    name="newPassword"
                    value={newPassword}
                    onChange={(event) => setNewPassword(event.target.value)}
                />
            </div>

            {error && <p style={{ color: 'red' }}>{error}</p>}
            {success && <p style={{ color: 'green' }}>{success}</p>}

            <button type="submit" disabled={submitting}>
                {submitting ? 'Applying...' : 'Apply New Password'}
            </button>
        </form>
    );
}

export default PasswordForm;
