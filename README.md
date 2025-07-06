# 251.ntfs-stream-inspector

Приложение для поиска подозрительных NTFS потоков. Технологии: Java8, JNA.

## описание

Программа предназначена для поиска и выявления подозрительных потоков в NTFS

## Отличие от существующих - streams, Sysinternals

[streams, Sysinternals](https://learn.microsoft.com/en-us/sysinternals/downloads/streams)

Недостатки:

- не поддерживается русский язык в именах файлов;
- не поддерживается русский язык в именах потоков;
- никакого "анализа" не проводится. например, информация о потоке "по умолчанию"
выводится всегда; информация о потоке "Zone.Identifier" выводится всегда.

Достоинства:

- есть возможность удаления всех альтернативных потоков.

## Сборка проекта

### Инструменты для сборки

- Java 1.8.
- Gradle 6.8.2

Проверено: Java 17 - не совместимый.

### сборка

```cmd
gradle clean build
```

Результат сборки - см. build\libs\ntfs-streams-inspector.jar

## Пример запуска через gradlew

````cmd
gradlew run --args="-svid D:\INS\251-ntfs-multi\bravo.txt"
````

## Пример запуска через jar

файл в репозитории - config-example\alone-run.cmd

````cmd
java -Dlog4j2.configurationFile=config2/log4j2.xml ^
-Djava.io.tmpdir=tmpFolder ^
-Dfile.encoding=cp866 ^
-jar ntfs-streams-inspector.jar ^
D:\INS\251-subfolders-iter0
````

Расшифровка параметров:

<dl>

<dt>-Dlog4j2.configurationFile</dt>
<dd>местоположение и имя файла настройки логирования log4j2. пример файла - в config-example/config2/log4j2.xml.
Кодировка текста, которая используется при выводе в лог, определяется в нем как

```xpath
/Configuration/Appenders/RollingFile/PatternLayout/@charset
```

</dd>

<dt>-Djava.io.tmpdir</dt>
<dd>папка в текущем каталоге будет использоваться для временных файлов.
А также там будет расположен DLL файл из JNA - jna-109923694\jna16115844829843351541.dll

<dt>-Dfile.encoding</dt>
<dd>переопределение кодировки в которой
выдаются сообщения через system.out и логирование в консоль</dd>

<dt>актуальные кодировки, используемые в Windows</dt>
<dd>
    <dl>
        <dt>cp866</dt>
        <dd>кодировка по умолчанию в cmd</dd>
        <dt>cp1251</dt>
        <dd>кодировка по умолчанию, выводимая в Winows</dd>
    </dl>
</dl>
