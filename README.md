Maybe
=====
A `null` aware monadic _maybe_ type in idiomatic Java.

Overview
--------
Managing code that handles _optional_ values is often complex and cumbersome. Prior to Java 8 optional values were usually represented as `null`s. For example `Map.get(K key)` returns a `null` value if there is no mapping for the requested key, **but** a `null` value can sometimes be a valid return type for maps which support `null` values. And then there are maps which support `null` keys as well? As we all know code that handles (or forgets to handle) `null`s cases is an incredibly common source of errors.

To address the issue Java 8 introduced the `Optional` class which can hold either a single non-`null` value or _nothing at all_. So what's the problem?

Using `Optional` effectively removes `null` from a language where `null`s can explicitly represent valid parameters and return types.

Maybe is `null` aware. Meaning unlike `Optional` it does not use `null` to signify _no value_.

Do yourself a favor and start writing code like this:

```Java
        final Maybe<String> maybe = ...
        
        ...
        
        final String value = maybe.ifNull(() ->    logger.fine("value is null"))
                                  .ifNotNull(t ->  logger.fine("value is " + t))
                                  .otherwise(() -> logger.fine("value is absent: using default value"))
                                  .orElse(DEFAULT_VALUE);
```

Documentation
-------------
The latest API documentation can be accessed [here](https://zleonov.github.io/optional-config/api/latest).