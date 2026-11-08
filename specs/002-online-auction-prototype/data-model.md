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
| `FINISHED_WITH_WINNER` / Завершено з переможцем | кінцевий, незворотний стан |
| `FINISHED_WITHOUT_WINNER` / Завершено без переможця | кінцевий, незворотний стан |
| `WITHDRAWN` / Знято | кінцевий, незворотний стан |

Лише `Auction` змінює `status`. Схвалення одразу переводить
`ON_MODERATION -> ACTIVE`; окремого запуску торгів немає. Зняття дозволене
лише з `DRAFT`, `ON_MODERATION` і `REJECTED`.

## Правила ставок

1. Умови перевіряються саме в такому порядку: стан аукціону, часові межі,
   сума. Відмова повідомляє першу порушену умову.
2. Ставка приймається лише в `ACTIVE`.
3. `submittedAt` має відповідати включній умові
   `startTime ≤ submittedAt ≤ endTime`.
4. Перша ставка має бути не меншою за `startingPrice`.
5. Кожна наступна ставка має бути не меншою за поточну найвищу прийняту
   ставку плюс `minimumIncrement`.
6. Відхилена ставка не додається до `Lot.bids`. Повідомлення українською
   називає операцію, поточний стан і першу порушену умову.
7. Після `close()` нові ставки не приймаються.
8. Переможець — ставка з найбільшою сумою; за однакової суми перемагає ставка
   з ранішим `submittedAt`, а за однакового часу — раніше додана до списку.
9. Без ставок `close()` встановлює `FINISHED_WITHOUT_WINNER`; з переможцем
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
- Повідомлення про кожну відмову називає операцію, поточний стан і першу
  порушену умову.
- Успішні переходи, прийняття/відхилення ставок, результат торгів і
  симульована оплата мають бути видимі в stdout.
- Відновлення після переходу в кінцевий стан не передбачено.
