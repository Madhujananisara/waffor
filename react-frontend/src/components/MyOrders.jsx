import React, { useState, useEffect } from 'react';
import axios from 'axios';

const MyOrders = ({ onTrackOrder }) => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchOrders = async () => {
    try {
      const userStr = localStorage.getItem('user');
      const user = userStr ? JSON.parse(userStr) : null;
      
      const response = await axios.get('/api/orders');
      let userOrders = response.data || [];
      
      // Filter orders by customer ID if logged in
      if (user) {
        userOrders = userOrders.filter((order) => order.customerId === user.id);
      }
      
      // Sort: newest first
      userOrders.sort((a, b) => b.id - a.id);
      setOrders(userOrders);
      setError(null);
    } catch (err) {
      console.error('Error fetching user orders:', err);
      setError('Could not connect to backend server.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 4000);
    return () => clearInterval(interval);
  }, []);

  const isActive = (status) => {
    const s = status?.toUpperCase();
    return s !== 'DELIVERED' && 
           s !== 'DELIVERED_UNPAID' && 
           s !== 'CANCELLED' && 
           s !== 'PAYMENT_FAILED' && 
           s !== 'KITCHEN_FAILED' && 
           s !== 'DELIVERY_FAILED';
  };

  const activeOrders = orders.filter((o) => isActive(o.status));
  const pastOrders = orders.filter((o) => !isActive(o.status));

  if (loading) {
    return (
      <div className="loader-container">
        <div className="loader"></div>
        <p>Fetching your order log...</p>
      </div>
    );
  }

  return (
    <div className="myorders-container fade-in">
      <h2 className="title-gradient">My Order History</h2>

      {error && <div className="alert-box alert-error">{error}</div>}

      {/* Active orders section */}
      <div className="orders-section">
        <h3>🔥 Active Orders ({activeOrders.length})</h3>
        {activeOrders.length === 0 ? (
          <p className="no-orders-msg">No active orders right now. Craving something tasty?</p>
        ) : (
          <div className="orders-grid">
            {activeOrders.map((order) => (
              <div key={order.id} className="card glass-card order-history-card active-border fade-in">
                <div className="oh-header">
                  <h4>Order {order.orderNumber}</h4>
                  <span className={`status-badge ${order.status?.toLowerCase()}`}>{order.status}</span>
                </div>
                <div className="oh-items">
                  <ul>
                    {order.items?.map((item, idx) => (
                      <li key={item.id || idx}>
                        Product #{item.productId} &times; {item.quantity} (₹{item.price?.toFixed(0)})
                      </li>
                    ))}
                  </ul>
                </div>
                <div className="oh-footer" style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                    <span className="oh-amount">Total: ₹{order.totalAmount?.toFixed(0)}</span>
                    <button onClick={() => onTrackOrder(order.id)} className="btn-primary btn-sm">
                      Track Live Progress
                    </button>
                  </div>
                  {order.status === 'OUT_FOR_DELIVERY' && (
                    <button 
                      onClick={async (e) => {
                        e.stopPropagation();
                        try {
                          await axios.put(`/api/orders/${order.id}/status`, { status: 'DELIVERED' });
                          fetchOrders();
                        } catch (err) {
                          console.error("Error marking order as received:", err);
                          alert("Failed to mark order as received. Please try again.");
                        }
                      }} 
                      className="btn-secondary btn-sm"
                      style={{ background: '#2e7d32', color: '#fff', border: 'none', width: '100%', marginTop: '0.25rem' }}
                    >
                      ✅ Mark as Received
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Past orders section */}
      <div className="orders-section past-section">
        <h3>📦 Previous Orders ({pastOrders.length})</h3>
        {pastOrders.length === 0 ? (
          <p className="no-orders-msg">No past orders in your logs.</p>
        ) : (
          <div className="orders-grid">
            {pastOrders.map((order) => (
              <div key={order.id} className="card glass-card order-history-card fade-in">
                <div className="oh-header">
                  <h4>Order {order.orderNumber}</h4>
                  <span className={`status-badge finished ${order.status?.toLowerCase()}`}>{order.status}</span>
                </div>
                <div className="oh-items">
                  <ul>
                    {order.items?.map((item, idx) => (
                      <li key={item.id || idx}>
                        Product #{item.productId} &times; {item.quantity} (₹{item.price?.toFixed(0)})
                      </li>
                    ))}
                  </ul>
                </div>
                <div className="oh-footer">
                  <span className="oh-amount">Total: ₹{order.totalAmount?.toFixed(0)}</span>
                  <span className="oh-date">{new Date(order.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyOrders;
