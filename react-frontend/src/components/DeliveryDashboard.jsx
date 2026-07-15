import React, { useState, useEffect } from 'react';
import axios from 'axios';

const DeliveryDashboard = ({ user, handleLogout }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  const fetchOrders = async (isInitial = false) => {
    if (isInitial) setLoading(true);
    try {
      const response = await axios.get('/api/orders');
      // Sort orders descending by ID (newest on top)
      const sorted = (response.data || []).sort((a, b) => b.id - a.id);
      setOrders(sorted);
      setLastUpdated(new Date().toLocaleTimeString());
      setError(null);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Could not connect to backend service.');
    } finally {
      if (isInitial) setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders(true);
    const intervalId = setInterval(() => {
      fetchOrders(false);
    }, 3000);

    return () => clearInterval(intervalId);
  }, []);

  const handleStatusChange = async (orderId, newStatus) => {
    setUpdatingId(orderId);
    try {
      await axios.put(`/api/orders/${orderId}/status`, { status: newStatus });
      // Update local state directly for responsive feedback
      setOrders(prevOrders => prevOrders.map((o) => o.id === orderId ? { ...o, status: newStatus } : o));
    } catch (err) {
      console.error('Error updating order status:', err);
      alert('Failed to update order status. Please verify the backend logs.');
    } finally {
      setUpdatingId(null);
    }
  };

  // Available orders for pick up: status 'PAID' or 'PREPARING'
  const availableOrders = orders.filter(o => o.status === 'PAID' || o.status === 'PREPARING');

  // Active deliveries: status 'OUT_FOR_DELIVERY'
  const activeDeliveries = orders.filter(o => o.status === 'OUT_FOR_DELIVERY');

  // Completed/Failed deliveries: status 'DELIVERED', 'DELIVERED_UNPAID', or 'DELIVERY_FAILED'
  const completedDeliveries = orders.filter(o => o.status === 'DELIVERED' || o.status === 'DELIVERED_UNPAID' || o.status === 'DELIVERY_FAILED');

  const getStatusClass = (status) => {
    switch (status?.toUpperCase()) {
      case 'PAID': return 'status-paid';
      case 'PREPARING': return 'status-ready';
      case 'OUT_FOR_DELIVERY': return 'status-placed';
      case 'DELIVERED': return 'status-delivered';
      case 'DELIVERED_UNPAID': return 'status-failed';
      case 'DELIVERY_FAILED': return 'status-failed';
      default: return 'status-default';
    }
  };



  if (loading) {
    return (
      <div className="loader-container">
        <div className="loader"></div>
        <p>Loading Delivery Panel...</p>
      </div>
    );
  }

  return (
    <div className="delivery-container fade-in">
      {/* Top Banner */}
      <div className="dashboard-header card glass-card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h2 className="title-gradient">🛵 Delivery Partner Panel</h2>
            <p style={{ color: 'var(--text-secondary)' }}>Welcome, {user?.name || 'Partner'}. Manage and fulfill active deliveries.</p>
          </div>
          <button onClick={handleLogout} className="logout-btn" style={{ padding: '0.6rem 1.2rem', fontSize: '0.95rem' }}>
            Logout Panel
          </button>
        </div>
        <div className="poll-indicator" style={{ marginTop: '1rem' }}>
          <span className="dot pulse-green"></span>
          <span className="poll-text">Auto-refreshing every 3s</span>
          {lastUpdated && <span className="last-updated"> | Updated: {lastUpdated}</span>}
        </div>
      </div>

      {error && <div className="alert-box alert-error fade-in">{error}</div>}

      <div className="delivery-grid">
        {/* Active Deliveries Section */}
        <div className="delivery-section">
          <h3 className="delivery-section-title">
            <span>📦</span> Active Deliveries ({activeDeliveries.length})
          </h3>
          {activeDeliveries.length === 0 ? (
            <div className="empty-state card glass-card" style={{ padding: '2rem' }}>
              <p>You have no active deliveries. Claim an order below!</p>
            </div>
          ) : (
            <div className="delivery-cards-list">
              {activeDeliveries.map((order) => (
                <div key={order.id} className="card glass-card delivery-card fade-in">
                  <div className="delivery-card-header">
                    <span className="delivery-order-num">{order.orderNumber}</span>
                    <span className={`status-badge ${getStatusClass(order.status)}`}>
                      {order.status}
                    </span>
                  </div>
                  <div className="delivery-card-body">
                    {order.paymentMethod === 'COD' && (
                      <div className="alert-box alert-error fade-in" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.3rem', margin: '0.2rem 0 0.6rem 0' }}>
                        <span>💵</span> CASH ON DELIVERY: Collect ₹{order.totalAmount?.toFixed(0)}
                      </div>
                    )}
                    <div><strong>Customer:</strong> {order.customerName}</div>
                    <div><strong>Contact:</strong> 📞 {order.mobileNumber}</div>
                    <div className="delivery-address-info">
                      <strong>📍 Address:</strong>
                      <div style={{ marginTop: '0.2rem', fontSize: '0.85rem' }}>{order.deliveryAddress}</div>
                    </div>
                    <div className="delivery-item-summary">
                      <strong>Items:</strong> {order.items?.map(i => `Prod #${i.productId} (x${i.quantity})`).join(', ')}
                    </div>
                  </div>
                  <div className="delivery-card-actions" style={{ flexDirection: 'column', gap: '0.6rem' }}>
                    {order.paymentMethod === 'COD' ? (
                      <>
                        <button
                          className="btn-primary btn-block"
                          style={{ background: 'var(--accent-green)', boxShadow: 'none' }}
                          disabled={updatingId === order.id}
                          onClick={() => handleStatusChange(order.id, 'DELIVERED')}
                        >
                          💵 Cash Received
                        </button>
                        <button
                          className="btn-primary btn-block"
                          style={{ background: 'var(--accent-yellow)', color: '#000', boxShadow: 'none' }}
                          disabled={updatingId === order.id}
                          onClick={() => handleStatusChange(order.id, 'DELIVERED_UNPAID')}
                        >
                          ⚠️ Delivered (No Cash/Unpaid)
                        </button>
                      </>
                    ) : (
                      <button
                        className="btn-primary btn-block"
                        style={{ background: 'var(--accent-green)', boxShadow: 'none' }}
                        disabled={updatingId === order.id}
                        onClick={() => handleStatusChange(order.id, 'DELIVERED')}
                      >
                        ✓ Mark Delivered
                      </button>
                    )}
                    <button
                      className="btn-primary btn-block"
                      style={{ background: 'var(--accent-red)', boxShadow: 'none' }}
                      disabled={updatingId === order.id}
                      onClick={() => handleStatusChange(order.id, 'DELIVERY_FAILED')}
                    >
                      ✗ Mark Failed
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Available Orders Section */}
        <div className="delivery-section" style={{ marginTop: '1rem' }}>
          <h3 className="delivery-section-title">
            <span>🛒</span> Available for Pick Up ({availableOrders.length})
          </h3>
          {availableOrders.length === 0 ? (
            <div className="empty-state card glass-card" style={{ padding: '2rem' }}>
              <p>No new orders ready for pickup at the moment.</p>
            </div>
          ) : (
            <div className="delivery-cards-list">
              {availableOrders.map((order) => (
                <div key={order.id} className="card glass-card delivery-card fade-in">
                  <div className="delivery-card-header">
                    <span className="delivery-order-num">{order.orderNumber}</span>
                    <span className={`status-badge ${getStatusClass(order.status)}`}>
                      {order.status}
                    </span>
                  </div>
                  <div className="delivery-card-body">
                    {order.paymentMethod === 'COD' && (
                      <div className="alert-box alert-error fade-in" style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.3rem', margin: '0.2rem 0 0.6rem 0' }}>
                        <span>💵</span> CASH ON DELIVERY: Collect ₹{order.totalAmount?.toFixed(0)}
                      </div>
                    )}
                    <div><strong>Customer:</strong> {order.customerName}</div>
                    <div className="delivery-address-info">
                      <strong>📍 Destination:</strong>
                      <div style={{ marginTop: '0.2rem', fontSize: '0.85rem' }}>{order.deliveryAddress}</div>
                    </div>
                    <div><strong>Bill Amount:</strong> ₹{order.totalAmount?.toFixed(0)}</div>
                  </div>
                  <div className="delivery-card-actions">
                    <button
                      className="btn-primary btn-block"
                      disabled={updatingId === order.id}
                      onClick={() => handleStatusChange(order.id, 'OUT_FOR_DELIVERY')}
                    >
                      🛵 Accept & Start Delivery
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* History Section */}
        <div className="delivery-section" style={{ marginTop: '1rem' }}>
          <h3 className="delivery-section-title">
            <span>📜</span> Delivery History ({completedDeliveries.length})
          </h3>
          {completedDeliveries.length === 0 ? (
            <div className="empty-state card glass-card" style={{ padding: '2rem' }}>
              <p>No delivery history recorded yet.</p>
            </div>
          ) : (
            <div className="card glass-card" style={{ padding: '1rem 1.5rem' }}>
              <div className="orders-table-container">
                <table className="orders-table">
                  <thead>
                    <tr>
                      <th>Order Number</th>
                      <th>Customer Details</th>
                      <th>Delivery Destination</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {completedDeliveries.map((order) => (
                      <tr key={order.id} className="order-row glass-row fade-in">
                        <td><strong>{order.orderNumber}</strong></td>
                        <td>
                          <div>{order.customerName}</div>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>📞 {order.mobileNumber}</div>
                        </td>
                        <td>{order.deliveryAddress}</td>
                        <td>
                          <span className={`status-badge ${getStatusClass(order.status)}`}>
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DeliveryDashboard;
