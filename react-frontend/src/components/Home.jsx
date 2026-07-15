import React from 'react';

const Home = ({ searchQuery, setSearchQuery, onNavigateToMenu }) => {
  return (
    <div className="home-container fade-in">
      {/* Banner section */}
      <div className="restaurant-banner glass-card">
        <div className="banner-overlay"></div>
        <div className="banner-content">
          <span className="restaurant-logo-large">🍳</span>
          <h1 className="restaurant-title">Waffor Food</h1>
          <p className="restaurant-tagline">Freshly Prepared Gourmet Delights Delivered Hot to Your Doorstep</p>
        </div>
      </div>

      {/* Details & Hours section */}
      <div className="details-grid">
        <div className="card glass-card info-card">
          <h3>📍 Restaurant Details</h3>
          <p><strong>Address:</strong> Waffor City Square, Sector 5, Tech Park Area</p>
          <p><strong>Contact:</strong> +91 98765 43210</p>
          <p><strong>Email:</strong> support@wafforfood.com</p>
          <p className="description-text">We craft artisanal recipes using organically sourced ingredients. From wood-fired chicken pizzas to melting chocolate fudge brownies, every dish is prepared with passion.</p>
        </div>

        <div className="card glass-card info-card">
          <h3>🕒 Opening Hours</h3>
          <ul className="opening-hours-list">
            <li><span>Monday - Friday:</span> <strong>11:00 AM - 11:00 PM</strong></li>
            <li><span>Saturday - Sunday:</span> <strong>10:00 AM - 12:00 AM</strong></li>
            <li className="status-open-now"><span className="dot pulse-green"></span> Open Now</li>
          </ul>
        </div>
      </div>

      {/* Search food bar */}
      <div className="search-section card glass-card">
        <h2>Craving Something Specific?</h2>
        <p>Search your favorite dishes from our curated gourmet menu</p>
        <div className="search-bar-container">
          <input
            type="text"
            placeholder="Search pizza, burger, pasta, garlic bread..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
          <button onClick={onNavigateToMenu} className="btn-primary search-btn">
            Browse Menu
          </button>
        </div>
      </div>
    </div>
  );
};

export default Home;
