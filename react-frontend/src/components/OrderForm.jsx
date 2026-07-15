import React, { useState } from 'react';
import axios from 'axios';

const OrderForm = ({ onOrderPlaced }) => {
  const [customerId, setCustomerId] = useState('');
  const [items, setItems] = useState([{ productId: '', quantity: '', price: '' }]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const handleItemChange = (index, field, value) => {
    const newItems = [...items];
    newItems[index][field] = value;
    setItems(newItems);
  };

  const addItem = () => {
    setItems([...items, { productId: '', quantity: '', price: '' }]);
  };

  const removeItem = (index) => {
    if (items.length > 1) {
      setItems(items.filter((_, i) => i !== index));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);

    // Validate inputs
    const formattedCustomerId = parseInt(customerId, 10);
    if (isNaN(formattedCustomerId)) {
      setMessage({ type: 'error', text: 'Please enter a valid Customer ID.' });
      setLoading(false);
      return;
    }

    const formattedItems = [];
    for (let i = 0; i < items.length; i++) {
      const item = items[i];
      const prodId = parseInt(item.productId, 10);
      const qty = parseInt(item.quantity, 10);
      const prc = parseFloat(item.price);

      if (isNaN(prodId) || isNaN(qty) || isNaN(prc)) {
        setMessage({ type: 'error', text: `Please fill in valid numbers for all items.` });
        setLoading(false);
        return;
      }
      formattedItems.push({ productId: prodId, quantity: qty, price: prc });
    }

    try {
      const response = await axios.post('/api/orders', {
        customerId: formattedCustomerId,
        items: formattedItems
      });

      setMessage({ type: 'success', text: `Order created successfully! Order #: ${response.data.orderNumber}` });
      setCustomerId('');
      setItems([{ productId: '', quantity: '', price: '' }]);
      if (onOrderPlaced) {
        onOrderPlaced();
      }
    } catch (error) {
      console.error('Error placing order:', error);
      const errMsg = error.response?.data?.message || error.message || 'Unknown backend error';
      setMessage({ type: 'error', text: `Failed to place order: ${errMsg}` });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card glass-card fade-in">
      <h2 className="title-gradient">Place New Order</h2>
      <form onSubmit={handleSubmit} className="order-form">
        <div className="form-group">
          <label htmlFor="customerId">Customer ID</label>
          <input
            type="number"
            id="customerId"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
            placeholder="e.g. 1001"
            required
            className="input-glow"
          />
        </div>

        <div className="items-section">
          <h3>Order Items</h3>
          {items.map((item, index) => (
            <div key={index} className="item-row glass-row fade-in">
              <div className="form-group inline">
                <label>Product ID</label>
                <input
                  type="number"
                  value={item.productId}
                  onChange={(e) => handleItemChange(index, 'productId', e.target.value)}
                  placeholder="e.g. 42"
                  required
                  className="input-glow"
                />
              </div>
              <div className="form-group inline">
                <label>Quantity</label>
                <input
                  type="number"
                  value={item.quantity}
                  onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                  placeholder="e.g. 2"
                  min="1"
                  required
                  className="input-glow"
                />
              </div>
              <div className="form-group inline">
                <label>Price ($)</label>
                <input
                  type="number"
                  step="0.01"
                  value={item.price}
                  onChange={(e) => handleItemChange(index, 'price', e.target.value)}
                  placeholder="e.g. 12.99"
                  required
                  className="input-glow"
                />
              </div>
              {items.length > 1 && (
                <button
                  type="button"
                  onClick={() => removeItem(index)}
                  className="btn-danger-icon"
                  title="Remove Item"
                >
                  &times;
                </button>
              )}
            </div>
          ))}

          <button type="button" onClick={addItem} className="btn-secondary">
            + Add Product
          </button>
        </div>

        {message && (
          <div className={`alert-box ${message.type === 'error' ? 'alert-error' : 'alert-success'} fade-in`}>
            {message.text}
          </div>
        )}

        <button type="submit" disabled={loading} className="btn-primary btn-block">
          {loading ? 'Submitting...' : 'Submit Order'}
        </button>
      </form>
    </div>
  );
};

export default OrderForm;
