# Модель даних: UML-діаграми та CRC-картки

## Сутності

| Сутність | Основні поля | Конструктор | Основні методи | Відповідальність |
|---|---|---|---|---|
| `AuctionParticipant` | `participantId`, `displayName` | `AuctionParticipant(String participantId, String displayName)` | `abstract getRole(): String`; `final getDisplayName(): String` | Спільна ідентичність і базова поведінка учасника |
| `Seller` | успадковані `participantId`, `displayName`; `lots` | `Seller(String participantId, String displayName)` | `override getRole(): String`; `override canPerform(Lot lot): boolean`; `createLot(String description, double startingPrice): Lot`; `submitForModeration(Auction auction): void`; `withdraw(Auction auction): void` | Створення, подання та зняття лоту |
| `Bidder` | успадковані `participantId`, `displayName`; `submittedBids` | `Bidder(String participantId, String displayName)` | `override getRole(): String`; `override canPerform(Lot lot): boolean`; `getSubmittedBids(): List<Bid>` | Подання ставки за правилами та перегляд власних ставок |
| `Moderator` | успадковані `participantId`, `displayName`; `reviewedLots` | `Moderator(String participantId, String displayName)` | `override getRole(): String`; `override canPerform(Lot lot): boolean`; `approve(Auction auction): void`; `reject(Auction auction): void` | Перевірка та схвалення або відхилення аукціону |
| `ITradingRules` | Немає полів | Конструктор не застосовується до інтерфейсу | `canPerform(Lot lot): boolean` | Контракт перевірки можливості виконання операції |
| `Auction` | `auctionId`, `status`, `startTime`, `endTime`, `minimumIncrement`, `lot`, `finalPrice`, `winner`, `acquiringService` | `Auction(String auctionId, Lot lot, LocalDateTime startTime, LocalDateTime endTime, double minimumIncrement, AcquiringService acquiringService)` | `submitForModeration(): void`; `approve(): void`; `reject(): void`; `withdraw(): void`; `placeBid(Bidder bidder, double amount): boolean`; `placeBid(Bidder bidder, double amount, LocalDateTime submittedAt): boolean`; `close(): void`; `determineWinner(): Bidder` | Керування життєвим циклом, прийманням ставок і фінальним результатом |
| `Lot` | `lotId`, `description`, `startingPrice`, `bids` | `Lot(String lotId, String description, double startingPrice)` | `addBid(Bid bid): void`; `getHighestBid(): Bid`; `getBids(): List<Bid>` | Опис лоту та композиційний власник ставок |
| `Bid` | `amount`, `submittedAt`, `bidder` | `Bid(double amount, LocalDateTime submittedAt, Bidder bidder)` | `getAmount(): double`; `getSubmittedAt(): LocalDateTime`; `getBidder(): Bidder` | Значення ставки, час подання та учасник |
| `AcquiringService` | `merchantId` | `AcquiringService(String merchantId)` | `charge(Bidder bidder, double amount): void` | Симуляція зовнішньої операції еквайрингу |

## Зв’язки

- `AuctionParticipant` є абстрактним класом. `Seller`, `Bidder` і `Moderator`
  наслідують його та реалізують `ITradingRules`.
- `Seller`, `Bidder` і `Moderator` перевизначають `getRole()` і
  `canPerform(Lot lot): boolean`.
- `ITradingRules` є інтерфейсом лише з методами; він не містить полів і не має
  конструктора.
- `Auction` має асоціацію з одним `Lot`, зберігає `status`, часові межі торгів,
  мінімальний крок ставки, фінальну ціну та переможця.
- Лише `Auction` змінює `status`: `submitForModeration()`, `approve()`,
  `reject()` і `withdraw()` виконують переходи життєвого циклу.
- `Seller.submitForModeration(Auction auction)`, `Seller.withdraw(Auction auction)`,
  `Moderator.approve(Auction auction)` і `Moderator.reject(Auction auction)`
  делегують відповідні зміни стану методам `Auction`.
- `Auction` отримує `AcquiringService` через конструктор і залежить від нього
  під час фінального розрахунку.
- `Auction` створює `Bid` всередині обох перевантажених методів `placeBid(...)`
  і одразу додає його через `lot.addBid(bid)`. `Bidder` не створює ставки
  самостійно; він надає їх через `getSubmittedBids(): List<Bid>`.
- `Auction` містить два перевантажені методи `placeBid(...)`: варіант без
  явного часу використовує поточний час, а варіант із `submittedAt` дає змогу
  моделювати конкретний момент подання ставки.
- `Lot` композиційно містить `0..*` `Bid`; ставка не існує без лоту.
- `Bid` посилається на `Bidder`.
- Статус життєвого циклу зберігається лише в `Auction.status`; `Lot` не має
  власного статусу.

## Правила поведінки та переходи

- `AuctionParticipant.getDisplayName()` є `final`, а `getRole()` — абстрактним
  методом, який реалізують нащадки.
- Ставка приймається лише в стані `Активні торги`, у проміжку
  `startTime`—`endTime`, і лише якщо її сума перевищує поточну допустиму ціну
  щонайменше на `minimumIncrement`.
- Якщо кілька прийнятих ставок мають однакове значення, переможцем стає та,
  що була прийнята раніше.
- `approve()` переводить `status` до `Активні торги`, а `close()` завершує
  торги, викликає `determineWinner()` і фіксує `finalPrice`.
- Якщо ставки прийняті, завершення веде до `Завершено з переможцем`; якщо
  ставок немає — до `Завершено без переможця`.
- Після визначення переможця `Auction` може викликати
  `acquiringService.charge(winner, finalPrice)`. Метод `charge(...)` лише
  виводить повідомлення в консоль і не виконує реального платежу.

## Стани життєвого циклу `Auction`

```text
[*] --> Чернетка : створення лоту
Чернетка --> На модерації : подання на перевірку
На модерації --> Відхилено : відхилення модератором
На модерації --> Активні торги : схвалення модератором через approve()
Відхилено --> На модерації : повторне подання продавцем
Відхилено --> [*] : зняття лоту продавцем
Активні торги --> Завершено з переможцем : завершення часу і наявність ставок
Активні торги --> Завершено без переможця : завершення часу без ставок
Завершено з переможцем --> [*]
Завершено без переможця --> [*]
```

Стан `Чернетка` створюється разом з `Auction`. Метод
`Seller.submitForModeration(Auction auction)` викликає
`Auction.submitForModeration()`, а `Moderator.approve(Auction auction)` —
`Auction.approve()`, що переводить аукціон безпосередньо в `Активні торги`.
`Moderator.reject(Auction auction)` переводить його до `Відхилено`.
`Seller.withdraw(Auction auction)` викликає `Auction.withdraw()` і переводить
відхилений аукціон до кінцевого псевдостану.

Ставки дозволені лише між переходами до `Активні торги` та з нього до одного
із завершальних станів. Усі стани належать `Auction.status`; `Lot` не дублює
цей стан.
