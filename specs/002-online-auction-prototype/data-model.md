# Модель даних: Імітаційний прототип онлайн-аукціону

## Сутності та відповідальності

| Сутність | Основні поля | Ключові операції | Відповідальність |
|---|---|---|---|
| `AuctionParticipant` | `participantId`, `displayName` | `getRole()`, `getDisplayName()` | Спільна ідентичність ролей |
| `Seller` | успадковані поля, `lots` | `createLot()`, `submitForModeration()`, `withdraw()` | Створення, подання і зняття лоту |
| `Bidder` | успадковані поля, `submittedBids` | `getSubmittedBids()` | Подання ставок і роль переможця |
| `Moderator` | успадковані поля, `reviewedAuctions` | `approve()`, `reject()` | Перевірка аукціону |
| `ITradingRules` | немає | `canPerform(Lot)` | Контракт дозволу операції |
| `IAcquiringService` | немає | `charge(Bidder, BigDecimal)` | Контракт симульованої оплати |
| `AuctionStatus` | `label` | `getLabel()` | Сім дозволених станів |
| `Auction` | `auctionId`, `lot`, `startTime`, `endTime`, `minimumIncrement`, `status`, `winner`, `finalPrice`, `acquiringService` | переходи станів, `placeBid()`, `close()`, `determineWinner()` | Життєвий цикл і правила торгів |
| `Lot` | `lotId`, `description`, `startingPrice`, `bids` | `addBid()`, `getHighestBid()` | Опис лоту та композиційне зберігання ставок |
| `Bid` | `amount`, `submittedAt`, `bidder` | getters | Незмінне значення прийнятої ставки |
| `AcquiringService` | `merchantId` | `charge()` | Виведення повідомлення замість реального списання |

Усі грошові поля та параметри мають тип `BigDecimal`; колекції не повертаються
для зовнішньої мутації. Залежність `IAcquiringService` передається в
конструктор `Auction`.

## Стани та переходи

| Стан | Дозволені переходи |
|---|---|
| `DRAFT` / Чернетка | `ON_MODERATION`, `WITHDRAWN` |
| `ON_MODERATION` / На модерації | `ACTIVE`, `REJECTED`, `WITHDRAWN` |
| `REJECTED` / Відхилено | `ON_MODERATION`, `WITHDRAWN` |
| `ACTIVE` / Активні торги | `FINISHED_WITH_WINNER`, `FINISHED_WITHOUT_WINNER` |
| `FINISHED_WITH_WINNER` / Завершено з переможцем | кінцевий стан |
| `FINISHED_WITHOUT_WINNER` / Завершено без переможця | кінцевий стан |
| `WITHDRAWN` / Знято | кінцевий стан |

Лише `Auction` змінює `status`. Схвалення одразу переводить
`ON_MODERATION -> ACTIVE`; окремого запуску торгів немає. Зняття дозволене
лише з `DRAFT`, `ON_MODERATION` і `REJECTED`.

## Правила ставок

1. Ставка приймається лише в `ACTIVE`.
2. `submittedAt` має бути не раніше `startTime` і не пізніше `endTime`.
3. Допустима сума не менша за стартову/поточну ціну плюс `minimumIncrement`.
4. Відхилена ставка не додається до `Lot.bids` і пояснюється українською.
5. Після `close()` нові ставки не приймаються.
6. Переможець — ставка з найбільшою сумою; за рівності — з найранішим
   часом прийняття.
7. Без ставок `close()` встановлює `FINISHED_WITHOUT_WINNER`; з переможцем
   фіксує `finalPrice` і викликає `IAcquiringService.charge(...)`.

## Зв'язки

- `Seller` створює `0..*` `Lot`; `Auction` керує одним `Lot`.
- `Lot` композиційно володіє `0..*` `Bid`.
- `Bid` посилається на одного `Bidder`; `Auction` має `0..1` переможця.
- `Moderator` взаємодіє з `Auction`; `Auction` залежить від
  `IAcquiringService`.
- `Seller`, `Bidder` і `Moderator` успадковують `AuctionParticipant` та
  реалізують `ITradingRules`.

## Інваріанти та повідомлення

- Ідентифікатори, опис, ціни, часові межі та мінімальний крок перевіряються
  під час створення об'єктів.
- Несумісна операція не змінює стан і виводить причину українською.
- Успішні переходи, прийняття/відхилення ставок, результат торгів і
  симульована оплата мають бути видимі в stdout.
