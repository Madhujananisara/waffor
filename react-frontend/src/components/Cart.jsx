import React from 'react';

const Cart = ({ cartItems, updateQuantity, removeFromCart, onNavigateToCheckout }) => {
  const totalAmount = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  const getImageUrl = (imgUrl) => {
    if (imgUrl && (imgUrl.startsWith('http://') || imgUrl.startsWith('https://'))) {
      return imgUrl;
    }
    switch (imgUrl) {
      case 'pizza': return 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=150&q=80';
      case 'burger': return 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=150&q=80';
      case 'pasta': return 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?auto=format&fit=crop&w=150&q=80';
      case 'bread': return 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=150&q=80';
      case 'wings': return 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=150&q=80';
      case 'brownie': return 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=150&q=80';
      default: return 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=150&q=80';
    }
  };

  return (
    <div className="cart-container fade-in">
      <h2 className="title-gradient">Your Shopping Cart</h2>
      
      {cartItems.length === 0 ? (
        <div className="empty-state">
          <span className="empty-cart-icon">🛒</span>
          <p>Your cart is empty. Add delicious items from the menu!</p>
        </div>
      ) : (
        <div className="cart-content-wrapper">
          <div className="cart-items-list">
            {cartItems.map((item) => (
              <div key={item.id} className="card glass-card cart-item-row fade-in">
                <img 
                  src={getImageUrl(item.imageUrl)} 
                  alt={item.name} 
                  style={{ width: '60px', height: '60px', borderRadius: '12px', objectFit: 'cover' }}
                />
                
                <div className="cart-item-details">
                  <h4 className="cart-item-name">{item.name}</h4>
                  <span className={`food-badge-mini ${item.veg ? 'veg' : 'non-veg'}`}>
                    {item.veg ? 'Veg' : 'Non-Veg'}
                  </span>
                  <p className="cart-item-price-unit">₹{item.price.toFixed(0)} each</p>
                </div>

                <div className="cart-item-quantity-controls">
                  <button
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    className="qty-btn"
                  >
                    -
                  </button>
                  <span className="qty-value">{item.quantity}</span>
                  <button
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    className="qty-btn"
                  >
                    +
                  </button>
                </div>

                <div className="cart-item-total">
                  <p className="item-subtotal">₹{(item.price * item.quantity).toFixed(0)}</p>
                  <button onClick={() => removeFromCart(item.id)} className="btn-remove" title="Remove item">
                    &times;
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="card glass-card cart-summary-card">
            <h3>Order Summary</h3>
            <div className="summary-row">
              <span>Subtotal:</span>
              <span>₹{totalAmount.toFixed(0)}</span>
            </div>
            <div className="summary-row">
              <span>Delivery Charge:</span>
              <span className="text-free">FREE</span>
            </div>
            <hr />
            <div className="summary-row total-row">
              <span>Total Amount:</span>
              <span className="total-price">₹{totalAmount.toFixed(0)}</span>
            </div>

            <button onClick={onNavigateToCheckout} className="btn-primary btn-block checkout-btn">
              Proceed to Checkout
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;
