/** Component imports */
import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import UsernameForm from '../components/UsernameForm';
import PasswordForm from '../components/PasswordForm';
import { getBaseUrl } from '../API';

/** Styling */
import '../assets/ProfilePage.css'

/** ProfilePage */
function ProfilePage() {
    const { user, logout, loading } = useAuth();
    const [profileUser, setProfileUser] = useState(null);
    const [profilePicUrl, setProfilePicUrl] = useState('');
    const [uploadMessage, setUploadMessage] = useState('');
    const [uploading, setUploading] = useState(false);
    const inputRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (!user || loading) return;

        const parseUsersResponse = async (response) => {
            const text = await response.text();
            try {
                return JSON.parse(text);
            } catch {
                const rawIndex = text.indexOf('Raw:');
                if (rawIndex !== -1) {
                    const rawJson = text.slice(rawIndex + 4).trim();
                    return JSON.parse(rawJson);
                }
                return null;
            }
        };

        const fetchProfile = async () => {
            try {
                const res = await fetch(`${getBaseUrl()}/Utilisateur`);
                if (!res.ok) {
                    throw new Error('Unable to load profile');
                }

                const allUsers = await parseUsersResponse(res);
                if (!Array.isArray(allUsers)) {
                    throw new Error('Unable to parse profile list');
                }

                const currentUser = allUsers.find((u) => u.id_utilisateur === user.userId || u.Id === user.userId || u.id === user.userId);
                if (currentUser) {
                    setProfileUser(currentUser);
                    setProfilePicUrl(currentUser.photo_profil || currentUser.PhotoProfil || '');
                } else {
                    setProfileUser(null);
                }
            } catch (err) {
                console.error('Profile load failed:', err);
            }
        };

        fetchProfile();
    }, [user, loading]);

    const handleLogout = async () => {
        await logout();
        navigate('/LoginPage');
    };

    const handleFileChange = async (event) => {
        const file = event.target.files?.[0];
        if (!file) return;
        setUploadMessage('');
        setUploading(true);

        try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('nom', file.name);
            formData.append('type', 'pfp');
 
            const response = await fetch(`${getBaseUrl()}/upload`, {
                method: 'POST',
                credentials: 'include',
                body: formData,
            });
 
            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'Upload failed');
            }
 
            const data = await response.json();
            const uploadedUrl = data.url || '';
 
            const patchResponse = await fetch(`${getBaseUrl()}/user/photo`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ photoUrl: uploadedUrl }),
            });
 
            if (!patchResponse.ok) {
                const text = await patchResponse.text();
                throw new Error(text || 'Failed to update profile picture');
            }
 
            setProfilePicUrl(uploadedUrl);
            setUploadMessage('Profile picture uploaded successfully.');
        } catch (err) {
            console.error('Upload failed:', err);
            setUploadMessage(err.message || 'Upload failed.');
        } finally {
            setUploading(false);
        }
    };

    if (loading) {
        return <main id="profileMain"><p>Loading profile...</p></main>;
    }

    if (!user) {
        return (
            <main id="profileMain">
                <section id="profile-edit-container">
                    <div id="profile-edit">
                        <h2>Please log in to access your profile</h2>
                        <button type="button" onClick={() => navigate('/LoginPage')}>Go to Login</button>
                    </div>
                </section>
            </main>
        );
    }

    return (
            <main id="profileMain">
                <section id="profile-edit-container">
                    <div id="profile-edit">
                        <h2>Settings</h2>
                        <p><strong>User ID:</strong> {user.userId}</p>
                        {profileUser && profileUser.pseudo && <p><strong>Username:</strong> {profileUser.pseudo}</p>}

                        <UsernameForm />
                        <PasswordForm />

                        <div id="profile-picture">
                            <h3>Profile Picture</h3>
                            <img
                                alt="Profile"
                                src={profilePicUrl || 'https://via.placeholder.com/140?text=No+image'}
                                height="140"
                            />
                            <input
                                ref={inputRef}
                                type="file"
                                id="profile-picture-input"
                                accept="image/*"
                                style={{ display: 'none' }}
                                onChange={handleFileChange}
                            />
                            <button type="button" onClick={() => inputRef.current?.click()} disabled={uploading}>
                                {uploading ? 'Uploading…' : 'Change Profile Picture'}
                            </button>
                            {uploadMessage && <p id="profile-upload-message">{uploadMessage}</p>}
                        </div>

                        <button type="button" onClick={handleLogout}>Log Out</button>
                    </div>
                </section>
            </main>
    );
}

export default ProfilePage;
