---

description: "План реалізації імітаційного прототипу онлайн-аукціону"
---

# Tasks: Імітаційний прототип онлайн-аукціону

**Input**: Design documents from `specs/002-online-auction-prototype/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`,
`contracts/console-demo.md`, `quickstart.md`

**Tests**: Окремий тестовий фреймворк не передбачено. Перевірка виконується
детермінованим запуском `Main` і перевіркою stdout згідно з контрактом.

**Organization**: Tasks are grouped by user story to enable independent
implementation and validation of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Підготувати мінімальну структуру standalone Java-проєкту без
зовнішніх залежностей.

- [ ] T001 Створити каталог `src/onlineauction` і перевірити структуру `docs` згідно з `specs/002-online-auction-prototype/plan.md`
- [ ] T002 [P] Підготувати кореневий `README.md` з призначенням прототипу, межами симуляції та розділами команд Windows PowerShell і Linux/macOS bash
- [ ] T003 [P] Перевірити наявні артефакти `docs/usecase.puml`, `docs/class.mmd`, `docs/sequence.mmd`, `docs/state.mmd` і `docs/crc.xlsx` як джерело синхронізації доменної моделі

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Створити спільні доменні типи та зафіксувати інваріанти, від яких
залежать усі користувацькі історії.

**Checkpoint**: Після цієї фази спільна модель, правила станів і залежність
еквайрингу готові для реалізації історій у пріоритетному порядку.

- [ ] T004 [P] Створити `src/onlineauction/AuctionParticipant.java` з англійськими ідентифікаторами, ідентифікатором учасника та відображуваним ім'ям
- [ ] T005 [P] Створити `src/onlineauction/AuctionStatus.java` із сімома станами та українськими відображуваними мітками
- [ ] T006 [P] Створити `src/onlineauction/ITradingRules.java` з контрактом перевірки дозволу операції
- [ ] T007 [P] Створити `src/onlineauction/IAcquiringService.java` з контрактом симульованої оплати
- [ ] T008 Створити `src/onlineauction/Bid.java` як незмінну модель суми, `submittedAt` і учасника ставки
- [ ] T009 Створити `src/onlineauction/Lot.java` з початковою ціною, прийнятими ставками та захистом колекції від зовнішньої мутації
- [ ] T010 Створити `src/onlineauction/Auction.java` з полями життєвого циклу, явною залежністю `IAcquiringService` через конструктор і незворотними кінцевими станами
- [ ] T011 [P] Створити `src/onlineauction/Seller.java`, `src/onlineauction/Bidder.java` і `src/onlineauction/Moderator.java` як ролі доменної моделі
- [ ] T012 [P] Створити `src/onlineauction/AcquiringService.java` з повідомленням замість реального фінансового списання

---

## Phase 3: User Story 1 - Проведення торгів від створення до результату (Priority: P1) 🎯 MVP

**Goal**: Реалізувати повний успішний цикл від створення лоту до завершення з
переможцем, підсумковою ціною і симульованою оплатою.

**Independent Test**: Один запуск `onlineauction.Main` без stdin показує створення,
перехід `Чернетка -> На модерації -> Активні торги`, прийняття допустимих ставок,
завершення з переможцем, підсумкову ціну та симульовану оплату не більш ніж за
10 секунд.

### Implementation for User Story 1

