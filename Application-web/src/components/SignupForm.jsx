import { useState, useEffect } from 'react';
import { getBaseUrl } from '../API';
import { useAuth } from '../AuthContext';

/** SignupForm */
function SignupForm() {
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [birthYear, setBirthYear] = useState('');
  const [profilePic, setProfilePic] = useState(null);
  const [profilePicPreview, setProfilePicPreview] = useState(null);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Handle image preview
  useEffect(() => {
    if (!profilePic) {
      setProfilePicPreview(null);
      return;
    }

    const objectUrl = URL.createObjectURL(profilePic);
    setProfilePicPreview(objectUrl);

    return () => URL.revokeObjectURL(objectUrl);
  }, [profilePic]);

  const handleProfilePicChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setProfilePic(file);
      //setProfilePicPreview(URL.createObjectURL(file));
    }
  };
  const uploadFile = async (file) => {
  const form = new FormData();
  form.append('file', file);
  form.append('nom', file.name);
  form.append('type', file.type);

  const res = await fetch(`${getBaseUrl()}/upload`, {
    method: 'POST',
    credentials: 'include',
    body: form,
  });

  if (!res.ok) {
    const err = await res.json();
    throw new Error(err.error || 'Upload failed');
  }

  return await res.json();
};
  const signUp = async (e) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !username.trim() || !password.trim() || !firstName.trim() || !lastName.trim()) {
      setError('Please fill all fields.');
      return;
    }

    if (username.trim().length < 3) {
      setError('Username must be at least 3 characters.');
      return;
    }

    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (!birthYear) {
      setError('Please enter your year of birth.');
      return;
    }
    try {
      setSubmitting(true);
      let photoUrl = null;
      if (profilePic) {
        const uploaded = await uploadFile(profilePic);
        photoUrl = uploaded.url;
      }
      const res = await fetch(`${getBaseUrl()}/auth/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          email,
          password,
          pseudo: username,
          nom: firstName,
          prenom: lastName,
          dateNaissance: birthYear,
          PhotoProfil: photoUrl,
        }),
      });

      if (!res.ok) {
        const message = await res.text();
        throw new Error(message || 'Signup failed');
      }

      const meRes = await fetch(`${getBaseUrl()}/me`, {
        credentials: 'include',
      });

      if (!meRes.ok) {
        throw new Error('Failed to verify session after signup');
      }

      const userData = await meRes.json();
      login(userData);
    } catch (err) {
      console.error('Signup failed:', err);
      setError(err.message ?? 'Signup failed.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={signUp}>
      <h2>Sign Up</h2>

      {/* Profile Picture Section */}
      <div style={{ marginBottom: '20px', textAlign: 'center' }}>
        {profilePicPreview && (
          <img 
            src={profilePicPreview} 
            alt="Preview" 
            style={{ width: '100px', height: '100px', borderRadius: '50%', objectFit: 'cover', marginBottom: '10px' }} 
          />
        )}
        <br />
        <button type="button" onClick={() => document.getElementById('profilePicInput').click()}>
          {profilePic ? 'Change Photo' : 'Upload Photo'}
        </button>
        <input
          type="file"
          id="profilePicInput"
          accept="image/*"
          style={{ display: 'none' }}
          onChange={handleProfilePicChange}
        />
      </div>

      <div>
        <label htmlFor="signEmail">Email: </label>
        <input
          type="email"
          id="signEmail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Email"
        />
      </div>

      <div>
        <label htmlFor="signUsername">Username: </label>
        <input
          type="text"
          id="signUsername"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Username"
        />
      </div>

      <div>
        <label htmlFor="signFirstName">First Name: </label>
        <input
          type="text"
          id="signFirstName"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          placeholder="First Name"
        />
      </div>

      <div>
        <label htmlFor="signLastName">Last Name: </label>
        <input
          type="text"
          id="signLastName"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          placeholder="Last Name"
        />
      </div>

      <div>
        <label>Date of Birth: </label>
        <select value={birthYear} onChange={e => { setBirthYear(e.target.value);}}>
          <option value="">Year</option>
          {Array.from({ length: 100 }, (_, i) => {
            const y = new Date().getFullYear() - i;
            return <option key={y} value={y}>{y}</option>;
          })}
        </select>
      </div>

      <div>
        <label htmlFor="signPassword">Password: </label>
        <input
          type="password"
          id="signPassword"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
        />
      </div>

      <div>
        <label htmlFor="confirmPassword">Confirm Password: </label>
        <input
          type="password"
          id="confirmPassword"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          placeholder="Confirm Password"
        />
      </div>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Signing up...' : 'Sign Up'}
      </button>
    </form>
  );
}

export default SignupForm;