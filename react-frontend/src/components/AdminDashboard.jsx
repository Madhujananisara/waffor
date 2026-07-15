import React, { useState, useEffect } from 'react';
import axios from 'axios';

const AdminDashboard = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  // Tabs: 'orders' | 'menu' | 'offers'
  const [activeTab, setActiveTab] = useState('orders');

  // Menu Management State
  const [menuItems, setMenuItems] = useState([]);
  const [newFood, setNewFood] = useState({ name: '', description: '', price: '', imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80', isVeg: true, rating: 4.5 });
  const [editingItemId, setEditingItemId] = useState(null);
  const [editPriceVal, setEditPriceVal] = useState('');

  // Offers State
  const [offers, setOffers] = useState([]);
  const [newOfferText, setNewOfferText] = useState('');

  const fetchOrders = async (isInitial = false) => {
    if (isInitial) setLoading(true);
    try {
      const response = await axios.get('/api/orders');
      // Sort orders descending by ID (newest on top)
      const sorted = (response.data || []).sort((a, b) => b.id - a.id);
      
      setOrders(prevOrders => {
        if (prevOrders.length > 0) {
          sorted.forEach(newOrder => {
            if (newOrder.status?.toUpperCase() === 'DELIVERED') {
              const oldOrder = prevOrders.find(o => o.id === newOrder.id);
              if (oldOrder && oldOrder.status?.toUpperCase() !== 'DELIVERED') {
                alert(`🔔 Order #${newOrder.orderNumber} has been successfully delivered to the customer!`);
              }
            }
          });
        }
        return sorted;
      });

      setLastUpdated(new Date().toLocaleTimeString());
      setError(null);
    } catch (err) {
      console.error('Error fetching orders:', err);
      setError('Could not connect to backend service.');
    } finally {
      if (isInitial) setLoading(false);
    }
  };

  const fetchMenu = async () => {
    try {
      const response = await axios.get('/api/food');
      setMenuItems(response.data || []);
    } catch (err) {
      console.error('Error fetching menu items:', err);
    }
  };

  const fetchOffers = async () => {
    try {
      const response = await axios.get('/api/offers');
      setOffers(response.data || []);
    } catch (err) {
      console.error('Error fetching offers:', err);
    }
  };

  useEffect(() => {
    fetchOrders(true);
    fetchMenu();
    fetchOffers();

    const intervalId = setInterval(() => {
      fetchOrders(false);
    }, 3000);

    return () => clearInterval(intervalId);
  }, []);

  const handleStatusChange = async (orderId, newStatus) => {
    setUpdatingId(orderId);
    try {
      await axios.put(`/api/orders/${orderId}/status`, { status: newStatus });
      setOrders(orders.map((o) => o.id === orderId ? { ...o, status: newStatus } : o));
    } catch (err) {
      console.error('Error updating order status:', err);
      alert('Failed to update order status. Please verify the backend logs.');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleAddFood = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        name: newFood.name,
        description: newFood.description,
        price: parseFloat(newFood.price),
        imageUrl: newFood.imageUrl,
        veg: newFood.isVeg,
        rating: parseFloat(newFood.rating)
      };
      await axios.post('/api/food', payload);
      alert('Food item added successfully!');
      setNewFood({ name: '', description: '', price: '', imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80', isVeg: true, rating: 4.5 });
      fetchMenu();
    } catch (err) {
      console.error('Error adding food item:', err);
      alert('Failed to add food item.');
    }
  };

  const handleUpdatePrice = async (id) => {
    if (!editPriceVal || isNaN(editPriceVal)) {
      alert('Please enter a valid price.');
      return;
    }
    try {
      await axios.put(`/api/food/${id}/price`, { price: parseFloat(editPriceVal) });
      alert('Price updated successfully!');
      setEditingItemId(null);
      setEditPriceVal('');
      fetchMenu();
    } catch (err) {
      console.error('Error updating food price:', err);
      alert('Failed to update price.');
    }
  };

  const handleAddOffer = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/offers', { text: newOfferText });
      alert('Offer announced successfully!');
      setNewOfferText('');
      fetchOffers();
    } catch (err) {
      console.error('Error adding offer:', err);
      alert('Failed to announce offer.');
    }
  };

  const handleDeleteOffer = async (id) => {
    try {
      await axios.delete(`/api/offers/${id}`);
      alert('Offer deleted successfully!');
      fetchOffers();
    } catch (err) {
      console.error('Error deleting offer:', err);
      alert('Failed to delete offer.');
    }
  };

  const getStatusClass = (status) => {
    switch (status?.toUpperCase()) {
      case 'PLACED': return 'status-placed';
      case 'PAID': return 'status-paid';
      case 'PREPARING': return 'status-ready';
      case 'OUT_FOR_DELIVERY': return 'status-default';
      case 'DELIVERED': return 'status-delivered';
      case 'DELIVERED_UNPAID': return 'status-failed';
      case 'PAYMENT_FAILED':
      case 'KITCHEN_FAILED':
      case 'DELIVERY_FAILED':
        return 'status-failed';
      default: return 'status-default';
    }
  };

  if (loading) {
    return (
      <div className="loader-container">
        <div className="loader"></div>
        <p>Loading Admin Dashboard...</p>
      </div>
    );
  }

  return (
    <div className="admin-container fade-in">
      <div className="dashboard-header card glass-card" style={{ marginBottom: '2.5rem' }}>
        <h2 className="title-gradient">👑 Admin Control Panel</h2>
        <p style={{ color: 'var(--text-secondary)' }}>Manage food menu, update prices, and announce real-time offers.</p>
        <div className="poll-indicator" style={{ marginTop: '0.5rem' }}>
          <span className="dot pulse-green"></span>
          <span className="poll-text">Auto-refreshing orders every 3s</span>
          {lastUpdated && <span className="last-updated"> | Last Check: {lastUpdated}</span>}
        </div>
      </div>

      {error && <div className="alert-box alert-error fade-in">{error}</div>}

      {/* Tabs Menu */}
      <div className="admin-tabs" style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
        <button 
          onClick={() => setActiveTab('orders')}
          className={`btn-primary ${activeTab === 'orders' ? 'active' : ''}`}
          style={{ background: activeTab === 'orders' ? 'var(--accent-primary)' : '#1e1e24', boxShadow: 'none' }}
        >
          📋 Orders List
        </button>
        <button 
          onClick={() => setActiveTab('menu')}
          className={`btn-primary ${activeTab === 'menu' ? 'active' : ''}`}
          style={{ background: activeTab === 'menu' ? 'var(--accent-primary)' : '#1e1e24', boxShadow: 'none' }}
        >
          🍳 Manage Menu
        </button>
        <button 
          onClick={() => setActiveTab('offers')}
          className={`btn-primary ${activeTab === 'offers' ? 'active' : ''}`}
          style={{ background: activeTab === 'offers' ? 'var(--accent-primary)' : '#1e1e24', boxShadow: 'none' }}
        >
          📢 Announce Offers
        </button>
      </div>

      {/* Active Tab Content */}

      {/* 1. ORDERS LIST */}
      {activeTab === 'orders' && (
        orders.length === 0 ? (
          <div className="empty-state card glass-card">
            <p>No orders currently registered in the database.</p>
          </div>
        ) : (
          <div className="card glass-card">
            <div className="orders-table-container">
              <table className="orders-table">
                <thead>
                  <tr>
                    <th>Order # / Info</th>
                    <th>Customer Coordinates</th>
                    <th>Total Billing</th>
                    <th>Current State</th>
                    <th>Update Order State</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => (
                    <tr key={order.id} className="order-row glass-row fade-in">
                      <td className="order-num-col">
                        <strong>{order.orderNumber}</strong>
                        <span className="order-db-id">ID: {order.id}</span>
                        <span className="order-time-stamp">{new Date(order.createdAt).toLocaleString()}</span>
                      </td>
                      <td>
                        <div><strong>{order.customerName}</strong></div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>📞 {order.mobileNumber}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>📍 {order.deliveryAddress}</div>
                      </td>
                      <td className="amount-col">₹{order.totalAmount?.toFixed(0)}</td>
                      <td>
                        <span className={`status-badge ${getStatusClass(order.status)}`}>
                          {order.status === 'DELIVERED' ? 'DELIVERED & PAID' : (order.status === 'DELIVERED_UNPAID' ? 'DELIVERED & UNPAID' : order.status)}
                        </span>
                      </td>
                      <td>
                        <select
                          value={order.status}
                          disabled={updatingId === order.id}
                          onChange={(e) => handleStatusChange(order.id, e.target.value)}
                          className="input-glow"
                          style={{
                            fontSize: '0.85rem',
                            padding: '0.4rem 0.8rem',
                            background: '#111115',
                            borderColor: 'var(--card-border)'
                          }}
                        >
                          <option value="PLACED">Placed</option>
                          <option value="PAID">Payment Completed (PAID)</option>
                          <option value="PREPARING">Preparing Food</option>
                          <option value="OUT_FOR_DELIVERY">Out for Delivery</option>
                          <option value="DELIVERED">Delivered</option>
                          <option value="DELIVERED_UNPAID">Delivered (UNPAID)</option>
                          <option value="PAYMENT_FAILED">Payment Failed</option>
                          <option value="KITCHEN_FAILED">Kitchen Failed</option>
                          <option value="DELIVERY_FAILED">Delivery Failed</option>
                        </select>
                      </td>
                      <td className="items-col">
                        <ul className="items-list">
                          {order.items?.map((item, idx) => (
                            <li key={item.id || idx}>
                              Prod #{item.productId} &times; {item.quantity} (₹{item.price?.toFixed(0)})
                            </li>
                          ))}
                        </ul>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      )}

      {/* 2. MANAGE MENU */}
      {activeTab === 'menu' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '2rem' }}>
          {/* Menu Items Table */}
          <div className="card glass-card">
            <h3>Current Menu Items</h3>
            <div style={{ maxHeight: '550px', overflowY: 'auto', marginTop: '1rem' }}>
              <table className="orders-table">
                <thead>
                  <tr>
                    <th>Item Details</th>
                    <th>Price</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {menuItems.map((item) => (
                    <tr key={item.id} className="order-row glass-row">
                      <td>
                        <strong>{item.name}</strong>
                        <span className={`food-badge-mini ${item.veg ? 'veg' : 'non-veg'}`} style={{ marginLeft: '0.5rem' }}>
                          {item.veg ? 'Veg' : 'Non-Veg'}
                        </span>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{item.description}</div>
                      </td>
                      <td>
                        {editingItemId === item.id ? (
                          <input 
                            type="number" 
                            value={editPriceVal} 
                            onChange={(e) => setEditPriceVal(e.target.value)}
                            style={{ width: '80px', padding: '0.3rem', background: '#111', color: '#fff', border: '1px solid #333', borderRadius: '4px' }}
                          />
                        ) : (
                          <span>₹{item.price?.toFixed(0)}</span>
                        )}
                      </td>
                      <td>
                        {editingItemId === item.id ? (
                          <div style={{ display: 'flex', gap: '0.3rem' }}>
                            <button onClick={() => handleUpdatePrice(item.id)} className="btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', background: 'var(--accent-green)', boxShadow: 'none' }}>Save</button>
                            <button onClick={() => setEditingItemId(null)} className="btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', background: 'var(--accent-red)', boxShadow: 'none' }}>Cancel</button>
                          </div>
                        ) : (
                          <button 
                            onClick={() => {
                              setEditingItemId(item.id);
                              setEditPriceVal(item.price);
                            }} 
                            className="btn-primary" 
                            style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', boxShadow: 'none' }}
                          >
                            Edit Price
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Add Food Item Form */}
          <div className="card glass-card">
            <h3>Add New Food Item</h3>
            <form onSubmit={handleAddFood} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginTop: '1rem' }}>
              <div className="form-group">
                <label>Food Name</label>
                <input 
                  type="text" 
                  value={newFood.name} 
                  onChange={(e) => setNewFood({ ...newFood, name: e.target.value })}
                  placeholder="e.g. French Fries"
                  required
                  className="input-glow"
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input 
                  type="text" 
                  value={newFood.description} 
                  onChange={(e) => setNewFood({ ...newFood, description: e.target.value })}
                  placeholder="e.g. Crispy golden salted fries"
                  required
                  className="input-glow"
                />
              </div>
              <div className="form-group">
                <label>Price (₹)</label>
                <input 
                  type="number" 
                  value={newFood.price} 
                  onChange={(e) => setNewFood({ ...newFood, price: e.target.value })}
                  placeholder="e.g. 120"
                  required
                  className="input-glow"
                />
              </div>
              <div className="form-group">
                <label>Product Image URL</label>
                <input 
                  type="url" 
                  value={newFood.imageUrl} 
                  onChange={(e) => setNewFood({ ...newFood, imageUrl: e.target.value })}
                  placeholder="e.g. https://images.unsplash.com/photo-..."
                  required
                  className="input-glow"
                />
              </div>
              <div className="form-group">
                <label>Veg / Non-Veg</label>
                <select 
                  value={newFood.isVeg ? 'true' : 'false'} 
                  onChange={(e) => setNewFood({ ...newFood, isVeg: e.target.value === 'true' })}
                  className="input-glow"
                  style={{ background: '#1e1e24', color: '#fff' }}
                >
                  <option value="true">🟢 Veg</option>
                  <option value="false">🔴 Non-Veg</option>
                </select>
              </div>
              <button type="submit" className="btn-primary" style={{ background: 'var(--accent-green)', marginTop: '0.5rem' }}>Add Item to Menu</button>
            </form>
          </div>
        </div>
      )}

      {/* 3. ANNOUNCE OFFERS */}
      {activeTab === 'offers' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '2rem' }}>
          {/* Offers Table */}
          <div className="card glass-card">
            <h3>Announced Offers</h3>
            <div style={{ maxHeight: '550px', overflowY: 'auto', marginTop: '1rem' }}>
              <table className="orders-table">
                <thead>
                  <tr>
                    <th>Offer Announcement</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {offers.map((offer) => (
                    <tr key={offer.id} className="order-row glass-row">
                      <td style={{ fontSize: '0.9rem' }}>{offer.text}</td>
                      <td>
                        <span className={`status-badge ${offer.active ? 'status-delivered' : 'status-failed'}`}>
                          {offer.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td>
                        <button onClick={() => handleDeleteOffer(offer.id)} className="btn-primary" style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem', background: 'var(--accent-red)', boxShadow: 'none' }}>Delete</button>
                      </td>
                    </tr>
                  ))}
                  {offers.length === 0 && (
                    <tr>
                      <td colSpan="3" style={{ textAlign: 'center', padding: '1rem', color: 'var(--text-secondary)' }}>No offers announced yet.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* New Offer Announcement Form */}
          <div className="card glass-card">
            <h3>Announce a New Offer</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '0.5rem 0 1rem 0' }}>Creating a new offer will replace the active announcement banner shown at the top of the client layout in real-time.</p>
            <form onSubmit={handleAddOffer} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div className="form-group">
                <label>Offer Announcement Text</label>
                <textarea 
                  value={newOfferText} 
                  onChange={(e) => setNewOfferText(e.target.value)}
                  placeholder="e.g. 🎉 FLAT 30% OFF ON ALL PIZZAS! USE CODE: WAFFOR30 🎉"
                  required
                  rows="4"
                  className="input-glow"
                  style={{ background: '#111115', color: '#fff', padding: '0.8rem', borderRadius: '12px', border: '1px solid var(--card-border)', resize: 'vertical' }}
                />
              </div>
              <button type="submit" className="btn-primary" style={{ background: 'var(--accent-green)' }}>Publish Announcement</button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
