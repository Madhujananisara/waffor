import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Checkout = ({ cartItems, clearCart, onOrderPlaced, activeOffer }) => {
  const [customerName, setCustomerName] = useState('');
  const [mobileNumber, setMobileNumber] = useState('');
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('UPI'); // 'UPI' | 'COD'
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Prefill details from logged-in user
  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      setCustomerName(user.name || '');
      setMobileNumber(user.mobile || '');
      setDeliveryAddress(user.address || '');
    }
  }, []);

  const totalAmount = cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  // Parse discount from activeOffer
  const parseDiscount = (offerText) => {
    if (!offerText) return { type: 'none', value: 0 };
    // Check for percentage, e.g. "15%" or "15 %" or "15 percent"
    const pctMatch = offerText.match(/(\d+(?:\.\d+)?)\s*%/);
    if (pctMatch) {
      return { type: 'percent', value: parseFloat(pctMatch[1]) };
    }
    // Check for flat discount, e.g. "flat 50" or "50 Rs off" or "₹50"
    const flatMatch = offerText.match(/(?:flat|₹|rs\.?)\s*(\d+(?:\.\d+)?)/i) || offerText.match(/(\d+(?:\.\d+)?)\s*(?:rs|rupees|off)/i);
    if (flatMatch) {
      return { type: 'flat', value: parseFloat(flatMatch[1]) };
    }
    return { type: 'none', value: 0 };
  };

  const discountDetails = parseDiscount(activeOffer?.text);
  let discountAmount = 0;
  if (discountDetails.type === 'percent') {
    discountAmount = (totalAmount * discountDetails.value) / 100;
  } else if (discountDetails.type === 'flat') {
    discountAmount = Math.min(discountDetails.value, totalAmount);
  }
  const finalPayableAmount = Math.max(0, totalAmount - discountAmount);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : { id: 1 }; // fallback customer ID

    const orderItems = cartItems.map((item) => ({
      productId: item.id,
      quantity: item.quantity,
      price: item.price
    }));

    try {
      const response = await axios.post('/api/orders', {
        customerId: user.id,
        customerName,
        mobileNumber,
        deliveryAddress,
        paymentMethod,
        items: orderItems
      });

      clearCart();
      onOrderPlaced(response.data.id);
    } catch (err) {
      console.error('Error placing order:', err);
      const errMsg = err.response?.data?.message || err.message || 'Could not place order';
      setError(errMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-container fade-in">
      <h2 className="title-gradient">Delivery & Payment Details</h2>

      <div className="checkout-content-wrapper">
        <form onSubmit={handleSubmit} className="card glass-card checkout-form">
          <div className="form-group">
            <label>Customer Name</label>
            <input
              type="text"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
              placeholder="e.g. John Doe"
              required
              className="input-glow"
            />
          </div>

          <div className="form-group">
            <label>Mobile Number</label>
            <input
              type="tel"
              value={mobileNumber}
              onChange={(e) => setMobileNumber(e.target.value)}
              placeholder="e.g. 9876543210"
              required
              className="input-glow"
            />
          </div>

          <div className="form-group">
            <label>Delivery Address</label>
            <input
              type="text"
              value={deliveryAddress}
              onChange={(e) => setDeliveryAddress(e.target.value)}
              placeholder="e.g. Apartment, Street, City"
              required
              className="input-glow"
            />
          </div>

          <div className="form-group">
            <label>Payment Method</label>
            <div className="payment-options">
              <label className={`payment-option-card ${paymentMethod === 'UPI' ? 'active' : ''}`}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="UPI"
                  checked={paymentMethod === 'UPI'}
                  onChange={() => setPaymentMethod('UPI')}
                  className="hidden-radio"
                />
                <span className="payment-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>
                </span>
                <span className="payment-label">Pay via UPI</span>
              </label>

              <label className={`payment-option-card ${paymentMethod === 'COD' ? 'active' : ''}`}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="COD"
                  checked={paymentMethod === 'COD'}
                  onChange={() => setPaymentMethod('COD')}
                  className="hidden-radio"
                />
                <span className="payment-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"></rect><circle cx="12" cy="12" r="2"></circle><path d="M6 12h.01M18 12h.01"></path></svg>
                </span>
                <span className="payment-label">Cash on Delivery</span>
              </label>
            </div>
          </div>

          {error && (
            <div className="alert-box alert-error fade-in">
              {error}
            </div>
          )}

          <button type="submit" disabled={loading || cartItems.length === 0} className="btn-primary btn-block checkout-submit-btn">
            {loading ? 'Placing Order...' : `Place Order (Total: ₹${finalPayableAmount.toFixed(0)})`}
          </button>
        </form>

        <div className="card glass-card order-checkout-summary">
          <h3>Your Order</h3>
          <div className="checkout-summary-list">
            {cartItems.map((item) => (
              <div key={item.id} className="checkout-summary-item">
                <span>{item.name} &times; {item.quantity}</span>
                <span>₹{(item.price * item.quantity).toFixed(0)}</span>
              </div>
            ))}
          </div>
          <hr />
          <div className="checkout-summary-subtotal" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', opacity: 0.8 }}>
            <span>Subtotal:</span>
            <span>₹{totalAmount.toFixed(0)}</span>
          </div>
          {discountAmount > 0 && (
            <div className="checkout-summary-discount" style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: '#4caf50', fontWeight: 'bold' }}>
              <span>Offer Applied ({activeOffer.text}):</span>
              <span>-₹{discountAmount.toFixed(0)}</span>
            </div>
          )}
          <hr />
          <div className="checkout-summary-total">
            <span>Total Payable:</span>
            <span>₹{finalPayableAmount.toFixed(0)}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
