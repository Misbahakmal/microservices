```
com.demo.ORDER_SERVICE
├── domain/
│   └── model/          ← Order, OrderItem, OrderEvent, OrderStatus (state machine)
├── application/
│   ├── port/in/        ← CreateOrderUseCase, GetOrderUseCase, UpdateOrderStatusUseCase
│   ├── port/out/       ← OrderRepositoryPort, OrderEventPublisherPort, CustomerServicePort
│   └── usecase/        ← OrderService
└── infrastructure/
    ├── adapter/in/web/           ← OrderController (implements generated API interface)
    ├── adapter/out/persistence/  ← JPA entities + OrderPersistenceAdapter
    ├── adapter/out/messaging/    ← KafkaOrderEventPublisher
    ├── adapter/out/rest/         ← CustomerServiceRestAdapter (WebClient)
    └── config/                   ← InfrastructureConfig (Kafka transactional producer, WebClient)
```

  ##  to start docker
  docker-compose up -d

  ##  to end 
  docker-compose down -v

  ##  to check log of container
  docker logs customer_service-kafka-1

  ##  if postgres is running locally stop it 
   sudo systemctl stop postgresql

   ## first clean compile
./mvnw clean compile or ./mvnw spring-boot:run

   ##  to check port if already running other process
 lsof -i : portnumber the kill -9 portnumber

 
  ## to start docker
  docker-compose up -d

  ## to end 
  docker-compose down -v

  ## to check log of container
  docker logs customer_service-kafka-1

  ## if postgres is running locally stop it 
   sudo systemctl stop postgresql

## first clean compile
./mvnw clean compile or ./mvnw spring-boot:run

 to check port if already running other process
 lsof -i : portnumber the kill -9 portnumber

 ## Endpoints

| Method | Path                    | Description                     | Status |
|--------|-------------------------|---------------------------------|--------|
| POST   | /orders                 | Create order                    | 201    |
| GET    | /orders                 | List orders (filterable)        | 200    |
| GET    | /orders/{id}            | Get order by ID                 | 200    |
| PATCH  | /orders/{id}/status     | Update status (ship/cancel)     | 200    |

## Example
// http://localhost:8082/orders  
// http://localhost:8081/orders/id



## to check order and add order with X-Idempotency-Key in header
X-Idempotency-Key = 550e8400-e29b-41d4-a716-446655440000
Content-Type	application/json

```json 
//add orders
{
  "customerId": "5624c38f-ef19-4715-9d98-91f5ee49f63b",
  "addressId": "be504958-cea6-4192-a39c-3cb9d3fcc8b2",
  "items": [
    {
      "productId": "7858c241-1191-4963-8d07-28564177d452",
      "productName": "monitor",
      "quantity": 1,
      "unitPrice": 10.00
    }
  ]
} 
```
## after order creation check if event is created or not 
docker exec -it customer_service-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic order-events --from-beginning
//pathch
## http://localhost:8082/orders/order_id/status

## for oder patch request without header
json  
```
{
  "status": "SHIPPED"
}
```

## Order Status State Machine

```
CREATED ──► SHIPPED
CREATED ──► CANCELLED
```
All other transitions are rejected with HTTP 400.

## Kafka Events

**Topic:** `order-events`  **Partitions:** 3  **Key:** `orderId`

### Event schema (v1)

```json
{
  "eventId":    "uuid",
  "orderId":    "uuid",
  "occurredAt": "2024-01-15T10:30:00Z",
  "status":     "CREATED | SHIPPED | CANCELLED"
}
```
## To create partitions explicitly
run inside kafka container

kafka-topics --create \
  --topic order-events \
  --partitions 3 \
  --bootstrap-server localhost:9092

  ##check partitions
  kafka-topics --describe \
  --topic order-events \
  --bootstrap-server localhost:9092