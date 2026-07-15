import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Dashboard = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);

  const fetchOrders = async (isInitial = false) => {
    if (isInitial) setLoading(true);
    try {
      const response = await axios.get('/api/orders');
      // Sort orders descending by id/created date so newest are on top
      const sortedOrders = (response.data || []).sort((a, b) => b.id - a.id);
      setOrders(sortedOrders);
      setLastUpdated(new Date().toLocaleTimeString());
      setError(null);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Could not connect to backend service. Retrying...');
    } finally {
      if (isInitial) setLoading(false);
    }
  };

  useEffect(() => {
    // Initial fetch
    fetchOrders(true);

    // Setup polling every 2 seconds
    const intervalId = setInterval(() => {
      fetchOrders(false);
    }, 2000);

    // Cleanup interval on unmount
    return () => clearInterval(intervalId);
  }, []);

  const getStatusClass = (status) => {
    switch (status?.toUpperCase()) {
      case 'PLACED': return 'status-placed';
      case 'PAID': return 'status-paid';
      case 'READY': return 'status-ready';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      case 'PAYMENT_FAILED': return 'status-failed';
      case 'KITCHEN_FAILED': return 'status-failed';
      case 'DELIVERY_FAILED': return 'status-failed';
      default: return 'status-default';
    }
  };

  return (
    <div className="card glass-card fade-in">
      <div className="dashboard-header">
        <h2 className="title-gradient">Order Dashboard</h2>
        <div className="poll-indicator">
          <span className="dot pulse-green"></span>
          <span className="poll-text">Polling every 2s</span>
          {lastUpdated && <span className="last-updated">| Updated: {lastUpdated}</span>}
        </div>
      </div>

      {loading ? (
        <div className="loader-container">
          <div className="loader"></div>
          <p>Loading orders...</p>
        </div>
      ) : error ? (
        <div className="alert-box alert-error fade-in">
          {error}
          <button onClick={() => fetchOrders(true)} className="btn-retry">Retry Now</button>
        </div>
      ) : orders.length === 0 ? (
        <div className="empty-state">
          <p>No orders found. Use the form to place your first order!</p>
        </div>
      ) : (
        <div className="orders-table-container">
          <table className="orders-table">
            <thead>
              <tr>
                <th>Order Number</th>
                <th>Customer ID</th>
                <th>Total Amount</th>
                <th>Status</th>
                <th>Order Items</th>
                <th>Placed At</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id} className="order-row glass-row fade-in">
                  <td className="order-num-col">
                    <strong>{order.orderNumber}</strong>
                    <span className="order-db-id">ID: {order.id}</span>
                  </td>
                  <td>Customer #{order.customerId}</td>
                  <td className="amount-col">${order.totalAmount?.toFixed(2)}</td>
                  <td>
                    <span className={`status-badge ${getStatusClass(order.status)}`}>
                      {order.status}
                    </span>
                  </td>
                  <td className="items-col">
                    <ul className="items-list">
                      {order.items && order.items.map((item, idx) => (
                        <li key={item.id || idx}>
                          Prod #{item.productId} &times; {item.quantity} (${item.price?.toFixed(2)})
                        </li>
                      ))}
                    </ul>
                  </td>
                  <td className="time-col">
                    {order.createdAt ? new Date(order.createdAt).toLocaleString() : 'N/A'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
