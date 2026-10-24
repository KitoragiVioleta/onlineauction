# Модель даних: UML-діаграми та CRC-картки

## Сутності

| Сутність | Основні поля | Конструктор | Основні методи | Відповідальність |
|---|---|---|---|---|
| `AuctionParticipant` | `participantId`, `displayName` | `AuctionParticipant(String participantId, String displayName)` | `abstract getRole(): String`; `final getDisplayName(): String` | Спільна ідентичність і базова поведінка учасника |
| `Seller` | успадковані `participantId`, `displayName`; `lots` | `Seller(String participantId, String displayName)` | `override getRole(): String`; `override canPerform(Lot lot): boolean`; `createLot(String description, BigDecimal startingPrice): Lot`; `submitForModeration(Auction auction): void`; `withdraw(Auction auction): void` | Створення, подання та зняття лоту |
| `Bidder` | успадковані `participantId`, `displayName`; `submittedBids` | `Bidder(String participantId, String displayName)` | `override getRole(): String`; `override canPerform(Lot lot): boolean`; `getSubmittedBids(): List<Bid>` | Подання ставки за правилами та перегляд власних ставок |
| `Moderator` | успадковані `participantId`, `displayName`; `reviewedAuctions` | `Moderator(String participantId, String displayName)` | `override getRole(): String`; `override canPerform(Lot lot): boolean`; `approve(Auction auction): void`; `reject(Auction auction): void` | Перевірка та схвалення або відхилення аукціону |
| `ITradingRules` | Немає полів | Конструктор не застосовується до інтерфейсу | `canPerform(Lot lot): boolean` | Контракт перевірки можливості виконання операції |
| `IAcquiringService` | Немає полів | Конструктор не застосовується до інтерфейсу | `charge(Bidder bidder, BigDecimal amount): void` | Абстракція операції списання коштів |
| `AuctionStatus` | `label` | `AuctionStatus(String label)` | `getLabel(): String` | Типізований стан життєвого циклу аукціону |
| `Auction` | `auctionId`, `status: AuctionStatus`, `startTime`, `endTime`, `minimumIncrement: BigDecimal`, `lot`, `finalPrice: BigDecimal`, `winner`, `acquiringService: IAcquiringService` | `Auction(String auctionId, Lot lot, LocalDateTime startTime, LocalDateTime endTime, BigDecimal minimumIncrement, IAcquiringService acquiringService)` | `submitForModeration(): void`; `approve(): void`; `reject(): void`; `withdraw(): void`; `placeBid(Bidder bidder, BigDecimal amount): boolean`; `placeBid(Bidder bidder, BigDecimal amount, LocalDateTime submittedAt): boolean`; `close(): void`; `determineWinner(): Bidder` | Керування життєвим циклом, перевірка правил ставок, завершення торгів і визначення результату |
| `Lot` | `lotId`, `description`, `startingPrice: BigDecimal`, `bids` | `Lot(String lotId, String description, BigDecimal startingPrice)` | `addBid(BigDecimal amount, LocalDateTime submittedAt, Bidder bidder): Bid`; `getHighestBid(): Bid`; `getBids(): List<Bid>` | Опис лоту та композиційний власник ставок; сам створює `Bid` |
| `Bid` | `amount: BigDecimal`, `submittedAt`, `bidder` | `Bid(BigDecimal amount, LocalDateTime submittedAt, Bidder bidder)` | `getAmount(): BigDecimal`; `getSubmittedAt(): LocalDateTime`; `getBidder(): Bidder` | Значення прийнятої ставки, час подання та учасник |
| `AcquiringService` | `merchantId` | `AcquiringService(String merchantId)` | `charge(Bidder bidder, BigDecimal amount): void` | Симуляція зовнішньої операції еквайрингу; реалізація `IAcquiringService` |

`AuctionStatus` має такі значення:

| Значення | Українська назва (`label`) |
|---|---|
| `DRAFT` | `Чернетка` |
| `ON_MODERATION` | `На модерації` |
| `REJECTED` | `Відхилено` |
| `ACTIVE` | `Активні торги` |
| `FINISHED_WITH_WINNER` | `Завершено з переможцем` |
| `FINISHED_WITHOUT_WINNER` | `Завершено без переможця` |
| `WITHDRAWN` | `Знято` |

## Зв’язки та кратності

- `AuctionParticipant` є абстрактним класом. `Seller`, `Bidder` і `Moderator`
  наслідують його та реалізують `ITradingRules`.
- `IAcquiringService` є інтерфейсом лише з методом
  `charge(Bidder bidder, BigDecimal amount): void`. `AcquiringService` реалізує
  цей інтерфейс.
