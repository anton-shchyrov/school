package org.example.school.users.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class LocalDateAdapterTest {
    private static class TestClass {
        private final LocalDate date;

        @SuppressWarnings("SameParameterValue")
        private TestClass(LocalDate date) {
            this.date = date;
        }

        public LocalDate getDate() {
            return date;
        }
    }

    private static final LocalDate TEST_DATE = LocalDate.of(2022, 1, 2);
    private static final String TEST_JSON = "{\"date\":\"2022-01-02\"}";

    private static Gson gson;

    @BeforeAll
    static void init() {
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
    }

    @Test
    void testSerialize() {
        TestClass item = new TestClass(TEST_DATE);
        String str = gson.toJson(item);
        MatcherAssert.assertThat(str, Matchers.is(TEST_JSON));
    }

    @Test
    void testDeserialize() {
        TestClass item = gson.fromJson(TEST_JSON, TestClass.class);
        MatcherAssert.assertThat(item.getDate(), Matchers.is(TEST_DATE));
    }
}