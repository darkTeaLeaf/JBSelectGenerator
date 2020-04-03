# Task description
На Java или Kotlin вам нужно написать генератор SELECT запросов, который будет помогать находить данные в базе.

На вход генератор получает yaml файл со структурой базы (список схем, таблиц и колонок):

```
- schemas:
    - name: sakila
      tables:
        - name: actor
          columns:
            - name: actor_id
              type: integer
            - name: first_name
              type: varchar(45)
            - name: last_name
              type: varchar(45)
            - name: last_update
              type: date
            - name: has_kids
              type: boolean
        - name: address
          columns:
            - name: address_id
              type: integer
            - name: city_id
              type: integer
```

У генератора должен быть метод:

```
/**
 * tablePattern - regexp of qualified table name
 *     e.g. "sakila\.actor ", "sakila\..*", ".*\.person"
 * query - text to find in database e.g. "Alice", "42", "true"
 * caseSensitive - whether to use LIKE or ILIKE operation for varchar columns
 */
List<String> generateSelects(String tablePattern, String query, boolean caseSensitive)
```

Результат вызова этого метода - список строк с SELECT запросами к таблицам, имена которых соответствуют tablePattern

**Пример 1**

Запрос: generateSelects(".*\.actor", "true", true)

Результат:

```
SELECT * FROM actor
WHERE first_name ILIKE '%true%'
  OR last_name ILIKE '%true%'
  OR has_kids = true
```

**Пример 2**

Запрос: generateSelects(".*\.actor", "2016-02-15", true)

Результат:

```
SELECT * FROM actor
WHERE first_name ILIKE '%2016-02-15%'
  OR last_name ILIKE '%2016-02-15%'
  OR last_update = '2016-02-15'
```

**Пример 3**

Запрос: generateSelects(".*\.actor", "42", false)

Результат:
```
SELECT * FROM actor
WHERE actor_id = 42
  OR first_name LIKE '%42%'
  OR last_name LIKE '%42%'
```

Подробнее про типы и операции

Ваш генератор должен обращать внимание на типы колонок, и генерировать для них подходящие операции:

* varchar(N) сравнивать с помощью LIKE или ILIKE (case insensitive), например first_name LIKE '%Alice%'
* integer сравнивать только с числовыми литералами, например actor_id = 42
* boolean сравнивать только с boolean-литералами true и false
* date можно сравнивать со строковыми литералами, только если они подходят под формат ГГГГ-ММ-ДД (нужно также учитывать количество дней в разных месяцах)


