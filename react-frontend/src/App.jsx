import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Auth from './components/Auth';
import Home from './components/Home';
import Menu from './components/Menu';
import Cart from './components/Cart';
import Checkout from './components/Checkout';
import OrderTracking from './components/OrderTracking';
import MyOrders from './components/MyOrders';
import AdminDashboard from './components/AdminDashboard';
import DeliveryDashboard from './components/DeliveryDashboard';
import Profile from './components/Profile';
import './App.css';

function App() {
  const [user, setUser] = useState(null);
  const [selectedRole, setSelectedRole] = useState(null); // 'CUSTOMER' | 'ADMIN' | 'DELIVERY'
  const [activeTab, setActiveTab] = useState('home'); // 'home' | 'menu' | 'cart' | 'checkout' | 'tracking' | 'orders'
  const [cartItems, setCartItems] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTrackingId, setActiveTrackingId] = useState(null);

  const [activeOffer, setActiveOffer] = useState(null);

  // Check auth status on load
  useEffect(() => {
    const cachedUser = localStorage.getItem('user');
    if (cachedUser) {
      setUser(JSON.parse(cachedUser));
    }
  }, []);

  // Fetch active offer announcements
  useEffect(() => {
    const fetchActiveOffer = async () => {
      try {
        const response = await axios.get('/api/offers');
        if (response.data && response.data.length > 0) {
          setActiveOffer(response.data[0]);
        } else {
          setActiveOffer(null);
        }
      } catch (err) {
        console.error('Error fetching active offers:', err);
      }
    };

    fetchActiveOffer();
    const interval = setInterval(fetchActiveOffer, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('user');
    setUser(null);
    setSelectedRole(null);
    setActiveTab('home');
    setCartItems([]);
  };

  const addToCart = (item) => {
    const exists = cartItems.find((i) => i.id === item.id);
    if (exists) {
      setCartItems(cartItems.map((i) => i.id === item.id ? { ...i, quantity: i.quantity + 1 } : i));
    } else {
      setCartItems([...cartItems, { ...item, quantity: 1 }]);
    }
    // Redirect to cart or notify user
    setActiveTab('cart');
  };

  const updateQuantity = (itemId, newQty) => {
    if (newQty <= 0) {
      removeFromCart(itemId);
    } else {
      setCartItems(cartItems.map((item) => item.id === itemId ? { ...item, quantity: newQty } : item));
    }
  };

  const removeFromCart = (itemId) => {
    setCartItems(cartItems.filter((item) => item.id !== itemId));
  };

  const handleOrderPlaced = (orderId) => {
    setActiveTrackingId(orderId);
    setActiveTab('tracking');
  };

  const handleTrackOrderFromHistory = (orderId) => {
    setActiveTrackingId(orderId);
    setActiveTab('tracking');
  };

  const cartCount = cartItems.reduce((count, item) => count + item.quantity, 0);

  if (!user && !selectedRole) {
    return (
      <div className="role-selection-container fade-in">
        <div className="role-selection-title">
          <span className="brand-logo" style={{ fontSize: '4rem', display: 'block', marginBottom: '1rem' }}>🍳</span>
          <h2 className="title-gradient" style={{ fontSize: '3rem', margin: 0 }}>Waffor Food</h2>
          <p>Choose your portal to get started</p>
        </div>
        <div className="role-cards-grid">
          <div className="card glass-card role-card" onClick={() => setSelectedRole('CUSTOMER')}>
            <span className="role-card-icon">🍔</span>
            <h3>Customer</h3>
            <p>Order fresh, delicious food items and track them in real-time.</p>
          </div>
          <div className="card glass-card role-card" onClick={() => setSelectedRole('ADMIN')}>
            <span className="role-card-icon">👑</span>
            <h3>Admin Panel</h3>
            <p>Manage order lifecycle, track billing states, and update menu catalog.</p>
          </div>
          <div className="card glass-card role-card" onClick={() => setSelectedRole('DELIVERY')}>
            <span className="role-card-icon">🛵</span>
            <h3>Delivery Partner</h3>
            <p>View available pickups, navigate destinations, and deliver hot food.</p>
          </div>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <Auth
        onLoginSuccess={(u) => setUser(u)}
        selectedRole={selectedRole}
        onBackToRoles={() => setSelectedRole(null)}
      />
    );
  }

  if (user.role === 'DELIVERY') {
    return (
      <div className="app-container">
        <main className="main-content">
          <DeliveryDashboard user={user} handleLogout={handleLogout} />
        </main>
        <footer className="app-footer">
          <p>&copy; 2026 Waffor Food Inc. Delivery Partner System.</p>
        </footer>
      </div>
    );
  }

  if (user.role === 'ADMIN') {
    return (
      <div className="app-container">
        <header className="app-header glass-header">
          <div className="brand">
            <span className="brand-logo">🍳</span>
            <h1>Waffor Food Panel</h1>
          </div>
          <nav className="navigation">
            <div className="user-profile-section">
              <span className="user-name-label">Admin: {user.name || 'System'}</span>
              <button onClick={handleLogout} className="logout-btn">
                Logout
              </button>
            </div>
          </nav>
        </header>

        <main className="main-content">
          <AdminDashboard />
        </main>

        <footer className="app-footer">
          <p>&copy; 2026 Waffor Food Inc. Administration System.</p>
        </footer>
      </div>
    );
  }

  return (
    <div className="app-container">
      {activeOffer && (
        <div className="offer-announcement-bar" style={{
          background: 'linear-gradient(90deg, #ff4e50, #f9d423)',
          color: '#000',
          padding: '0.6rem 1rem',
          textAlign: 'center',
          fontWeight: 'bold',
          fontSize: '0.9rem',
          overflow: 'hidden',
          whiteSpace: 'nowrap',
          boxShadow: '0 4px 15px rgba(255, 78, 80, 0.3)',
          borderRadius: '0 0 12px 12px'
        }}>
          <marquee scrollamount="5" behavior="scroll" direction="left">
            {activeOffer.text}
          </marquee>
        </div>
      )}
      <header className="app-header glass-header">
        <div className="brand" onClick={() => setActiveTab('home')} style={{ cursor: 'pointer' }}>
          <span className="brand-logo">🍳</span>
          <h1>Waffor Food</h1>
        </div>
        <nav className="navigation">
          <button
            onClick={() => setActiveTab('home')}
            className={`nav-btn ${activeTab === 'home' ? 'active' : ''}`}
          >
            Home
          </button>
          <button
            onClick={() => setActiveTab('menu')}
            className={`nav-btn ${activeTab === 'menu' ? 'active' : ''}`}
          >
            Menu
          </button>
          <button
            onClick={() => setActiveTab('cart')}
            className={`nav-btn ${activeTab === 'cart' ? 'active' : ''}`}
          >
            Cart {cartCount > 0 && <span className="nav-badge">{cartCount}</span>}
          </button>
          <button
            onClick={() => setActiveTab('orders')}
            className={`nav-btn ${activeTab === 'orders' ? 'active' : ''}`}
          >
            My Orders
          </button>
          <button
            onClick={() => setActiveTab('profile')}
            className={`nav-btn ${activeTab === 'profile' ? 'active' : ''}`}
          >
            Profile
          </button>
          
          <div className="user-profile-section">
            <span className="user-name-label">Hi, {user.name || 'Foodie'}</span>
            <button onClick={handleLogout} className="logout-btn">
              Logout
            </button>
          </div>
        </nav>
      </header>

      <main className="main-content">
        {activeTab === 'home' && (
          <Home
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
            onNavigateToMenu={() => setActiveTab('menu')}
          />
        )}
        {activeTab === 'menu' && (
          <Menu
            addToCart={addToCart}
            searchQuery={searchQuery}
          />
        )}
        {activeTab === 'cart' && (
          <Cart
            cartItems={cartItems}
            updateQuantity={updateQuantity}
            removeFromCart={removeFromCart}
            onNavigateToCheckout={() => setActiveTab('checkout')}
          />
        )}
        {activeTab === 'checkout' && (
          <Checkout
            cartItems={cartItems}
            clearCart={() => setCartItems([])}
            onOrderPlaced={handleOrderPlaced}
            activeOffer={activeOffer}
          />
        )}
        {activeTab === 'tracking' && (
          <OrderTracking
            orderId={activeTrackingId}
            onBackToHome={() => setActiveTab('home')}
          />
        )}
        {activeTab === 'orders' && (
          <MyOrders
            onTrackOrder={handleTrackOrderFromHistory}
          />
        )}
        {activeTab === 'profile' && (
          <Profile
            user={user}
            onProfileUpdate={(updatedUser) => setUser(updatedUser)}
          />
        )}
      </main>

      <footer className="app-footer">
        <p>&copy; 2026 Waffor Food Inc. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default App;
