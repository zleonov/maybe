package software.leonov.maybe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class MaybeTest {

    @Test
    void test_absent() {
        final Maybe<String> absent = Maybe.absent();

        assertFalse(absent.isPresent());
    }

    @Test
    void test_of_isPresent() {
        final Maybe<String> present = Maybe.of("Hello");

        assertTrue(present.isPresent());
    }

    @Test
    void test_map_get_existing_key() {
        final Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        final Maybe<String> value = Maybe.get(map, "key");

        assertEquals("value", value.defaultTo("default"));
    }

    @Test
    void test_map_get_not_found() {
        final Map<String, String> map = new HashMap<>();

        final Maybe<String> absent = Maybe.get(map, "unknown");

        assertEquals("default", absent.defaultTo("default"));
    }

    @Test
    void test_map_get_null_key() {
        final Map<String, String> map = new HashMap<>();
        map.put(null, "value");

        final Maybe<String> value = Maybe.get(map, null);

        assertTrue(value.isPresent());
        assertEquals("value", value.get());
    }

//    @Test
//    void test_get_PathExisting() {
//        Path path = Paths.get("path/to/file");
//        final Option<Path> pathOption = Option.get(path);
//        assertTrue(pathOption.isPresent());
//    }

//    @Test
//    void test_get_PathNonExisting() {
//        Path               path       = Paths.get("non/existing/file");
//        final Option<Path> pathOption = Option.get(path);
//        assertFalse(pathOption.isPresent());
//    }

    @Test
    void test_defaultTo_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertEquals("Hello", present.defaultTo("default"));
    }

    @Test
    void test_defaultTo_null() {
        final Maybe<String> maybe = Maybe.of(null);

        assertNull(maybe.defaultTo("default"));
    }

    @Test
    void test_defaultTo_absent() {
        final Maybe<String> absent = Maybe.absent();
        assertEquals("default", absent.defaultTo("default"));
    }

    @Test
    void test_filter_matching_predicate() {
        final Maybe<String> present  = Maybe.of("Hello");
        final Maybe<String> filtered = present.filter(s -> s.startsWith("H"));

        assertTrue(filtered.isPresent());
    }

    @Test
    void test_filter_non_matching_predicate() {
        final Maybe<String> present  = Maybe.of("Hello");
        final Maybe<String> filtered = present.filter(s -> s.startsWith("W"));

        assertFalse(filtered.isPresent());
    }

    @Test
    void test_get_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertEquals("Hello", present.get());
    }

    @Test
    void test_get_absent_throws() {
        final Maybe<String> absent = Maybe.absent();

        assertThrows(NoSuchElementException.class, absent::get);
    }

    @Test
    void test_ifNotNull_present() {
        final Maybe<String> present = Maybe.of("Hello");
        final StringBuilder result  = new StringBuilder();

        present.ifNotNull(result::append);

        assertEquals("Hello", result.toString());
    }

    @Test
    void test_ifNotNull_null() {
        final Maybe<String> maybe  = Maybe.of(null);
        final StringBuilder result = new StringBuilder();

        maybe.ifNotNull(result::append);

        assertEquals(0, result.length());
    }

    @Test
    void test_ifNull_present() {
        final Maybe<String> present = Maybe.of("Hello");
        final Maybe<String> other   = Maybe.of("Other");

        assertSame(present, present.ifNull(other));
    }

    @Test
    void test_ifNull_null_value() {
        final Maybe<String> pption = Maybe.of(null);
        final Maybe<String> other  = Maybe.of("Other");

        assertSame(other, pption.ifNull(other));
    }

    @Test
    void test_ifNull_null_throws() {
        final Maybe<String> present = Maybe.of("Hello");
        final Maybe<String> other   = null;

        assertThrows(NullPointerException.class, () -> present.ifNull(other));
    }

    @Test
    void test_ifNull_runnable() {
        final Maybe<String> maybe  = Maybe.of(null);
        final StringBuilder result = new StringBuilder();

        maybe.ifNull(() -> result.append("Value is null"));

        assertEquals("Value is null", result.toString());
    }

    @Test
    void test_ifPresent_present() {
        final Maybe<String> present = Maybe.of("Hello");
        final StringBuilder result  = new StringBuilder();

        present.ifPresent(result::append);

        assertEquals("Hello", result.toString());
    }

    @Test
    void test_ifPresent_absent() {
        final Maybe<String> absent = Maybe.absent();
        final StringBuilder result = new StringBuilder();

        absent.ifPresent(result::append);

        assertEquals(0, result.length());
    }

    @Test
    void test_isNull_WithNullValue() {
        final Maybe<String> maybe = Maybe.of(null);

        assertTrue(maybe.isNull());
    }

    @Test
    void test_isNull_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertFalse(present.isNull());
    }

    @Test
    void test_map_present() {
        final Maybe<String>  present = Maybe.of("Hello");
        final Maybe<Integer> mapped  = present.map(String::length);

        assertEquals(Integer.valueOf(5), mapped.get());
    }

    @Test
    void test_map_absent() {
        final Maybe<String>  absent = Maybe.absent();
        final Maybe<Integer> mapped = absent.map(String::length);

        assertFalse(mapped.isPresent());
    }

    @Test
    void test_or_prsent() {
        final Maybe<String> present = Maybe.of("Hello");
        final Maybe<String> other   = Maybe.of("Other");

        assertSame(present, present.or(other));
    }

    @Test
    void test_or_absent() {
        final Maybe<String> absent = Maybe.absent();
        final Maybe<String> other  = Maybe.of("Other");

        assertSame(other, absent.or(other));
    }

    @Test
    void test_otherwise() {
        final Maybe<String> absent = Maybe.absent();
        final StringBuilder result = new StringBuilder();

        absent.otherwise(() -> result.append("Value is absent"));

        assertEquals("Value is absent", result.toString());
    }

    @Test
    void test_orElseGet() {
        final Maybe<String> absent = Maybe.absent();
        final String        value  = absent.orElseGet(() -> "default");

        assertEquals("default", value);
    }

    @Test
    void test_orElseThrow_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertEquals("Hello", present.orElseThrow(IllegalStateException::new));
    }

    @Test
    void test_orElseThrow_absent() {
        final Maybe<String> absent = Maybe.absent();

        assertThrows(IllegalStateException.class, () -> absent.orElseThrow(IllegalStateException::new));
    }

    @Test
    void test_orNull_present() {
        final Maybe<String> present = Maybe.of("Hello");
        final String        value   = present.orNull();

        assertEquals("Hello", value);
    }

    @Test
    void test_orNull_absent() {
        final Maybe<String> absent = Maybe.absent();

        assertNull(absent.orNull());
    }

    @Test
    void test_stream_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertEquals(1, present.stream().count());
    }

    @Test
    void test_stream_absent() {
        final Maybe<String> absent = Maybe.absent();

        assertEquals(0, absent.stream().count());
    }

    @Test
    void test_toOptional_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertTrue(present.toOptional().isPresent());
    }

    @Test
    void test_toOptional_absent() {
        final Maybe<String> absent = Maybe.absent();

        assertFalse(absent.toOptional().isPresent());
    }

    @Test
    void test_equals_hashCode() {
        final Maybe<String> option1 = Maybe.of("Hello");
        final Maybe<String> option2 = Maybe.of("Hello");
        final Maybe<String> option3 = Maybe.of("World");

        assertEquals(option1, option2);
        assertNotEquals(option1, option3);
        assertEquals(option1.hashCode(), option2.hashCode());
        assertNotEquals(option1.hashCode(), option3.hashCode());
    }

    @Test
    void test_toString_present() {
        final Maybe<String> present = Maybe.of("Hello");

        assertEquals("Maybe[Hello]", present.toString());
    }

    @Test
    void test_toString_absent() {
        final Maybe<String> absent = Maybe.absent();

        assertEquals("Maybe[]", absent.toString());
    }
}