- `Auction ..> IAcquiringService` є залежністю від абстракції. `Auction`
  отримує `IAcquiringService` через конструктор і не залежить від конкретного
  `AcquiringService`.
- `Seller "1" --> "0..*" Lot` — продавець створює нуль або більше лотів.
- `Auction "1" --> "1" Lot` — кожні торги проводяться для рівно одного лоту.
- `Auction --> "0..1" Bidder` — аукціон може мати не більше одного переможця.
- `Bidder "1" -- "0..*" Bid` — учасник торгів подає нуль або більше ставок.
- `Lot "1" *-- "0..*" Bid` — лот композиційно володіє ставками; ставка не
  існує без лоту.
- `Moderator "1" --> "0..*" Auction` — модератор перевіряє нуль або більше
  аукціонів, що зберігаються в `reviewedAuctions`.

## Правила відповідальностей і поведінки

- Лише `Auction` змінює `status`: `submitForModeration()`, `approve()`,
  `reject()`, `withdraw()` і `close()` виконують переходи життєвого циклу.
- `Seller.submitForModeration(Auction auction)`, `Seller.withdraw(Auction
  auction)`, `Moderator.approve(Auction auction)` і
  `Moderator.reject(Auction auction)` делегують відповідні зміни стану
  методам `Auction`.
- `Auction.placeBid(...)` перевіряє стан `ACTIVE`, часовий інтервал,
  повноваження `Bidder` і правило мінімального кроку, а потім викликає
  `lot.addBid(amount, submittedAt, bidder)`.
- `Lot.addBid(BigDecimal amount, LocalDateTime submittedAt, Bidder bidder)`
  сам створює `Bid`, додає його до `bids` і повертає створену ставку.
  `Auction` не створює `Bid` напряму.
- Перевантажений `placeBid(Bidder bidder, BigDecimal amount)` використовує
  поточний час, а варіант із `submittedAt` дає змогу моделювати конкретний
  момент подання ставки.
- Ставка приймається лише в стані `ACTIVE`, у проміжку `startTime`—`endTime`,
  і лише якщо її сума не менша за поточну допустиму ціну плюс
  `minimumIncrement`. Відхилена ставка не змінює `Lot.bids`.
- Якщо кілька прийнятих ставок мають однакове значення, переможцем стає та,
  що була прийнята раніше.
- Після завершення часу торгів система ініціює `Auction.close()`, який
  викликає `determineWinner()` і фіксує `finalPrice`. За наявності переможця
  `Auction` може викликати `IAcquiringService.charge(winner, finalPrice)`.
- `AcquiringService.charge(...)` лише симулює списання повідомленням у
  консоль і не виконує реального платежу.
- Усі грошові значення (`startingPrice`, `amount`, `minimumIncrement`,
  `finalPrice` і відповідні параметри методів) мають тип `BigDecimal`.

## Стани життєвого циклу `Auction`

```text
[*] --> DRAFT : створення лоту
DRAFT --> ON_MODERATION : подання на перевірку
ON_MODERATION --> REJECTED : відхилення модератором
ON_MODERATION --> ACTIVE : схвалення модератором
REJECTED --> ON_MODERATION : повторне подання продавцем
DRAFT --> WITHDRAWN : зняття продавцем
ON_MODERATION --> WITHDRAWN : зняття продавцем
REJECTED --> WITHDRAWN : зняття продавцем
ACTIVE --> FINISHED_WITH_WINNER : завершення часу і наявність ставок
ACTIVE --> FINISHED_WITHOUT_WINNER : завершення часу без ставок
WITHDRAWN --> [*] : завершення життєвого циклу знятого лоту
FINISHED_WITH_WINNER --> [*]
FINISHED_WITHOUT_WINNER --> [*]
```

Зняття продавцем дозволене зі станів `DRAFT`, `ON_MODERATION` і `REJECTED`;
воно переводить аукціон у `WITHDRAWN` (`Знято`). Із `WITHDRAWN` аукціон
переходить безпосередньо у кінцевий псевдостан. Ставки дозволені лише між
переходом до `ACTIVE` та завершенням торгів.

## Правила для діаграм і CRC-карток

- `Auction` створює система в точці складання застосунку конструктором
  `Auction(auctionId, lot, startTime, endTime, minimumIncrement,
  acquiringService)`. Аргумент `acquiringService` має тип `IAcquiringService`;
  конкретний `AcquiringService` передається як реалізація цієї абстракції.
- На діаграмі послідовності створення аукціону показується як
  `create participant Auction`, а поруч додається `Note` про впровадження
  `IAcquiringService` через конструктор (DI).
