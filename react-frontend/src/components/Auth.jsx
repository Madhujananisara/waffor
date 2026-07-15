import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Auth = ({ onLoginSuccess, selectedRole, onBackToRoles }) => {
  const [mode, setMode] = useState('login'); // 'login' | 'register' | 'forgot'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [mobile, setMobile] = useState('');
  const [address, setAddress] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [role, setRole] = useState(selectedRole || 'CUSTOMER'); // 'CUSTOMER' | 'ADMIN' | 'DELIVERY'

  useEffect(() => {
    if (selectedRole) {
      setRole(selectedRole);
    }
  }, [selectedRole]);
  
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      if (mode === 'login') {
        const response = await axios.post('/api/auth/login', { email, password });
        localStorage.setItem('user', JSON.stringify(response.data));
        onLoginSuccess(response.data);
      } else if (mode === 'register') {
        const response = await axios.post('/api/auth/register', {
          email,
          password,
          name,
          mobile,
          address,
          role
        });
        setMessage({ type: 'success', text: 'Registration successful! You can now log in.' });
        setMode('login');
        setPassword('');
      } else {
        // forgot password
        await axios.post('/api/auth/forgot-password', {
          email,
          newPassword
        });
        setMessage({ type: 'success', text: 'Password reset successfully! Log in with your new password.' });
        setMode('login');
        setNewPassword('');
        setPassword('');
      }
    } catch (err) {
      console.error(err);
      const errMsg = err.response?.data?.message || err.message || 'Authentication error';
      setMessage({ type: 'error', text: errMsg });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container fade-in">
      <div className="card glass-card auth-card">
        {onBackToRoles && (
          <button onClick={onBackToRoles} className="btn-link" style={{ alignSelf: 'flex-start', display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.85rem', marginBottom: '1.5rem', padding: 0 }}>
            ← Back to Role Selection
          </button>
        )}
        <div className="auth-header">
          <span className="auth-logo">🍕</span>
          <h2 className="title-gradient">Waffor Food</h2>
          <p className="auth-subtitle">
            {mode === 'login' && 'Welcome back! Log in to order delicious food.'}
            {mode === 'register' && 'Create your account to start ordering.'}
            {mode === 'forgot' && 'Reset your password to regain access.'}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {mode === 'register' && (
            <>
              <div className="form-group">
                <label>Full Name</label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. John Doe"
                  required
                  className="input-glow"
                />
              </div>
              {(role === 'CUSTOMER' || role === 'DELIVERY') && (
                <div className="form-group">
                  <label>Mobile Number</label>
                  <input
                    type="tel"
                    value={mobile}
                    onChange={(e) => setMobile(e.target.value)}
                    placeholder="e.g. 9876543210"
                    required
                    className="input-glow"
                  />
                </div>
              )}
              {role === 'CUSTOMER' && (
                <div className="form-group">
                  <label>Delivery Address</label>
                  <input
                    type="text"
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    placeholder="e.g. Flat 101, Waffor Residency"
                    required
                    className="input-glow"
                  />
                </div>
              )}

            </>
          )}

          <div className="form-group">
            <label>Email Address</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. john@example.com"
              required
              className="input-glow"
            />
          </div>

          {mode !== 'forgot' && (
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                className="input-glow"
              />
            </div>
          )}

          {mode === 'forgot' && (
            <div className="form-group">
              <label>New Password</label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="••••••••"
                required
                className="input-glow"
              />
            </div>
          )}

          {message && (
            <div className={`alert-box ${message.type === 'error' ? 'alert-error' : 'alert-success'} fade-in`}>
              {message.text}
            </div>
          )}

          <button type="submit" disabled={loading} className="btn-primary btn-block">
            {loading ? 'Processing...' : mode === 'login' ? 'Log In' : mode === 'register' ? 'Register Account' : 'Reset Password'}
          </button>
        </form>

        <div className="auth-footer">
          {mode === 'login' && (
            <>
              <p>Don't have an account? <button onClick={() => { setMode('register'); setMessage(null); }} className="btn-link">Register</button></p>
              <p><button onClick={() => { setMode('forgot'); setMessage(null); }} className="btn-link">Forgot Password?</button></p>
            </>
          )}
          {mode === 'register' && (
            <p>Already have an account? <button onClick={() => { setMode('login'); setMessage(null); }} className="btn-link">Log In</button></p>
          )}
          {mode === 'forgot' && (
            <p>Remembered password? <button onClick={() => { setMode('login'); setMessage(null); }} className="btn-link">Back to Log In</button></p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Auth;
