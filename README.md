# Імітаційний прототип онлайн-аукціону

Standalone CLI-прототип на Java 17 демонструє життєвий цикл лоту: створення,
модерацію, активні торги, прийняті й відхилені ставки, завершення з переможцем,
відхилення з повторним поданням, зняття та завершення без переможця.

Дані зберігаються лише в пам’яті одного запуску. Проєкт не використовує БД,
мережу, зовнішні сервіси, ручне введення або сторонні бібліотеки. Оплата є
симуляцією та лише виводить повідомлення.

## Windows PowerShell

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Remove-Item -Recurse -Force build -ErrorAction SilentlyContinue
New-Item -ItemType Directory build | Out-Null
javac -encoding UTF-8 --release 17 -d build (Get-ChildItem -Recurse src -Filter *.java).FullName
java '-Dstdout.encoding=UTF-8' -cp build onlineauction.Main
```

## Linux/macOS bash

```bash
rm -rf build
mkdir -p build
javac -encoding UTF-8 --release 17 -d build $(find src -name '*.java')
java -Dstdout.encoding=UTF-8 -cp build onlineauction.Main
```

Запуск не читає stdin, є детермінованим і має завершуватися з кодом `0` не
більш ніж за 10 секунд. Українські повідомлення stdout є основним контрактом
перевірки.
