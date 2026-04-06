import { useState } from 'react';
import { getBaseUrl } from '../API';
import { useAuth } from '../AuthContext';

function TestSignup() {
  const { login } = useAuth();

  const [form, setForm] = useState({
    email: '',
    password: '',
    username: '',
    firstName: '',
    lastName: '',
    yearOfBirth: '',
  });

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const sendSignup = async () => {
    try {
      // 1. Send signup request
      await fetch(`${getBaseUrl()}/auth/signup`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          email: form.email,
          password: form.password,
          user: form.username,
          firstname: form.firstName,
          lastname: form.lastName,
          yearOfBirth: form.yearOfBirth,
        }),
      });

      // 2. Fetch logged-in user (cookie should now exist)
      const res = await fetch(`${getBaseUrl()}/me`, {
        credentials: 'include',
      });

      const userData = await res.json();

      // 3. Save user in AuthContext
      login(userData);

      console.log('Signed up + logged in:', userData);

    } catch (err) {
      console.error('Signup failed:', err);
    }
  };

  return (
    <div>
      <h2>Test Signup</h2>

      <input name="email" placeholder="Email" onChange={handleChange} />
      <input name="password" type="password" placeholder="Password" onChange={handleChange} />
      <input name="username" placeholder="Username" onChange={handleChange} />
      <input name="firstName" placeholder="First Name" onChange={handleChange} />
      <input name="lastName" placeholder="Last Name" onChange={handleChange} />
      <input name="yearOfBirth" placeholder="Year of Birth" onChange={handleChange} />

      <br /><br />

      <button onClick={sendSignup}>
        Send Signup Request
      </button>
    </div>
  );
}

export default TestSignup;