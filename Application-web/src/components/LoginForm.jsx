import { useState } from 'react';
import { getBaseUrl } from '../API';
import { useAuth } from '../AuthContext';
import { useNavigate } from 'react-router-dom';

/** LoginForm */
function LoginForm() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  
  const signIn = async (e) => {
    e.preventDefault();
    setError('');

    if (!email.trim() || !password.trim()) {
      setError('Email and password are required.');
      return;
    }

    try {
      setSubmitting(true);
      const res = await fetch(`${getBaseUrl()}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          email,
          password,
        }),
      });

      if (!res.ok) {
        const message = await res.text();
        throw new Error(message || 'Login failed');
      }

      const meRes = await fetch(`${getBaseUrl()}/me`, {
        credentials: 'include',
      });

      if (!meRes.ok) {
        throw new Error('Failed to load user after login');
      }

      const userData = await meRes.json();
      login(userData);
    } catch (err) {
      console.error('Login failed:', err);
      setError(err.message ?? 'Login failed.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={signIn}>
      <h2>Log In</h2>

      <div>
        <label htmlFor="loginEmail">Email: </label>
        <input
          type="email"
          id="loginEmail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </div>

      <div>
        <label htmlFor="loginPassword">Password: </label>
        <input
          type="password"
          id="loginPassword"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
      </div>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'Logging in...' : 'Log In'}
      </button>
      <p style={{ marginTop: '12px', fontSize: '14px' }}>
        Don't have an account?{' '}
        <span
          onClick={() => navigate('/signup')}
          style={{ color: 'blue', cursor: 'pointer', textDecoration: 'underline' }}
        >
          Sign up
        </span>
      </p>
    </form>
  );
}

export default LoginForm;