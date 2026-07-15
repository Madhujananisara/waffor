import React, { useState, useEffect } from 'react';
import axios from 'axios';

const OrderTracking = ({ orderId, onBackToHome }) => {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [alertShown, setAlertShown] = useState(false);
  const alertShownRef = React.useRef(false);

  const fetchOrderStatus = async () => {
    try {
      const response = await axios.get(`/api/orders/${orderId}`);
      const newOrder = response.data;
      setOrder(newOrder);
      setError(null);

      // Check if status is READY or further to trigger customer alert
      const statusUpper = newOrder.status?.toUpperCase();
      if ((statusUpper === 'READY' || statusUpper === 'OUT_FOR_DELIVERY' || statusUpper === 'DELIVERED') && !alertShownRef.current) {
        alertShownRef.current = true;
        setAlertShown(true);
        // Show HTML5 native audio beep or simple alert
        alert(`🔔 Waffor Food: Order #${newOrder.orderNumber} is ready! Delicious hot food is prepared and ready for delivery.`);
      }
    } catch (err) {
      console.error('Error fetching order tracking:', err);
      setError('Could not fetch order status. Retrying...');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrderStatus();
    
    // Poll order status every 2 seconds
    const interval = setInterval(() => {
      fetchOrderStatus();
    }, 2000);

    return () => clearInterval(interval);
  }, [orderId]);

  const isCOD = order?.paymentMethod === 'COD';

  const PlacedIcon = <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>;
  const PaidIcon = <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>;
  const PreparingIcon = <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"></path></svg>;
  const OutForDeliveryIcon = <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="1" y="3" width="15" height="13"></rect><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon><circle cx="5.5" cy="18.5" r="2.5"></circle><circle cx="18.5" cy="18.5" r="2.5"></circle></svg>;
  const DeliveredIcon = <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>;
  const CashIcon = <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="6" width="20" height="12" rx="2"></rect><circle cx="12" cy="12" r="2"></circle><path d="M6 12h.01M18 12h.01"></path></svg>;

  const steps = isCOD ? [
    { key: 'PLACED', label: 'Placed', icon: PlacedIcon, desc: 'Order received by restaurant' },
    { key: 'PREPARING', label: 'Preparing Food', icon: PreparingIcon, desc: 'Kitchen is preparing your fresh meal' },
    { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: OutForDeliveryIcon, desc: 'Driver is on the way to your address' },
    { key: 'DELIVERED', label: 'Delivered', icon: DeliveredIcon, desc: 'Enjoy your delicious hot meal!' },
    { key: 'PAID', label: 'Payment Successful', icon: CashIcon, desc: 'Delivery partner verified cash received' }
  ] : [
    { key: 'PLACED', label: 'Placed', icon: PlacedIcon, desc: 'Order received by restaurant' },
    { key: 'PAID', label: 'Payment Completed', icon: PaidIcon, desc: 'Transaction processed successfully' },
    { key: 'PREPARING', label: 'Preparing Food', icon: PreparingIcon, desc: 'Kitchen is preparing your fresh meal' },
    { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: OutForDeliveryIcon, desc: 'Driver is on the way to your address' },
    { key: 'DELIVERED', label: 'Delivered', icon: DeliveredIcon, desc: 'Enjoy your delicious hot meal!' }
  ];

  const getStepStatus = (stepKey) => {
    if (!order) return 'upcoming';
    const status = order.status?.toUpperCase();

    if (isCOD) {
      const sequence = ['PLACED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'PAID'];
      let currentIdx = 0;
      if (status === 'PREPARING') currentIdx = 1;
      else if (status === 'OUT_FOR_DELIVERY') currentIdx = 2;
      else if (status === 'DELIVERED_UNPAID') currentIdx = 3;
      else if (status === 'DELIVERED') currentIdx = 4;

      const stepIdx = sequence.indexOf(stepKey);

      if (status === 'KITCHEN_FAILED') {
        if (stepKey === 'PLACED') return 'completed';
        if (stepKey === 'PREPARING') return 'failed';
        return 'upcoming';
      }
      if (status === 'DELIVERY_FAILED') {
        if (sequence.indexOf(stepKey) < 2) return 'completed';
        if (stepKey === 'OUT_FOR_DELIVERY') return 'failed';
        return 'upcoming';
      }
      if (status === 'DELIVERED_UNPAID') {
        if (stepKey === 'PAID') return 'failed';
        if (stepIdx <= 3) return 'completed';
        return 'upcoming';
      }

      if (currentIdx >= stepIdx) {
        return 'completed';
      } else if (currentIdx + 1 === stepIdx) {
        return 'active';
      }
      return 'upcoming';
    } else {
      // Mapping states in sequence
      const sequence = ['PLACED', 'PAID', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED'];
      const currentIdx = sequence.indexOf(status);
      const stepIdx = sequence.indexOf(stepKey);

      if (status === 'PAYMENT_FAILED') {
        if (stepKey === 'PLACED') return 'completed';
        if (stepKey === 'PAID') return 'failed';
        return 'upcoming';
      }
      if (status === 'KITCHEN_FAILED') {
        if (sequence.indexOf(stepKey) < 2) return 'completed';
        if (stepKey === 'PREPARING') return 'failed';
        return 'upcoming';
      }
      if (status === 'DELIVERY_FAILED') {
        if (sequence.indexOf(stepKey) < 3) return 'completed';
        if (stepKey === 'OUT_FOR_DELIVERY') return 'failed';
        return 'upcoming';
      }

      if (currentIdx >= stepIdx) {
        return 'completed';
      } else if (currentIdx + 1 === stepIdx) {
        return 'active';
      }
      return 'upcoming';
    }
  };

  if (loading && !order) {
    return (
      <div className="loader-container">
        <div className="loader"></div>
        <p>Connecting to tracking systems...</p>
      </div>
    );
  }

  return (
    <div className="tracking-container fade-in">
      <h2 className="title-gradient">Track Your Order</h2>

      {error && <div className="alert-box alert-error">{error}</div>}

      {order && (
        <div className="card glass-card tracking-card">
          <div className="tracking-meta">
            <div>
              <h3>Order Number: <span className="text-highlight">{order.orderNumber}</span></h3>
              <p className="order-time-stamp">Placed at: {new Date(order.createdAt).toLocaleTimeString()}</p>
            </div>
            <div className="tracking-amount">
              <h3>₹{order.totalAmount?.toFixed(0)}</h3>
              <span className={`status-badge-tracking ${order.status?.toLowerCase()}`}>
                {order.status === 'DELIVERED_UNPAID' ? 'DELIVERED (UNPAID)' : order.status}
              </span>
            </div>
          </div>

          <hr className="divider" />

          {/* Stepper progress bar */}
          <div className="stepper-container">
            {steps.map((step, index) => {
              const stepStatus = getStepStatus(step.key);
              return (
                <div key={step.key} className={`step-item ${stepStatus}`}>
                  <div className="step-badge">
                    <span className="step-icon">{step.icon}</span>
                    {stepStatus === 'completed' && <span className="step-check">✓</span>}
                    {stepStatus === 'failed' && <span className="step-check">✗</span>}
                  </div>
                  <div className="step-content">
                    <h4 className="step-label">{step.label}</h4>
                    <p className="step-desc">{step.desc}</p>
                  </div>
                  {index < steps.length - 1 && (
                    <div className={`step-line ${getStepStatus(steps[index + 1].key) === 'completed' || stepStatus === 'completed' ? 'completed' : ''}`}></div>
                  )}
                </div>
              );
            })}
          </div>

          <div className="delivery-details-section">
            <h4>📍 Delivery Coordinates</h4>
            <p><strong>Customer Name:</strong> {order.customerName}</p>
            <p><strong>Contact:</strong> {order.mobileNumber}</p>
            <p><strong>Address:</strong> {order.deliveryAddress}</p>
            <p><strong>Payment Method:</strong> {order.paymentMethod === 'COD' ? 'Cash on Delivery' : 'UPI Instant'}</p>
          </div>

          {order.status === 'OUT_FOR_DELIVERY' && (
            <button 
              onClick={async () => {
                try {
                  await axios.put(`/api/orders/${order.id}/status`, { status: 'DELIVERED' });
                  fetchOrderStatus();
                } catch (err) {
                  console.error("Error marking order as received:", err);
                  alert("Failed to mark order as received. Please try again.");
                }
              }} 
              className="btn-primary btn-block"
              style={{ marginBottom: '1rem', background: '#2e7d32', borderColor: '#2e7d32' }}
            >
              ✅ I Received the Order
            </button>
          )}

          <button onClick={onBackToHome} className="btn-secondary btn-block">
            Back to Home Page
          </button>
        </div>
      )}
    </div>
  );
};

export default OrderTracking;
