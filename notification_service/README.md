```
com.demo.NOTIFICATION_SERVICE
├── domain/
│   └── model/                          ← Notification,
├── application/
│   ├── port/in/                        ← //
│   ├── port/out/                       ← NotificationRepositoryPort
│   └── usecase/                        ← NotificationService
└── infrastructure/
    ├── adapter/in/messaging/           ← OrderEventConsumer
    ├── adapter/out/persistence/  ← JPA entities + NotificationPersistenceAdapter
    |── config                    ←   kafkaConfig

```
## run
./mvnw spring-boot:run