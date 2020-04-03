import database.Column;
import database.Database;
import database.Schema;
import database.Table;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate all possible SELECT queries from given
 * database structure according to given query test
 *
 * @author Arina Fedorovskaya
 */

public class Generator {
    private Constructor constructor;
    private Database database;

    /**
     * Constructor a {@code Generator}
     * Defines strict types for objects to which .yaml file is converted
     */
    Generator() {
        this.constructor = new Constructor(Database.class);
        TypeDescription customTypeDescription = new TypeDescription(Database.class);
        customTypeDescription.addPropertyParameters("schemas", Schema.class);
        customTypeDescription.addPropertyParameters("tables", Table.class);
        customTypeDescription.addPropertyParameters("columns", Column.class);
        this.constructor.addTypeDescription(customTypeDescription);
    }

    /**
     * Convert given .yaml file with database structure to Java object
     *
     * @param filepath path to .yaml file
     */
    public void load(String filepath) {
        Yaml yaml = new Yaml(this.constructor);
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filepath);

        this.database = yaml.load(inputStream);
    }

    /**
     * Generate SELECT queries for loaded database structure according to query text
     *
     * @param   tablePattern    regexp of qualified table name
     *                          e.g. "sakila\.actor", "sakila\..*", ".*\.person"
     * @param   query           text to find in database
     *                          e.g. "Alice", "42", "true"
     * @param   caseSensitive   whether to use LIKE or ILIKE operation for varchar columns
     */
    public List<String> generateSelects(String tablePattern, String query, boolean caseSensitive) {
        if(this.database == null){
            throw new RuntimeException("you did not load .yaml file with database structure");
        }

        if (!checkTablePattern(tablePattern)) {
            throw new RuntimeException("syntax of table pattern is incorrect");
        }

        String[] names = tablePattern.split("\\\\.");
        ArrayList<Table> tables = getTablesList(names);
        ArrayList<String> selects = new ArrayList<>();

        if (tables.isEmpty()) {
            return selects;
        }

        String like = caseSensitive ? "ILIKE" : "LIKE";
        String typeQuery = determineType(query);

        for (Table table : tables) {
            StringBuilder partAfterWhere = new StringBuilder();

            for (Column column : table.getColumns()) {
                if (column.getType().contains("varchar")) {
                    if(partAfterWhere.length() != 0){
                        partAfterWhere.append(" OR ");
                    }

                    partAfterWhere.append(column.getName()).append(" ").append(like).append(" '%")
                            .append(query).append("%'");
                } else {
                    if (column.getType().equals(typeQuery)) {
                        if(partAfterWhere.length() != 0){
                            partAfterWhere.append(" OR ");
                        }

                        if(!typeQuery.equals("date")) {
                            partAfterWhere.append(column.getName()).append(" = ").append(query);
                        }else {
                            partAfterWhere.append(column.getName()).append(" = '").append(query).append("'");
                        }
                    }
                }
            }

            if (partAfterWhere.length() != 0) {
                String select = "SELECT * FROM " + table.getName() + " WHERE " + partAfterWhere;
                selects.add(select);
            }
        }

        return selects;
    }

    /**
     * Check table pattern for correctness
     *
     * @param tablePattern pattern to check
     * @return true if patter in form "name_schema\.name_table"
     *         with ".*" valid for name_schema and name_table
     */

    private boolean checkTablePattern(String tablePattern) {
        return tablePattern.matches("^([a-zA-z]+|\\.\\*)\\\\.([a-zA-z]+|\\.\\*)$");
    }

    /**
     * Get {@code ArrayList} of tables related to table pattern
     *
     * @param names array of schema and table name from pattern
     * @return {@code ArrayList} with related tables
     */

    private ArrayList<Table> getTablesList(String[] names) {
        ArrayList<Table> tables = new ArrayList<>();

        if (names[0].equals(".*")) {
            if (names[1].equals(".*")) {
                for (Schema schema : this.database.getSchemas()) {
                    tables.addAll(schema.getTables());
                }
            } else {
                for (Schema schema : this.database.getSchemas()) {
                    Table table = schema.getTableByName(names[1]);
                    if (table != null) {
                        tables.add(table);
                    }
                }
            }
        } else {
            Schema schema = this.database.getSchemaByName(names[0]);

            if (schema != null) {
                if (names[1].equals(".*")) {
                    tables.addAll(schema.getTables());
                } else {
                    Table table = schema.getTableByName(names[1]);

                    if (table != null) {
                        tables.add(table);
                    }
                }
            }
        }

        return tables;
    }

    /**
     * Determine the type of query: boolean, integer, date or varchar
     *
     * @param query text to find in database
     * @return string with corresponded type
     */
    private String determineType(String query) {
        if (query.matches("^[0-9]+$")) {
            return "integer";
        }

        if (query.matches("^(true|false)$")) {
            return "boolean";
        }

        if (query.matches("^\\d{4}[\\-/\\s]?((((0[13578])|" +
                "(1[02]))[\\-/\\s]?(([0-2][0-9])|(3[01])))|(((0[469])|" +
                "(11))[\\-/\\s]?(([0-2][0-9])|(30)))|(02[\\-/\\s]?[0-2][0-9]))$")) {
            return "date";
        }

        return "varchar";
    }
}
