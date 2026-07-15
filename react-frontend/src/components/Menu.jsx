import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Menu = ({ addToCart, searchQuery }) => {
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [filter, setFilter] = useState('all'); // 'all' | 'veg' | 'non-veg'

  useEffect(() => {
    const fetchMenu = async () => {
      try {
        const response = await axios.get('/api/food');
        setMenuItems(response.data);
        setError(null);
      } catch (err) {
        console.error('Error fetching food items:', err);
        setError('Failed to load menu items. Please check if backend services are running.');
      } finally {
        setLoading(false);
      }
    };

    fetchMenu();
  }, []);

  const filteredItems = menuItems.filter((item) => {
    // Search query match
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          item.description.toLowerCase().includes(searchQuery.toLowerCase());
    
    // Veg/Non-Veg match
    if (filter === 'veg') return matchesSearch && item.veg;
    if (filter === 'non-veg') return matchesSearch && !item.veg;
    return matchesSearch;
  });

  const getImageUrl = (imgUrl) => {
    if (imgUrl && (imgUrl.startsWith('http://') || imgUrl.startsWith('https://'))) {
      return imgUrl;
    }
    switch (imgUrl) {
      case 'pizza': return 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=400&q=80';
      case 'burger': return 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80';
      case 'pasta': return 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=400&q=80';
      case 'bread': return 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=400&q=80';
      case 'wings': return 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=400&q=80';
      case 'brownie': return 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=400&q=80';
      default: return 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=400&q=80';
    }
  };

  const renderStars = (rating) => {
    const r = parseFloat(rating) || 0;
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      if (i <= Math.round(r)) {
        stars.push(<span key={i} className="star-filled">⭐</span>);
      } else {
        stars.push(<span key={i} className="star-empty">☆</span>);
      }
    }
    return stars;
  };

  if (loading) {
    return (
      <div className="loader-container">
        <div className="loader"></div>
        <p>Loading Waffor Food Specialities...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="alert-box alert-error fade-in">
        <p>{error}</p>
      </div>
    );
  }

  return (
    <div className="menu-container fade-in">
      <div className="menu-header">
        <h2 className="title-gradient">Our Signature Menu</h2>
        <div className="menu-filters">
          <button
            onClick={() => setFilter('all')}
            className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
          >
            All Items
          </button>
          <button
            onClick={() => setFilter('veg')}
            className={`filter-btn ${filter === 'veg' ? 'active' : ''}`}
          >
            🟢 Veg Only
          </button>
          <button
            onClick={() => setFilter('non-veg')}
            className={`filter-btn ${filter === 'non-veg' ? 'active' : ''}`}
          >
            🔴 Non-Veg Only
          </button>
        </div>
      </div>

      {filteredItems.length === 0 ? (
        <div className="empty-state">
          <p>No dishes found matching your criteria. Try another search!</p>
        </div>
      ) : (
        <div className="menu-grid">
          {filteredItems.map((item) => (
            <div key={item.id} className="card glass-card food-card fade-in">
              <div className="food-image-wrapper" style={{ height: '180px', overflow: 'hidden', position: 'relative' }}>
                <img 
                  src={getImageUrl(item.imageUrl)} 
                  alt={item.name}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
                <span className={`food-badge ${item.veg ? 'veg' : 'non-veg'}`}>
                  {item.veg ? '🟢 Veg' : '🔴 Non-Veg'}
                </span>
              </div>
              <div className="food-card-content">
                <div className="food-title-row">
                  <h3 className="food-name">{item.name}</h3>
                  <div className="food-rating">{renderStars(item.rating)}</div>
                </div>
                <p className="food-description">{item.description}</p>
                <div className="food-card-footer">
                  <span className="food-price">₹{item.price.toFixed(0)}</span>
                  <button onClick={() => addToCart(item)} className="btn-primary add-to-cart-btn">
                    Add to Cart
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Menu;
