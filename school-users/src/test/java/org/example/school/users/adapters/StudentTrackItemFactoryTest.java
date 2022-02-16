package org.example.school.users.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.school.users.protocol.student.GradeDateItem;
import org.example.school.users.protocol.student.StudentTrackItem;
import org.example.school.users.protocol.teacher.GradeType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

class StudentTrackItemFactoryTest {
    private static class StudentTrackItemAverage extends StudentTrackItem {
        public final double average;
        private StudentTrackItemAverage() {
            super(null, null);
            average = -1;
        }
    }
    private static final StudentTrackItem TEST_ITEM = new StudentTrackItem("Subject", "Teacher");
    private static final LocalDate TEST_DATE = LocalDate.now();

    private static Gson gson;

    @BeforeAll
    static void init() {
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapterFactory(new StudentTrackItemFactory())
            .create();
        TEST_ITEM.grades.addAll(
            Arrays.asList(
                new GradeDateItem(0, GradeType.CURRENT, TEST_DATE),
                new GradeDateItem(1, GradeType.CURRENT, TEST_DATE),
                new GradeDateItem(2, GradeType.CURRENT, TEST_DATE),
                new GradeDateItem(3, GradeType.CURRENT, TEST_DATE)
            )
        );
    }

    @Test
    void testSerialize() {
        String str = gson.toJson(TEST_ITEM);
        StudentTrackItemAverage deserialized = gson.fromJson(str, StudentTrackItemAverage.class);
        MatcherAssert.assertThat(TEST_ITEM, Matchers.is(deserialized));
        MatcherAssert.assertThat(deserialized.average, Matchers.closeTo(TEST_ITEM.getAverage(), 0.0001));
    }

    @Test
    void testDeserialize() {
        String str = gson.toJson(TEST_ITEM);
        StudentTrackItem deserialized = gson.fromJson(str, StudentTrackItem.class);
        MatcherAssert.assertThat(TEST_ITEM, Matchers.is(deserialized));
    }

}