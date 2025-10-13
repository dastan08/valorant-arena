// Production Environment Configuration
// Update this file with your production API URL before building

export const environment = {
  production: true,
  // Replace with your EC2 public IP or domain
  apiUrl: 'http://223.185.30.87/api'
  
  // When you get a domain, update to:
  // apiUrl: 'https://yourdomain.com/api'
  
  // Or if using CloudFront:
  // apiUrl: 'https://your-cloudfront-url/api'
};
