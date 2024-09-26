# OrderFlux

OrderFlux is a simple high-performance, in-memory order book system built with Vert.x and Kotlin. It provides a robust API for managing limit orders, viewing the order book, and retrieving trade history.

## Features

- In-memory order book with fast order matching
- RESTful API for order management and data retrieval
- JWT-based authentication for secure access
- Real-time order book updates
- Trade history tracking

## Setup

1. Clone the repository:
   ```
   git clone https://github.com/iamramtin/OrderFlux.git
   cd orderflux
   ```

2. Build the project:
   ```
   ./gradlew build
   ```

3. Run the application:
   ```
   ./gradlew run
   ```

The server will start on `http://localhost:8888`.

### Docker Setup

1. Build the Docker image:
   ```
   docker build -t orderflux .
   ```

2. Run the Docker container:
   ```
   docker run -p 8888:8888 orderflux
   ```

The server will be accessible at `http://localhost:8888`.

## API Endpoints

All endpoints except `/login` require JWT authentication.

### Authentication

- **Login**: `POST /login`
    - Request body: `{"username": "username", "password": "password"}`
    - Response: `{"token": "YOUR_JWT_TOKEN"}`

### Order Book Operations

- **Get Order Book**: `GET /BTCZAR/orderbook`
- **Submit Limit Order**: `POST /orders/limit`
    - Request body: `{"side": "BUY", "price": 50000.0, "quantity": 0.1}`
- **Get Trade History**: `GET /BTCZAR/tradehistory`
- **Get Specific Trade**: `GET /BTCZAR/trade/{id}`
- **Repopulate Order Book**: `POST /orderbook/init`
    
## Usage Examples

1. Login to get a token:
   ```
   curl -X POST http://localhost:8888/login -H "Content-Type: application/json" -d '{"username":"admin","password":"password"}'
   ```

2. Use the token for authenticated requests:
   ```
   curl -X GET http://localhost:8888/BTCZAR/orderbook -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

3. Submit a limit order:
   ```
   curl -X POST http://localhost:8888/orders/limit \
   -H "Authorization: Bearer YOUR_TOKEN_HERE" \
   -H "Content-Type: application/json" \
   -d '{"side":"BUY","price":50000.0,"quantity":0.1}'
   ```

4. Get a specific trade:
   ```
   curl -X GET http://localhost:8888/BTCZAR/trade/TRADE_ID_HERE -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

## Security Considerations

- The JWT secret key is hardcoded in this example. In a production environment, use a secure method to manage your secret keys.
- Implement proper user management and password hashing for production use.
- Always use HTTPS in production to encrypt all traffic.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.txt) file for details.