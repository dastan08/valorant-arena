const express = require('express');
const router = express.Router();
const { check } = require('express-validator');
const authController = require('../controllers/authController');
const auth = require('../middleware/auth');

// @route   POST api/auth/register
// @desc    Register a new user
// @access  Public
router.post(
  '/register',
  [
    check('username', 'Username is required').not().isEmpty(),
    check('email', 'Please include a valid email').isEmail(),
    check(
      'password',
      'Please enter a password with 6 or more characters'
    ).isLength({ min: 6 }),
    check('valorantId', 'Valorant ID is required').not().isEmpty(),
    check('rank', 'Rank is required').isIn([
      'Iron', 'Bronze', 'Silver', 'Gold',
      'Platinum', 'Diamond', 'Immortal', 'Radiant'
    ]),
  ],
  authController.register
);

// @route   POST api/auth/login
// @desc    Authenticate user & get token
// @access  Public
router.post(
  '/login',
  [
    check('email', 'Please include a valid email').isEmail(),
    check('password', 'Password is required').exists(),
  ],
  authController.login
);

// @route   GET api/auth/me
// @desc    Get current user profile
// @access  Private
router.get('/me', auth, authController.getMe);

// @route   PUT api/auth/me
// @desc    Update user profile
// @access  Private
router.put(
  '/me',
  [
    auth,
    [
      check('username', 'Username is required').optional().not().isEmpty(),
      check('email', 'Please include a valid email').optional().isEmail(),
      check('valorantId', 'Valorant ID is required').optional().not().isEmpty(),
      check('rank', 'Invalid rank').optional().isIn([
        'Iron', 'Bronze', 'Silver', 'Gold',
        'Platinum', 'Diamond', 'Immortal', 'Radiant'
      ]),
    ],
  ],
  authController.updateProfile
);

// @route   PUT api/auth/change-password
// @desc    Change user password
// @access  Private
router.put(
  '/change-password',
  [
    auth,
    [
      check('currentPassword', 'Current password is required').exists(),
      check(
        'newPassword',
        'Please enter a password with 6 or more characters'
      ).isLength({ min: 6 }),
    ],
  ],
  authController.changePassword
);

module.exports = router;
