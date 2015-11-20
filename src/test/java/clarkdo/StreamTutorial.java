package clarkdo;

import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by clark on 2015/11/19.
 */
public class StreamTutorial {

    @Test
    public void testBasis() {
        List<String> myList =
                Arrays.asList("a1", "a2", "b1", "c2", "c1");

        List<String> results = new ArrayList<>();

        myList.stream()
                .filter(s -> s.startsWith("c"))
                .map(String::toUpperCase)
                .sorted()
                .forEach(results::add);

        assertEquals("C1", results.get(0));
    }

    @Test
    public void testStreamOf() {
        List<String> results = new ArrayList<>();

        Arrays.asList("a1", "a2", "a3")
                .stream()
                .findFirst()
                .ifPresent(results::add);

        assertEquals("a1", results.get(0));

        results.clear();

        Stream.of("a1", "a2", "a3")
                .findFirst()
                .ifPresent(results::add);

        assertEquals("a1", results.get(0));
    }

    @Test
    public void testRange() {
        List<Integer> expects = Arrays.asList(1, 2, 3);
        List<Integer> results = new ArrayList<>();

        IntStream.range(1, 4)
                .forEach(results::add);

        assertEquals(expects, results);
    }

    @Test
    public void testMap() {
        Arrays.stream(new int[]{1, 2, 3})
                .map(n -> 2 * n + 1)
                .average()
                .ifPresent(x -> assertEquals(5.0d, x, 0));

        Stream.of("a1", "a2", "a3")
                .map(s -> s.substring(1))
                .mapToInt(Integer::parseInt)
                .max()
                .ifPresent(x -> assertEquals(3, x, 0));

        List<String> results = new ArrayList<>();

        IntStream.range(1, 4)
                .mapToObj(i -> "a" + i)
                .forEach(results::add);

        assertEquals(Arrays.asList("a1", "a2", "a3"), results);
        results.clear();

        Stream.of(1.0, 2.0, 3.0)
                .mapToInt(Double::intValue)
                .mapToObj(i -> "a" + i)
                .forEach(results::add);

        assertEquals(Arrays.asList("a1", "a2", "a3"), results);
    }

    @Test
    public void testOrder() {
        List<String> results = new ArrayList<>();

        Stream.of("d2", "a2", "b1", "b3", "c")
                .filter(results::add);

        assertTrue(results.isEmpty());
        results.clear();

        List<String> expects =
                Arrays.asList("d2", "d2", "a2", "a2", "b1", "b1", "b3", "b3", "c", "c");

        Stream.of("d2", "a2", "b1", "b3", "c")
                .filter(results::add)
                .forEach(results::add);

        assertEquals(expects, results);
        results.clear();

        expects = Arrays.asList("D2", "A2");
        Stream.of("d2", "a2", "b1", "b3", "c")
                .map(String::toUpperCase)
                .anyMatch(s -> results.add(s) && s.startsWith("A"));

        assertEquals(expects, results);
        results.clear();

        expects = Collections.singletonList("A2");
        Stream.of("d2", "a2", "b1", "b3", "c")
                .map(String::toUpperCase)
                .filter(s -> s.startsWith("A"))
                .forEach(results::add);

        assertEquals(expects, results);
        results.clear();

        Stream.of("d2", "a2", "b1", "b3", "c")
                .filter(s -> {
                    System.out.println("filter: " + s);
                    return s.startsWith("a");
                })
                .sorted((s1, s2) -> {
                    System.out.printf("sort: %s; %s\n", s1, s2);
                    return s1.compareTo(s2);
                })
                .map(s -> {
                    System.out.println("map: " + s);
                    return s.toUpperCase();
                })
                .forEach(s -> System.out.println("forEach: " + s));
    }

    @Test
    public void testReusableStream() {
        Supplier<Stream<String>> streamSupplier =
                () -> Stream.of("d2", "a2", "b1", "b3", "c");
        assertTrue(streamSupplier.get().anyMatch(s -> s.startsWith("a")));
        assertFalse(streamSupplier.get().noneMatch(s -> s.startsWith("a")));
    }

    @Test
    public void testAdvancedOperations() {

        Person max = new Person("Max", 18),
                peter = new Person("Peter", 23),
                pamela = new Person("Pamela", 23),
                david = new Person("David", 12);

        List<Person> persons =
                Arrays.asList(max, peter, pamela, david);

        List<Person> filtered =
                persons
                        .stream()
                        .filter(p -> p.name.startsWith("P"))
                        .collect(Collectors.toList());

        assertEquals(Arrays.asList(peter, pamela), filtered);

        Map<Integer, List<Person>> personsByAge = persons
                .stream()
                .collect(Collectors.groupingBy(p -> p.age));

        assertEquals(Collections.singletonList(max), personsByAge.get(18));
        assertEquals(Arrays.asList(peter, pamela), personsByAge.get(23));
        assertEquals(Collections.singletonList(david), personsByAge.get(12));

        Double averageAge = persons
                .stream()
                .collect(Collectors.averagingInt(p -> p.age));

        assertEquals(19.0d, averageAge, 0);

        IntSummaryStatistics ageSummary =
                persons
                        .stream()
                        .collect(Collectors.summarizingInt(p -> p.age));

        assertEquals(12, ageSummary.getMin(), 0);
        assertEquals(23, ageSummary.getMax(), 0);
        assertEquals(76, ageSummary.getSum(), 0);
        assertEquals(4, ageSummary.getCount(), 0);
        assertEquals(19.0d, ageSummary.getAverage(), 0);

        String phrase = persons
                .stream()
                .filter(p -> p.age >= 18)
                .map(p -> p.name)
                .collect(Collectors.joining(" and ", "In Germany ", " are of legal age."));

        assertEquals("In Germany Max and Peter and Pamela are of legal age.", phrase);

        Map<Integer, String> expects = new HashMap<>();
        expects.put(18, "Max");
        expects.put(23, "Peter;Pamela");
        expects.put(12, "David");
        Map<Integer, String> map = persons
                .stream()
                .collect(Collectors.toMap(
                        p -> p.age,
                        p -> p.name,
                        (name1, name2) -> name1 + ";" + name2));

        assertEquals(expects, map);

        Collector<Person, StringJoiner, String> personNameCollector =
                Collector.of(
                        () -> new StringJoiner(" | "),          // supplier
                        (j, p) -> j.add(p.name.toUpperCase()),  // accumulator
                        (j1, j2) -> j1.merge(j2),               // combiner
                        StringJoiner::toString);                // finisher

        String names = persons
                .stream()
                .collect(personNameCollector);

        assertEquals("MAX | PETER | PAMELA | DAVID", names);
    }

    @Test
    public void testFlatMap() {
        List<Foo> foos = new ArrayList<>();

        IntStream.range(1, 4)
                .forEach(i -> foos.add(new Foo("Foo" + i)));

        foos.forEach(f ->
                IntStream.range(1, 4)
                        .forEach(i -> f.bars.add(new Bar("Bar" + i + " <- " + f.name))));
        foos.stream()
                .flatMap(f -> f.bars.stream())
                .forEach(b -> System.out.println(b.name));
    }

    class Person {
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class Foo {
        String name;
        List<Bar> bars = new ArrayList<>();

        Foo(String name) {
            this.name = name;
        }
    }

    class Bar {
        String name;

        Bar(String name) {
            this.name = name;
        }
    }
}