- [ ] T013 [US1] Реалізувати створення лоту та аукціону в `src/onlineauction/Seller.java` і `src/onlineauction/Auction.java` з перевіркою ідентифікатора, опису, ціни та часових меж
- [ ] T014 [US1] Реалізувати подання лоту на модерацію в `src/onlineauction/Seller.java` і перехід `DRAFT -> ON_MODERATION` у `src/onlineauction/Auction.java`
- [ ] T015 [US1] Реалізувати схвалення модератором і негайний перехід `ON_MODERATION -> ACTIVE` у `src/onlineauction/Moderator.java` та `src/onlineauction/Auction.java`
- [ ] T016 [US1] Реалізувати перевірку ставки в `src/onlineauction/Auction.java` у порядку стан, час, сума з включними межами `startTime ≤ submittedAt ≤ endTime`
- [ ] T017 [US1] Реалізувати правило першої ставки не нижче `startingPrice` і наступних ставок не нижче поточної найвищої ставки плюс `minimumIncrement` у `src/onlineauction/Auction.java`
- [ ] T018 [US1] Реалізувати додавання прийнятої ставки до `src/onlineauction/Lot.java` та збереження порядку додавання
- [ ] T019 [US1] Реалізувати визначення переможця в `src/onlineauction/Auction.java` за сумою, ранішим `submittedAt` і ранішим порядком додавання
- [ ] T020 [US1] Реалізувати завершення аукціону, фіксацію `finalPrice` і виклик `IAcquiringService` у `src/onlineauction/Auction.java`
- [ ] T021 [US1] Додати основний детермінований сценарій без ручного введення в `src/onlineauction/Main.java`

**Checkpoint**: Основний сценарій P1 самостійно демонструє успішний життєвий
цикл і всі його обов'язкові результати.

---

## Phase 4: User Story 2 - Перевірка та відхилення лоту (Priority: P2)

**Goal**: Реалізувати відхилення лоту з поясненням продавцю та повторне подання
на модерацію.

**Independent Test**: Демонстраційний сценарій у `src/onlineauction/Main.java`
показує `Відхилено`, українське пояснення і перехід
`Відхилено -> На модерації` після повторного подання.

### Implementation for User Story 2

- [ ] T022 [P] [US2] Реалізувати відхилення аукціону з причиною в `src/onlineauction/Moderator.java` і переходом `ON_MODERATION -> REJECTED` у `src/onlineauction/Auction.java`
- [ ] T023 [US2] Реалізувати повторне подання відхиленого лоту в `src/onlineauction/Seller.java` з переходом `REJECTED -> ON_MODERATION`
- [ ] T024 [US2] Реалізувати повідомлення про відмову в `src/onlineauction/Auction.java` і `src/onlineauction/Moderator.java`, яке називає операцію, поточний стан і першу причину відмови
- [ ] T025 [US2] Додати сценарій відхилення та повторного подання в `src/onlineauction/Main.java` без зміни результатів основного сценарію

**Checkpoint**: Історія P2 незалежно демонструє модерацію з відхиленням і
повторним поданням.

---

## Phase 5: User Story 3 - Обробка альтернативних завершень (Priority: P3)

**Goal**: Реалізувати зняття лоту з дозволених станів, заборону зняття активного
аукціону та завершення без переможця.

**Independent Test**: Демонстраційний запуск показує зняття з дозволеного стану,
відмову у знятті активного лоту та стан `Завершено без переможця`.

### Implementation for User Story 3

- [ ] T026 [US3] Реалізувати зняття лоту з `DRAFT`, `ON_MODERATION` і `REJECTED` у `src/onlineauction/Seller.java` та `src/onlineauction/Auction.java`
- [ ] T027 [US3] Заборонити зняття з `ACTIVE` і всіх кінцевих станів у `src/onlineauction/Auction.java` без зміни поточного стану
- [ ] T028 [US3] Реалізувати завершення активного аукціону без ставок із переходом `ACTIVE -> FINISHED_WITHOUT_WINNER` у `src/onlineauction/Auction.java`
- [ ] T029 [US3] Заборонити нові ставки після `close()` і після зняття в `src/onlineauction/Auction.java` з повідомленням про стан і причину
- [ ] T030 [US3] Додати сценарії зняття, невдалого зняття активного аукціону та завершення без ставок у `src/onlineauction/Main.java`

**Checkpoint**: Усі альтернативні завершення P3 незалежно демонструються і не
порушують незворотність кінцевих станів.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Узгодити документацію, команди запуску і всі артефакти з остаточною
реалізацією та перевірити quickstart.

