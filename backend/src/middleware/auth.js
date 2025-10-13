const jwt = require('jsonwebtoken');
const User = require('../models/User');
const logger = require('../utils/logger');

// Middleware to verify JWT token and protect routes
const auth = async (req, res, next) => {
  try {
    // Get token from header
    const token = req.header('x-auth-token');

    // Check if no token
    if (!token) {
      return res.status(401).json({ 
        success: false,
        message: 'No token, authorization denied' 
      });
    }

    // Verify token
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      req.user = await User.findById(decoded.id).select('-password');
      next();
    } catch (err) {
      return res.status(401).json({ 
        success: false,
        message: 'Token is not valid' 
      });
    }
  } catch (err) {
    logger.error(`Error in auth middleware: ${err.message}`);
    return res.status(500).json({ 
      success: false,
      message: 'Server error' 
    });
  }
};

// Middleware to check if user has admin role
const admin = (req, res, next) => {
  if (req.user && req.user.role === 'admin') {
    next();
  } else {
    res.status(403).json({ 
      success: false,
      message: 'Admin access required' 
    });
  }
};

// Middleware to check if user has moderator role
const moderator = (req, res, next) => {
  if (req.user && (req.user.role === 'moderator' || req.user.role === 'admin')) {
    next();
  } else {
    res.status(403).json({ 
      success: false,
      message: 'Moderator access required' 
    });
  }
};

module.exports = { auth, admin, moderator };
