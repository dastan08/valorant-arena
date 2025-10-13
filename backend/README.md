# Valorant Arena - Backend

This is the backend service for Valorant Arena, a competitive tournament platform for Valorant players.

## Features

- User authentication and authorization
- Tournament management
- Team management
- Match scheduling and results
- ELO ranking system
- Real-time updates with Socket.IO
- File uploads for user avatars and team logos
- Rate limiting and security best practices

## Tech Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MongoDB with Mongoose
- **Authentication**: JWT
- **Real-time**: Socket.IO
- **Caching**: Redis
- **Logging**: Winston
- **Validation**: Express Validator
- **File Upload**: Multer
- **API Documentation**: Swagger/OpenAPI

## Prerequisites

- Node.js (v16 or higher)
- MongoDB (v4.4 or higher)
- Redis (v6 or higher)
- npm (v7 or higher) or yarn

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/valorant-arena.git
   cd valorant-arena/backend
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn
   ```

3. **Set up environment variables**
   - Copy `.env.example` to `.env`
   - Update the values in `.env` with your configuration

4. **Start the development server**
   ```bash
   # Development
   npm run dev
   
   # Production
   npm start
   ```

5. **Access the API**
   The API will be available at `http://localhost:5000`

## API Documentation

After starting the server, you can access the API documentation at:
- Swagger UI: `http://localhost:5000/api-docs`
- OpenAPI JSON: `http://localhost:5000/api-docs.json`

## Project Structure

```
backend/
├── src/
│   ├── config/         # Configuration files
│   ├── controllers/    # Route controllers
│   ├── middleware/     # Custom middleware
│   ├── models/         # Mongoose models
│   ├── routes/         # API routes
│   ├── services/       # Business logic
│   ├── utils/          # Utility functions
│   ├── validators/     # Request validators
│   └── index.js        # Application entry point
├── .env.example        # Environment variables example
├── .eslintrc.js        # ESLint configuration
├── .prettierrc         # Prettier configuration
├── package.json
└── README.md
```

## Environment Variables

See `.env.example` for all available environment variables.

## Development

### Linting
```bash
# Run ESLint
npm run lint

# Fix linting issues
npm run lint:fix
```

### Testing
```bash
# Run tests
npm test

# Run tests with coverage
npm run test:coverage
```

## Deployment

### Production Build
```bash
# Build the application
npm run build

# Start the production server
npm start
```

### Docker
```bash
# Build the Docker image
docker build -t valorant-arena-backend .

# Run the container
docker run -p 5000:5000 valorant-arena-backend
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Riot Games API](https://developer.riotgames.com/)
- [Valorant API](https://dash.valorant-api.com/)
- [Node.js](https://nodejs.org/)
- [Express](https://expressjs.com/)
- [MongoDB](https://www.mongodb.com/)
