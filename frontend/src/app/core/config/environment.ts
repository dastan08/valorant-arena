// Environment configuration
// This file is used to configure the API URL for different environments

export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};

// For production deployment, you can either:
// 1. Replace the apiUrl before building
// 2. Use Angular's fileReplacements in angular.json
// 3. Use environment variables during build

// Production example:
// export const environment = {
//   production: true,
//   apiUrl: 'http://223.185.30.87/api'  // Your EC2 IP
// };
