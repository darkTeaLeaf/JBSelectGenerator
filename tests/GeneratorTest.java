import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {
    private Generator generator = new Generator();

    @Test
    public void test1(){
        generator.load("yaml/database_with_single_schema.yaml");
        List<String> output = generator.generateSelects(".*\\.actor", "true", true);

        assertEquals("[SELECT * FROM actor WHERE first_name ILIKE '%true%' " +
                "OR last_name ILIKE '%true%' OR has_kids = true]", output.toString());
    }

    @Test
    public void test2(){
        generator.load("yaml/database_with_single_schema.yaml");
        List<String> output = generator.generateSelects(".*\\.actor", "2016-02-15", true);

        assertEquals("[SELECT * FROM actor WHERE first_name ILIKE '%2016-02-15%' " +
                "OR last_name ILIKE '%2016-02-15%' " +
                "OR last_update = '2016-02-15']", output.toString());
    }

    @Test
    public void test3(){
        generator.load("yaml/database_with_single_schema.yaml");
        List<String> output = generator.generateSelects(".*\\.actor", "42", false);

        assertEquals("[SELECT * FROM actor WHERE actor_id = 42 " +
                "OR first_name LIKE '%42%' " +
                "OR last_name LIKE '%42%']", output.toString());
    }

    @Test
    public void test4(){
        generator.load("yaml/database_with_multiple_schemas.yaml");
        List<String> output = generator.generateSelects(".*\\.address", "42", false);

        assertEquals("[SELECT * FROM address WHERE address_id = 42 OR city_id = 42," +
                " SELECT * FROM address WHERE store_id = 42 OR city_id = 42]", output.toString());
    }

    @Test
    public void test5(){
        generator.load("yaml/database_with_multiple_schemas.yaml");
        List<String> output = generator.generateSelects("sakila\\.address", "42", false);

        assertEquals("[SELECT * FROM address WHERE address_id = 42 OR city_id = 42]", output.toString());
    }

    @Test
    public void test6(){
        generator.load("yaml/database_with_multiple_schemas.yaml");
        List<String> output = generator.generateSelects("sakila\\..*", "42", false);

        assertEquals("[SELECT * FROM actor WHERE actor_id = 42 " +
                "OR first_name LIKE '%42%' OR last_name LIKE '%42%', " +
                "SELECT * FROM address WHERE address_id = 42 OR city_id = 42]", output.toString());
    }

    @Test
    public void test7(){
        generator.load("yaml/database_with_multiple_schemas.yaml");
        List<String> output = generator.generateSelects(".*\\..*", "true", false);

        assertEquals("[SELECT * FROM actor WHERE first_name LIKE '%true%' " +
                "OR last_name LIKE '%true%' OR has_kids = true, " +
                "SELECT * FROM book WHERE name LIKE '%true%' OR author LIKE '%true%' OR bestseller = true, " +
                "SELECT * FROM address WHERE has_terminal = true]", output.toString());
    }

    @Test
    public void test8(){
        generator.load("yaml/database_with_multiple_schemas.yaml");
        List<String> output = generator.generateSelects(".*\\.child", "true", false);

        assertEquals("[]", output.toString());
    }

    @Test
    public void testError1(){
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> this.generator
                .generateSelects(".*\\..*", "true", false));

        assertTrue(thrown.getMessage().contains("you did not load .yaml file with database structure"));
    }

    @Test
    public void testError2(){
        generator.load("yaml/database_with_multiple_schemas.yaml");
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> this.generator
                .generateSelects(".*\\..34", "true", false));

        assertTrue(thrown.getMessage().contains("syntax of table pattern is incorrect"));
    }
}