- На діаграмі послідовності створення лоту показується повідомлення
  `Seller->>Lot: Lot(lotId, description, startingPrice)`. Усередині
  `addBid(...)` створення ставки показується як
  `Lot->>Bid: Bid(amount, submittedAt, bidder)`.
- Перед фіксацією `finalPrice` на діаграмі послідовності
  `Auction->>Lot: getHighestBid()`. Перед викликом `close()` додається
  `Note` «після завершення часу торгів».
- Сповіщення про зміну статусу на діаграмі послідовності показуються через
  `Note`, а не пунктирною стрілкою-відповіддю.
- На діаграмі станів має бути петля `ACTIVE --> ACTIVE` з умовою
  `[ставка в межах часу і ≥ поточна ціна + minimumIncrement]`.
- У CRC-картках і матриці співпрацівниками вважаються лише об'єкти, з якими
  клас взаємодіє для виконання відповідальності. Суперкласи, підкласи та
  реалізовані інтерфейси не вказуються як співпрацівники, оскільки
  наслідування й реалізація не є співпрацею.
- Для CRC-карток і матриці використовуються такі уточнення співпрацівників:
  `AuctionParticipant` — `— (базовий клас для ролей)`;
  `ITradingRules` — `Lot`; `IAcquiringService` — `Bidder`;
  `AcquiringService` — `Bidder`; `AuctionStatus` — `Auction`.
  У `Seller` співпрацівником не є `Moderator`.
- Обов'язки в CRC-картках формулюються як дії класу, а не як обмеження.
  Для `AcquiringService` використовуються обов'язки «Симулювати списання
  коштів» і «Повідомляти про результат списання».

## Матриця узгодженості

| Сутність | Роль у сценарії | Ключові операції | Стани/результати | Співпрацівники |
|---|---|---|---|---|
| `AuctionParticipant` | Ідентифікувати роль і надати ім'я учасника | `getRole()`, `getDisplayName()` | Не керує станом | — (базовий клас для ролей) |
| `Seller` | Створювати лот; подавати його на модерацію; знімати лот | `createLot()`, `submitForModeration()`, `withdraw()` | `DRAFT`, `ON_MODERATION`, `REJECTED`, `WITHDRAWN` | `Lot`, `Auction` |
| `Bidder` | Подавати ставки; переглядати власні ставки | `getSubmittedBids()` | Ставка лише в `ACTIVE` | `Auction`, `Bid` |
| `Moderator` | Перевіряти аукціон; схвалювати або відхиляти його | `approve()`, `reject()` | `ON_MODERATION` → `ACTIVE` або `REJECTED` | `Auction` |
| `ITradingRules` | Перевіряти можливість виконання операції | `canPerform()` | Дозвіл або відмова | `Lot` |
| `IAcquiringService` | Приймати запит на списання коштів | `charge()` | Симульоване списання після перемоги | `Bidder` |
| `AuctionStatus` | Повертати українську назву стану | `getLabel()` | Сім визначених станів | `Auction` |
| `Auction` | Створюватися системою через конструктор у точці складання; керувати життєвим циклом; перевіряти ставки; завершувати торги; визначати переможця | `Auction(auctionId, lot, startTime, endTime, minimumIncrement, acquiringService)`, `submitForModeration()`, `approve()`, `placeBid()`, `close()` | Усі стани `AuctionStatus` | `Lot`, `Bidder`, `IAcquiringService` |
| `Lot` | Зберігати опис; створювати й зберігати ставки; повертати найвищу ставку | `addBid()`, `getHighestBid()` | Існує в межах `Auction` | `Bid`, `Bidder` |
| `Bid` | Зберігати суму, час подання та учасника ставки | `getAmount()`, `getSubmittedAt()` | Не існує без `Lot` | `Bidder`, `Lot` |
| `AcquiringService` | Симулювати списання коштів; повідомляти про результат списання | `charge()` | Лише після перемоги | `Bidder` |

## Правила узгодження динамічних артефактів

- Прийняття ставки перевіряє стан `ACTIVE`, часовий інтервал і мінімальний
  крок; після успішної перевірки саме `Lot.addBid(...)` створює `Bid`.
- За однакової суми перемагає ставка з найранішим `submittedAt`.
- Варіант використання «Визначити переможця» ініціює система після завершення
  часу торгів. `Seller` і `Bidder` лише отримують повідомлення про результат.
- `close()` встановлює `finalPrice` і переводить аукціон у стан із переможцем
  або без нього; для переможця залежність `IAcquiringService` може виконати
  симульоване списання.
