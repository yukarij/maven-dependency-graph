# Практическая работа №2, вариант 26

Визуализатор графа зависимостей Maven на Java 17+, только стандартная
библиотека JDK. POM скачиваются и разбираются самостоятельно. Приложение
не использует Maven и сторонние библиотеки для получения зависимостей.
Все пять этапов отражены в отдельных коммитах.

## Сборка и тесты

Нужен JDK 17+, команды java и javac должны быть доступны в PATH.
На Windows при необходимости задайте JAVA_HOME на каталог своего JDK.

```bat
run.bat examples/test.csv
test.bat
```

Linux/macOS: `sh run.sh examples/test.csv`, тесты: `sh test.sh`.
Скрипты компилируют Java-код с `--release 17 -encoding UTF-8` в build,
затем запускают программу или тесты. Отдельной установки зависимостей нет.
Тесты написаны на Java, выполняются с `-ea`, не требуют JUnit и внешней сети.
HTTP-тесты используют локальный сервер на автоматически выбранном порту.
Для сетевого режима нужен доступ к HTTP(S) репозиторию; Maven не нужен.

## Настройки CSV

Первый аргумент - путь к CSV. CSV без заголовка содержит ровно четыре
уникальных непустых ключа. В каждой строке один ключ и одно значение.
Пустые строки разрешены. Значение с запятой заключите в двойные кавычки.
Внутри него кавычка записывается как две кавычки. Многострочных значений нет.

| Ключ | Значение |
| --- | --- |
| package | A-Z в тестовом режиме, иначе groupId:artifactId:version с фиксированной версией |
| repository | Путь к CSV-графу либо базовый HTTP(S) URL репозитория Maven |
| test_mode | Строго true или false |
| ascii_tree | Строго true или false; включает ASCII-дерево на этапе 5 |

```csv
package,A
repository,repository.csv
test_mode,true
ascii_tree,true
```

Относительный путь к графу отсчитывается от конфигурационного CSV.
Проверяются имя, URL, доступность файла, флаги, неизвестные и повторные ключи.
Неизвестный пакет, HTTP-ошибка, плохой XML, неразрешённые версии и свойства
не заменяются пустым графом: выводится `Error: ...` в stderr, exit code 1.
Успех возвращает 0. Для демонстрации ошибок измените в копии CSV по одному:
package на lower, repository на отсутствующий файл, test_mode на yes,
ascii_tree на 1. Эти случаи также проверяет ConfigTests.

## Пять этапов и примеры

Необязательный второй аргумент выбирает этап 1-5; по умолчанию это этап 5.
Он выбирает операцию; все настройки анализа читаются только из CSV.
Промежуточные выводы этапов 1, 2 и 4 не добавляются в обычный финальный вывод.

| Команда Windows | Результат |
| --- | --- |
| run.bat examples/test.csv 1 | Четыре параметра ключ=значение |
| run.bat examples/junit.csv 2 | Прямые зависимости из настоящего POM |
| run.bat examples/test.csv 3 | Транзитивный граф, список смежности |
| run.bat examples/reverse.csv 4 | Обратные зависимости D: B, A, E, C |
| run.bat examples/test.csv | PlantUML и ASCII тестового графа |
| run.bat examples/junit.csv | Визуализация junit:junit:4.13.2 |
| run.bat examples/slf4j.csv | Визуализация org.slf4j:slf4j-simple:2.0.16 |
| run.bat examples/hamcrest.csv | Визуализация org.hamcrest:hamcrest:2.2 |

На Linux/macOS замените run.bat на sh run.sh.

## Граф, DFS и обратные зависимости

Тестовый репозиторий - CSV с заголовком `package,dependencies`.
Строка `A,B C` задаёт два ребра, строка `D,` задаёт лист.
Зависимости разделяются пробелами, имена состоят из A-Z.
Все вершины, включая листья, должны быть объявлены. Дубликаты запрещены.
В examples/repository.csv есть ромб A-B/C-D, E -> A, самоссылка F,
цикл G-H и изолированный I. Выбирайте их через package в копии test.csv.

