// Production Environment Configuration
// Update this file with your production API URL before building

export const environment = {
  production: true,
  // EC2 backend API URL
  apiUrl: 'http://13.201.185.135:8080/api'
  
  // When you get a domain, update to:
  // apiUrl: 'https://yourdomain.com/api'
  
  // Or if using CloudFront:
  // apiUrl: 'https://your-cloudfront-url/api'
};
