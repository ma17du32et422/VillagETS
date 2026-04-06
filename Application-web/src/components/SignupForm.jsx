import { useState } from 'react';
import { getBaseUrl } from '../API';
import { useAuth } from '../AuthContext';

/** SignupForm */
function SignupForm() {
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const signUp = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      console.error('Passwords do not match');
      return;
    }

    try {

      await fetch(`${getBaseUrl()}/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          username,
          password,
        }),
      });

      const res = await fetch(`${getBaseUrl()}/me`, {
        credentials: 'include',
      });

      const userData = await res.json();

      login(userData);

    } catch (err) {
      console.error('Signup failed:', err);
    }
  };

  return (
    <form onSubmit={signUp}>
      <h2>Sign Up</h2>

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

      <button type="submit">Sign Up</button>
    </form>
  );
}

export default SignupForm;