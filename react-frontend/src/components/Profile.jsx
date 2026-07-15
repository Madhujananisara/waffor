import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Profile = ({ user, onProfileUpdate }) => {
  const [name, setName] = useState('');
  const [mobile, setMobile] = useState('');
  const [address, setAddress] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  // Initialize fields with current user data
  useEffect(() => {
    if (user) {
      setName(user.name || '');
      setMobile(user.mobile || '');
      setAddress(user.address || '');
    }
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      const response = await axios.put(`/api/auth/profile/${user.id}`, {
        name,
        mobile,
        address,
        email: user.email // keep same
      });

      // Update local storage and parent state
      const updatedUser = { ...user, ...response.data };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      onProfileUpdate(updatedUser);

      setMessage({ type: 'success', text: 'Profile updated successfully!' });
    } catch (err) {
      console.error('Error updating profile:', err);
      const errMsg = err.response?.data?.message || err.message || 'Could not update profile';
      setMessage({ type: 'error', text: errMsg });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container fade-in" style={{ minHeight: 'calc(100vh - 200px)' }}>
      <div className="card glass-card auth-card">
        <div className="auth-header">
          <span className="auth-logo">👤</span>
          <h2 className="title-gradient">Manage Profile</h2>
          <p className="auth-subtitle">Update your personal details for faster checkout</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
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

          <div className="form-group">
            <label>Delivery Address</label>
            <textarea
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              placeholder="e.g. Flat 101, Waffor Residency"
              required
              className="input-glow"
              rows="3"
              style={{ resize: 'vertical', fontFamily: 'inherit' }}
            />
          </div>

          <div className="form-group">
            <label>Email Address (ReadOnly)</label>
            <input
              type="email"
              value={user?.email || ''}
              disabled
              className="input-glow"
              style={{ opacity: 0.6, cursor: 'not-allowed' }}
            />
          </div>

          {message && (
            <div className={`alert-box ${message.type === 'error' ? 'alert-error' : 'alert-success'} fade-in`}>
              {message.text}
            </div>
          )}

          <button type="submit" disabled={loading} className="btn-primary btn-block">
            {loading ? 'Saving Details...' : 'Save Profile Details'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Profile;
