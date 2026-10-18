# Перевірка артефактів

## Передумови

Потрібні локальні Java/PlantUML CLI, Node.js із `@mermaid-js/mermaid-cli` та
Python із `openpyxl`. Перевірка не використовує мережу, базу даних або реальні
зовнішні сервіси.

## Сценарій

1. Переконатися, що створені `docs/usecase.puml`, `docs/class.mmd`,
   `docs/sequence.mmd`, `docs/state.mmd` і `docs/crc.xlsx`.
2. Перевірити PlantUML:
   `java -jar plantuml.jar -checkonly docs/usecase.puml`.
3. Відрендерити Mermaid:
   `npx @mermaid-js/mermaid-cli -i docs/class.mmd -o class.svg`,
   аналогічно для `sequence.mmd` і `state.mmd`. Команди мають завершитися без
   помилок і створити SVG.
4. Перевірити XLSX через `openpyxl`: файл відкривається, має 9 аркушів, кожен
   містить заголовок, `обов’язки класу`, `з ким співпрацює`, щонайменше одну
   відповідальність і одного співпрацівника.
5. Провести змістову звірку: класи й ролі збігаються між діаграмами та CRC;
   послідовність використовує стани й операції, описані в `state.mmd` і
   `usecase.puml`.

## Очікуваний результат

PlantUML і Mermaid рендеряться без синтаксичних помилок, XLSX відкривається без
відновлення, а артефакти покривають SC-001—SC-008.
