import { useState } from 'react';
import { getBaseUrl } from '../API';
import { useAuth } from '../AuthContext';

/** SignupForm */
function SignupForm() {
  const { login } = useAuth();

  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

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

    try {
      setSubmitting(true);
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

      <div>
        <label htmlFor="signEmail">Email: </label>
        <input
          type="email"
          id="signEmail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="signUsername">Username: </label>
        <input
          type="text"
          id="signUsername"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="signFirstName">First Name: </label>
        <input
          type="text"
          id="signFirstName"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="signLastName">Last Name: </label>
        <input
          type="text"
          id="signLastName"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="signPassword">Password: </label>
        <input
          type="password"
          id="signPassword"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="confirmPassword">Confirm Password: </label>
        <input
          type="password"
          id="confirmPassword"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
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