- [ ] T031 [P] Оновити `README.md` точними командами компіляції та запуску для Windows PowerShell і Linux/macOS bash, межами симуляції та очікуваними результатами
- [ ] T032 [P] Узгодити `docs/usecase.puml` з ролями, варіантами використання та сценаріями всіх трьох історій
- [ ] T033 [P] Узгодити `docs/class.mmd` і `docs/crc.xlsx` з фактичними класами, полями, методами, відповідальностями та взаємодіями
- [ ] T034 [P] Узгодити `docs/sequence.mmd` з викликами основного й альтернативних сценаріїв та повідомленнями про відмови
- [ ] T035 [P] Узгодити `docs/state.mmd` із сімома станами, дозволеними переходами та незворотністю кінцевих станів
- [ ] T036 Оновити `specs/002-online-auction-prototype/quickstart.md` командами PowerShell і bash та перевірками включних часових меж, порядку валідації і tie-break правил
- [ ] T037 Перевірити компіляцію `javac --release 17`, запуск `java -cp build onlineauction.Main` і завершення з кодом 0 за `specs/002-online-auction-prototype/quickstart.md`
- [ ] T038 Повторити запуск і звірити детермінований stdout із `specs/002-online-auction-prototype/contracts/console-demo.md`, включно з українськими повідомленнями про відмови

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: без залежностей; T002 і T003 можуть виконуватися паралельно.
- **Foundational (Phase 2)**: залежить від Phase 1; незалежні типи T004–T007,
  T011 і T012 можуть готуватися паралельно, а T008–T010 залежать від відповідних
  контрактів.
- **User Story 1 (Phase 3)**: залежить від Phase 2 і є базою для інтегрованого
  демонстраційного запуску.
- **User Story 2 (Phase 4)**: залежить від Phase 2; інтеграція сценарію в
  `Main.java` використовує спільну модель і може виконуватися після P1 для
  послідовного демо.
- **User Story 3 (Phase 5)**: залежить від Phase 2; інтеграція сценарію в
  `Main.java` використовує спільну модель і може виконуватися після P1.
- **Polish (Phase 6)**: залежить від завершення потрібних користувацьких
  історій; документаційні T032–T035 можуть виконуватися паралельно.

### User Story Dependencies

- **US1 (P1)**: після Foundational; незалежна MVP-історія.
- **US2 (P2)**: після Foundational; логічно використовує ті самі переходи
  `Auction`, але має незалежний сценарій відхилення.
- **US3 (P3)**: після Foundational; логічно використовує `Auction`, але має
  незалежні сценарії альтернативних завершень.

### Parallel Opportunities

- Після Setup можна паралельно підготувати README і перевірити документаційні
  артефакти.
- У Foundational паралельні T004–T007, T011 і T012 не змінюють спільні файли.
- Після Foundational US1, US2 і US3 можна розподілити між виконавцями, якщо
  інтеграцію `Main.java` виконувати окремими узгодженими кроками.
- У Phase 6 T031–T035 стосуються різних файлів і можуть виконуватися паралельно.

## Parallel Example: User Story 1

```text
Task: "Створити src/onlineauction/Bid.java"
Task: "Створити src/onlineauction/Lot.java"
Task: "Створити src/onlineauction/AcquiringService.java"
```

Після завершення спільних типів ці задачі можуть виконуватися паралельно;
логіка `Auction.java` і сценарій `Main.java` виконуються після їхніх залежностей.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Виконати Phase 1 Setup.
2. Виконати Phase 2 Foundational.
3. Реалізувати Phase 3 User Story 1.
4. Перевірити незалежний запуск P1 за stdout-контрактом.
5. Зупинитися на MVP або перейти до альтернативних історій.

### Incremental Delivery

1. Додати US2 і перевірити відхилення та повторне подання.
2. Додати US3 і перевірити зняття та завершення без переможця.
3. Виконати Phase 6 для синхронізації README, UML, CRC, quickstart і контракту.

## Notes

- `[P]` означає різні файли без залежності від незавершених задач.
- `[US1]`, `[US2]`, `[US3]` забезпечують трасованість до користувацьких історій.
- Тестові задачі окремого фреймворку не додано, оскільки специфікація визначає
  детермінований інтеграційний запуск і не вимагає TDD.
- Усі задачі містять конкретний шлях до файлу або каталогу.