Graph.traverse использует стек ArrayDeque и таблицу обработанных вершин.
Рекурсии нет. Порядок DFS для A: A, B, D, C. Вершина загружается один раз,
общие и циклические рёбра сохраняются. Сложность обхода O(V + E).
Тест проверяет цепочку из 20000 вершин без переполнения стека Java.

Обратный поиск переворачивает рёбра и вызывает тот же DFS.
В тестовом режиме анализируется весь файл, поэтому находятся внешние
родители: для A ответ E, для D - B, A, E, C, для G - H, для F и I - пусто.
Целевой пакет исключается из ответа. В сети известен лишь загруженный граф:
по одному POM нельзя найти всех потребителей пакета во всём Maven Central.

## PlantUML и ASCII

В stdout выводится описание PlantUML от @startuml до @enduml.
Стрелка направлена от пакета к зависимости. Текст можно открыть в
совместимом рендерере; приложению сам PlantUML не нужен.
Ничего не отправляется внешнему сервису визуализации.
При ascii_tree=true после диаграммы печатается дерево:

```text
A
+-- B
|   \-- D
\-- C
    \-- D [shared]
```

`[cycle]` - возврат в активный путь, `[shared]` - вершина, развёрнутая
в другой ветке. Вывод конечный; представление PlantUML сохраняет все рёбра.
Примеры трёх настоящих пакетов сохранены в docs/comparison/*-ours.txt.

## Поддержка Maven и сравнение

Поддерживаются POM с namespace и без него, фиксированные версии, parent,
properties, dependencyManagement, BOM imports. Модели кешируются в памяти
одного запуска. Родители и BOM разрешаются явным стеком. DTD запрещены.
В граф входят неoptional compile/runtime-зависимости; test и plugins исключены.

Это учебный граф объявленных зависимостей, а не полный Maven Resolver:
не реализованы profiles, exclusions, nearest-wins, метаданные SNAPSHOT,
диапазоны версий и разрешение по classifier. Полные границы, включая
управление транзитивными версиями и scopes, описаны в сравнении.

[Сравнение с Maven для трёх пакетов](docs/COMPARISON.md): вершины и рёбра
на сохранённых примерах совпали. Maven запущен только как внешний эталон.

## Оформление и защита

- [Соответствие требованиям](docs/REQUIREMENTS.md).
- [Проверка репозитория](docs/VERIFICATION.md).
- [Подготовка к защите](docs/DEFENSE.md).

Проверка Java: скачайте checkstyle-10.21.4-all.jar за пределы репозитория
из официальных релизов Checkstyle и выполните:

```text
java -jar PATH_TO_CHECKSTYLE.jar -c checkstyle.xml src tests
```

Проверяются методы до 40 строк, строки до 120 символов, до 7 параметров,
сложность до 10, Javadoc и имена. Сборка, архивы, классы, JAR, кеши
и редакторские настройки в Git не добавляются.
Перед сдачей разместите всю историю в публичном репозитории,
проверьте доступ по ссылке без авторизации и повторите test.bat.

## Каталог функций

Назначение функций перечислено ниже; Javadoc также находится в исходниках.
Конструкторы создают объекты, закрытые конструкторы запрещают создание
utility-классов. Records предоставляют стандартные аксессоры полей.

| Класс | Функция | Назначение |
| --- | --- | --- |
| Config | load | Loads and validates every required setting. |
| Config | flag | Accepts only explicit true and false values. |
| Config | validatePackage | Checks uppercase fixture names or fixed Maven coordinates. |
| Config | validateRepository | Resolves fixture paths relative to the CSV and checks HTTP URLs. |
| Coordinate | parse | Validates and splits a coordinate. |
| Coordinate | pomPath | Returns the relative POM path defined by the Maven repository layout. |
| Coordinate | toString | Returns canonical Maven coordinates. |
| Csv | Csv | Prevents utility construction. |
| Csv | parse | Splits a record, accepting doubled quotes inside quoted fields. |
| Dependency | read | Reads a declaration and expands its property references. |
| Dependency | value | Resolves one XML field through the effective property map. |
| Dependency | key | Returns the management key, including Maven dependency type. |
| Dependency | isImport | Identifies imported dependency-management BOMs. |
| Dependency | included | Selects nonoptional compile/runtime dependencies for the graph. |
| Dependency | managed | Supplies omitted fields from dependencyManagement. |
| Dependency | fallback | Chooses the explicit value before its managed default. |
| Dependency | coordinate | Requires a resolved, fixed Maven version. |
| Graph | traverse | Builds the reachable subgraph using DFS with an explicit stack. |
| Graph | adjacency | Prints a deterministic adjacency list for the third development stage. |
| Graph | reversed | Reverses each edge while preserving vertices without incoming edges. |
| Graph | dependents | Finds direct and transitive dependents with the same iterative DFS. |
| Main | Main | Prevents utility construction. |
| Main | main | Reports configuration and input errors without a stack trace. |
| Main | execute | Runs the selected development stage; settings always come from CSV. |
| MavenRepository | MavenRepository | Stores the repository base URL. |
| MavenRepository | dependencies | Loads effective direct compile/runtime dependencies. |
| MavenRepository | resolve | Resolves parent and BOM models with an explicit stack, without recursion. |
| MavenRepository | schedule | Schedules one missing prerequisite and detects invalid model cycles. |
| MavenRepository | parent | Reads a parent's fixed coordinates when present. |
| MavenRepository | document | Downloads each POM once and rejects HTTP and malformed XML errors. |
| PomModel | read | Builds a model after its parent has been resolved. |
| PomModel | addProperties | Copies direct XML property elements. |
| PomModel | readDependencies | Reads only declarations directly inside the supplied dependencies element. |
| PomModel | expand | Expands nested properties, rejecting unresolved or cyclic references. |
| PomModel | imports | Lists imported BOM coordinates in declaration order. |
| PomModel | withImports | Merges BOM defaults without overriding explicit local management. |
| PomModel | direct | Resolves and filters the direct graph edges of this model. |
| Render | Render | Prevents utility construction. |
| Render | plantUml | Emits every vertex and edge, including isolated vertices and cycles. |
| Render | ascii | Produces a finite, deterministic tree without recursive calls. |
| Render | appendNode | Annotates back edges and vertices that were expanded on another branch. |
| Render | pushChildren | Pushes child tasks in reverse order so their output keeps declaration order. |
| Stages | Stages | Prevents utility construction. |
| Stages | run | Executes a demonstration stage using the CSV settings. |
| Stages | printConfig | Prints all four settings for stage one only. |
| TestRepository | TestRepository | Loads the complete test universe and rejects dangling dependencies. |
| TestRepository | addRow | Parses a package row whose dependencies are separated by spaces. |
| TestRepository | requirePackage | Rejects packages that are absent from the fixture. |
| TestRepository | dependencies | Returns the direct edges for an existing package. |
| Xml | error | Rejects malformed XML. |
| Xml | fatalError | Rejects fatal XML errors. |
| Xml | Xml | Prevents utility construction. |
| Xml | parse | Parses XML with DTDs and external resources disabled. |
| Xml | children | Returns direct element children, never nested plugin dependencies. |
| Xml | child | Returns an optional direct child. |
| Xml | text | Returns trimmed text or an empty string for an absent child. |
| Config | readValues | Читает уникальные непустые пары ключ-значение. |
| Repository | dependencies | Контракт получения прямых зависимостей без менеджера пакетов. |
| TestRepository | all | Возвращает весь конечный тестовый репозиторий. |
